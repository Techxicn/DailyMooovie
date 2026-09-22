package com.techxicn.dailymooovie.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

/**
 * MovieRepository
 * ═══════════════
 * Lecturas del catálogo global de películas bajo /movies.
 *
 * Todas las operaciones son suspend y usan Task.await() (misma estrategia que
 * AuthManager). Se apoya en el comportamiento por defecto del SDK de Realtime
 * Database (sin caché personalizado ni persistencia offline avanzada).
 *
 * Nota sobre el id: en RTDB el id de la película es la CLAVE del nodo, no un
 * campo. Al deserializar se rellena Movie.id con snapshot.key para que las capas
 * superiores puedan identificar y navegar por película.
 */
class MovieRepository(
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance()
) {

    private val moviesRef: DatabaseReference get() = db.getReference("movies")

    /**
     * Película del día: la que tiene releaseDate == fecha actual ("yyyy-MM-dd").
     * Devuelve null si no hay ninguna película programada para hoy.
     */
    suspend fun getMovieOfTheDay(): Movie? = getMovieForDate(DateUtils.today())

    /**
     * Película cuyo releaseDate coincide exactamente con [date] ("yyyy-MM-dd").
     * Devuelve null si no existe.
     */
    suspend fun getMovieForDate(date: String): Movie? {
        val snapshot = moviesRef
            .orderByChild("releaseDate")
            .equalTo(date)
            .get()
            .await()
        // equalTo puede devolver varios hijos; tomamos el primero.
        return snapshot.children.firstOrNull()?.toMovie()
    }

    /**
     * Catálogo de un mes: todas las películas cuyo releaseDate cae en [year]-[month]
     * (month es 1..12). Ordenadas por releaseDate ascendente.
     *
     * Se filtra por rango [inicio, fin) usando el prefijo "yyyy-MM": desde
     * "yyyy-MM-01" (startAt) hasta el primer día del mes siguiente (endBefore
     * emulado con endAt del último instante del mes actual vía prefijo).
     */
    suspend fun getCatalogForMonth(year: Int, month: Int): List<Movie> {
        val prefix = DateUtils.monthPrefix(year, month) // "yyyy-MM"
        // Rango lexicográfico: todo lo que empiece por "yyyy-MM-" queda entre
        // "yyyy-MM-" y "yyyy-MM-\uf8ff" (carácter alto que cierra el prefijo).
        val snapshot = moviesRef
            .orderByChild("releaseDate")
            .startAt("$prefix-")
            .endAt("$prefix-\uf8ff")
            .get()
            .await()
        return snapshot.children
            .mapNotNull { it.toMovie() }
            .sortedBy { it.releaseDate }
    }

    /** Película por id (clave del nodo). Devuelve null si no existe. */
    suspend fun getMovieById(movieId: String): Movie? {
        if (movieId.isBlank()) return null
        val snapshot = moviesRef.child(movieId).get().await()
        return snapshot.toMovie()
    }

    /** Deserializa un snapshot de /movies/{id} a Movie, rellenando el id con la clave. */
    private fun DataSnapshot.toMovie(): Movie? {
        val movie = getValue(Movie::class.java) ?: return null
        movie.id = key.orEmpty()
        return movie
    }
}
