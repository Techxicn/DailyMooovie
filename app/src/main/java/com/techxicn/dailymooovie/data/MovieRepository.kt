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
     * Todos los ids del catálogo (las CLAVES de los nodos bajo /movies).
     *
     * Es la fuente para construir/sincronizar la dailyQueue de cada usuario: se
     * lee la lista completa de ids y luego DailyQueueRepository la mezcla y guarda.
     * No deserializa las películas completas (solo necesita las claves).
     */
    suspend fun getAllMovieIds(): List<String> {
        val snapshot = moviesRef.get().await()
        return snapshot.children.mapNotNull { it.key }
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
