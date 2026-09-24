package com.techxicn.dailymooovie.data

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

/**
 * Models.kt
 * ═════════
 * Data classes de dominio que mapean 1:1 la estructura de Realtime Database.
 *
 * Requisitos de la deserialización automática de RTDB:
 *   • Constructor vacío (aquí se logra con valores por defecto en todos los campos).
 *   • Propiedades var/val con getters públicos y nombres iguales a las claves del nodo.
 *   • Los tipos primitivos (Int/Long) NO pueden ser null → se usan valores por defecto.
 *   • Los campos opcionales del nodo (p. ej. watchedAt solo si status = "watched")
 *     se modelan como nullable (Long?).
 *
 * @IgnoreExtraProperties evita que RTDB lance si el nodo trae claves extra.
 * Los campos anotados con @get:Exclude NO se serializan a la base: se usan solo
 * en memoria para llevar el id (que en RTDB es la CLAVE del nodo, no un campo).
 */

// ─────────────────────────────────────────────────────────────────────────
//  /movies/{movieId}
// ─────────────────────────────────────────────────────────────────────────

@IgnoreExtraProperties
data class Movie(
    val title: String = "",
    val originalTitle: String = "",
    val year: Int = 0,
    val country: String = "",
    val posterUrl: String = "",
    val synopsis: String = "",
    val trailerUrl: String = "",
    /** Director de la película. */
    val director: String = "",
    /** Géneros; pueden venir varios separados por " / " (p. ej. "Drama / Fantasía"). */
    val genre: String = "",
    /** Duración en minutos. */
    val durationMinutes: Int = 0,
    /** Valoración (0.0..10.0 típicamente). */
    val rating: Double = 0.0,
    /** URL de Letterboxd (opcional, solo referencia). */
    val letterboxdUrl: String = ""
) {
    /**
     * Id de la película = clave del nodo en RTDB. No se persiste como campo;
     * lo rellena el repositorio a partir de snapshot.key al leer.
     */
    @get:Exclude
    var id: String = ""
}

// ─────────────────────────────────────────────────────────────────────────
//  /users/{uid}/movies/{movieId}
// ─────────────────────────────────────────────────────────────────────────

@IgnoreExtraProperties
data class UserMovieStatus(
    /** "watchlist" o "watched". Ver [MovieStatus]. */
    val status: String = "",
    /** Timestamp (epoch millis). Solo presente si status = "watched". */
    val watchedAt: Long? = null
) {
    /** Id de la película = clave del nodo. No se persiste como campo. */
    @get:Exclude
    var movieId: String = ""
}

/** Valores válidos del campo status. Centralizados para evitar strings sueltos. */
object MovieStatus {
    const val WATCHLIST = "watchlist"
    const val WATCHED = "watched"
}

// ─────────────────────────────────────────────────────────────────────────
//  /users/{uid}/streak
// ─────────────────────────────────────────────────────────────────────────

@IgnoreExtraProperties
data class UserStreak(
    val current: Int = 0,
    val longest: Int = 0,
    /** Última fecha en la que se marcó una película como vista. Formato "yyyy-MM-dd". */
    val lastWatchedDate: String = ""
)
