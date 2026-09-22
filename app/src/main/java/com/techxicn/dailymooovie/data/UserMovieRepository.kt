package com.techxicn.dailymooovie.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

/**
 * UserMovieRepository
 * ═══════════════════
 * Lecturas/escrituras del estado por-usuario bajo /users/{uid}:
 *   • /users/{uid}/movies/{movieId} → status ("watchlist"|"watched") + watchedAt
 *   • /users/{uid}/streak           → current, longest, lastWatchedDate
 *
 * El uid sale del usuario autenticado (FirebaseAuth). Si no hay sesión, las
 * operaciones de escritura lanzan IllegalStateException y las de lectura
 * devuelven vacío/0: al flujo de la app no se llega sin login, así que esto es
 * solo una salvaguarda.
 *
 * Todas las operaciones son suspend y usan Task.await(), coherente con el resto
 * del proyecto. Se usa el comportamiento por defecto del SDK (sin caché propio).
 */
class UserMovieRepository(
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    /** uid del usuario actual, o null si no hay sesión. */
    private val uid: String? get() = auth.currentUser?.uid

    private fun requireUid(): String =
        uid ?: throw IllegalStateException("No hay usuario autenticado.")

    /** /users/{uid}/movies */
    private fun userMoviesRef(): DatabaseReference =
        db.getReference("users").child(requireUid()).child("movies")

    /** /users/{uid}/streak */
    private fun streakRef(): DatabaseReference =
        db.getReference("users").child(requireUid()).child("streak")

    // ─────────────────────────────────────────────────────────────────────
    //  ESCRITURAS DE ESTADO
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Marca [movieId] como "watchlist". Escribe status = "watchlist" y limpia
     * watchedAt (no aplica en watchlist).
     */
    suspend fun addToWatchlist(movieId: String) {
        val value = mapOf(
            "status" to MovieStatus.WATCHLIST,
            "watchedAt" to null
        )
        userMoviesRef().child(movieId).updateChildren(value).await()
    }

    /**
     * Marca [movieId] como "watched": status = "watched" + watchedAt = ahora.
     * Además actualiza el streak del usuario (ver [updateStreakOnWatched]).
     */
    suspend fun markAsWatched(movieId: String) {
        val value = mapOf(
            "status" to MovieStatus.WATCHED,
            "watchedAt" to System.currentTimeMillis()
        )
        userMoviesRef().child(movieId).updateChildren(value).await()
        updateStreakOnWatched()
    }

    /**
     * Quita [movieId] del estado del usuario (elimina el nodo completo, tanto de
     * watchlist como de watched).
     */
    suspend fun removeMovie(movieId: String) {
        userMoviesRef().child(movieId).removeValue().await()
    }

    // ─────────────────────────────────────────────────────────────────────
    //  LECTURAS
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Estado del usuario para una película concreta, o null si no la ha marcado.
     * Útil para reflejar el estado real en Today / Film / Catalog.
     */
    suspend fun getStatus(movieId: String): UserMovieStatus? {
        if (uid == null || movieId.isBlank()) return null
        val snapshot = userMoviesRef().child(movieId).get().await()
        val status = snapshot.getValue(UserMovieStatus::class.java) ?: return null
        status.movieId = snapshot.key.orEmpty()
        return status
    }

    /**
     * Mapa {movieId → UserMovieStatus} con TODO el estado del usuario. Práctico
     * para el catálogo: se lee una vez y se cruza contra las películas del mes.
     */
    suspend fun getAllStatuses(): Map<String, UserMovieStatus> {
        if (uid == null) return emptyMap()
        val snapshot = userMoviesRef().get().await()
        return snapshot.children.mapNotNull { child ->
            val status = child.getValue(UserMovieStatus::class.java) ?: return@mapNotNull null
            val key = child.key ?: return@mapNotNull null
            status.movieId = key
            key to status
        }.toMap()
    }

    /**
     * Ids de las películas con status "watchlist" (para la pantalla Watchlist).
     * El repositorio de películas resuelve luego los datos completos por id.
     */
    suspend fun getWatchlistMovieIds(): List<String> {
        if (uid == null) return emptyList()
        val snapshot = userMoviesRef()
            .orderByChild("status")
            .equalTo(MovieStatus.WATCHLIST)
            .get()
            .await()
        return snapshot.children.mapNotNull { it.key }
    }

    /** Conteo total de películas con status "watched" (para stats de Profile). */
    suspend fun getWatchedCount(): Int {
        if (uid == null) return 0
        val snapshot = userMoviesRef()
            .orderByChild("status")
            .equalTo(MovieStatus.WATCHED)
            .get()
            .await()
        return snapshot.childrenCount.toInt()
    }

    /**
     * Conteo de películas con status "watched" cuyo watchedAt cae en [year].
     * Para "Mooovies watched this year". Se filtra en cliente porque el año se
     * deriva del timestamp watchedAt.
     */
    suspend fun getWatchedCountForYear(year: Int): Int {
        if (uid == null) return 0
        val snapshot = userMoviesRef()
            .orderByChild("status")
            .equalTo(MovieStatus.WATCHED)
            .get()
            .await()
        val cal = java.util.Calendar.getInstance()
        return snapshot.children.count { child ->
            val status = child.getValue(UserMovieStatus::class.java)
            val watchedAt = status?.watchedAt ?: return@count false
            cal.timeInMillis = watchedAt
            cal.get(java.util.Calendar.YEAR) == year
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    //  STREAK
    // ─────────────────────────────────────────────────────────────────────

    /** Lee el streak actual del usuario (por defecto 0/0/"" si no existe). */
    suspend fun getStreak(): UserStreak {
        if (uid == null) return UserStreak()
        val snapshot = streakRef().get().await()
        return snapshot.getValue(UserStreak::class.java) ?: UserStreak()
    }

    /**
     * Recalcula el streak al marcar una película como vista HOY.
     *
     * Reglas (comparando la fecha de hoy con streak.lastWatchedDate):
     *   • lastWatchedDate == hoy       → no hace nada (ya contó hoy).
     *   • lastWatchedDate == ayer      → current += 1 (racha continúa).
     *   • salto de más de un día / vacío / futuro → current = 1 (se reinicia).
     * En todos los casos que escriben: longest = max(longest, current) y
     * lastWatchedDate = hoy.
     */
    private suspend fun updateStreakOnWatched() {
        val today = DateUtils.today()
        val current = getStreak()

        // Ya se marcó una película hoy: la racha no cambia.
        if (current.lastWatchedDate == today) return

        val diff = DateUtils.daysBetween(current.lastWatchedDate, today)
        val newCurrent = if (diff == 1) current.current + 1 else 1
        val newLongest = maxOf(current.longest, newCurrent)

        val updated = UserStreak(
            current = newCurrent,
            longest = newLongest,
            lastWatchedDate = today
        )
        streakRef().setValue(updated).await()
    }
}
