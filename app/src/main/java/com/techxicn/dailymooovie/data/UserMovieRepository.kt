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
     *
     * Se lee el nodo completo /users/{uid}/movies con get() y se filtra el status
     * EN MEMORIA, en vez de usar orderByChild("status").equalTo(...). Esto evita
     * depender de la regla ".indexOn": "status" (que si falta hace que RTDB
     * rechace la query en runtime). El índice sigue recomendado (ver
     * database.rules.json) por rendimiento, pero la app ya no falla sin él.
     */
    suspend fun getWatchlistMovieIds(): List<String> {
        if (uid == null) return emptyList()
        val snapshot = userMoviesRef().get().await()
        return snapshot.children.mapNotNull { child ->
            val status = child.getValue(UserMovieStatus::class.java)
            if (status?.status == MovieStatus.WATCHLIST) child.key else null
        }
    }

    /** Conteo total de películas con status "watched" (para stats de Profile). */
    suspend fun getWatchedCount(): Int {
        if (uid == null) return 0
        val snapshot = userMoviesRef().get().await()
        return snapshot.children.count { child ->
            child.getValue(UserMovieStatus::class.java)?.status == MovieStatus.WATCHED
        }
    }

    /**
     * Conteo de películas con status "watched" cuyo watchedAt cae en [year].
     * Para "Mooovies watched this year". Se lee el nodo completo y se filtra en
     * memoria (status + año del watchedAt), sin depender del índice.
     */
    suspend fun getWatchedCountForYear(year: Int): Int {
        if (uid == null) return 0
        val snapshot = userMoviesRef().get().await()
        val cal = java.util.Calendar.getInstance()
        return snapshot.children.count { child ->
            val status = child.getValue(UserMovieStatus::class.java)
            if (status?.status != MovieStatus.WATCHED) return@count false
            val watchedAt = status.watchedAt ?: return@count false
            cal.timeInMillis = watchedAt
            cal.get(java.util.Calendar.YEAR) == year
        }
    }

    /**
     * Ids de las últimas [limit] películas marcadas como "watched", ordenadas por
     * watchedAt DESCENDENTE (más reciente primero). Para "Mooovie Discoveries" de
     * Profile. Se lee el nodo completo y se ordena/filtra en memoria (sin índice).
     */
    suspend fun getRecentWatchedMovieIds(limit: Int = 5): List<String> {
        if (uid == null) return emptyList()
        val snapshot = userMoviesRef().get().await()
        return snapshot.children
            .mapNotNull { child ->
                val status = child.getValue(UserMovieStatus::class.java)
                val key = child.key
                if (status?.status == MovieStatus.WATCHED && status.watchedAt != null && key != null) {
                    key to status.watchedAt
                } else null
            }
            .sortedByDescending { it.second }
            .take(limit)
            .map { it.first }
    }

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
