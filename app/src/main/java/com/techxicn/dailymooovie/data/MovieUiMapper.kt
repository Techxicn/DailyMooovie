package com.techxicn.dailymooovie.data

import com.techxicn.dailymooovie.model.CatalogMovieUi
import com.techxicn.dailymooovie.model.FilmUiState
import com.techxicn.dailymooovie.model.TodayUiState
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * MovieUiMapper.kt
 * ════════════════
 * Traduce las entidades de dominio (Movie de RTDB) a los modelos de UI que las
 * pantallas ya saben renderizar (TodayUiState, FilmUiState, CatalogMovieUi).
 *
 * Centraliza el formato visual (subtítulo compuesto, etiqueta de fecha) para que
 * todas las pantallas lo muestren igual.
 */
object MovieUiMapper {

    /** Formato de cabecera "22 SEP 2026" a partir de una fecha ISO "yyyy-MM-dd". */
    private val headerDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)

    /**
     * "22 SEP 2026" (o la fecha cruda si no se puede parsear).
     *
     * IMPORTANTE: la fecha ya NO es intrínseca a la película (antes venía de
     * movie.releaseDate). Ahora representa el DÍA que se está mostrando (hoy en
     * Today, o el día concreto del mes en Catalog/Film), que es quien decide qué
     * película corresponde vía la dailyQueue del usuario.
     */
    fun dateLabel(date: String): String {
        val parsed = DateUtils.parse(date) ?: return date
        return headerDateFormat.format(parsed).uppercase(Locale.ENGLISH)
    }

    /**
     * Subtítulo compuesto a partir de los campos reales de la película. Omite los
     * vacíos: "Título original · Año · País · Director · Géneros".
     */
    fun subtitle(movie: Movie): String =
        listOf(
            movie.originalTitle,
            movie.year.takeIf { it > 0 }?.toString(),
            movie.country,
            movie.director,
            movie.genre
        )
            .filterNot { it.isNullOrBlank() }
            .joinToString(" · ")

    /**
     * Movie → TodayUiState. [date] es el día mostrado ("yyyy-MM-dd", normalmente
     * hoy); de él sale la etiqueta de fecha, no de la película.
     */
    fun toTodayState(
        movie: Movie,
        date: String,
        positionCounter: String = "",
        isWatched: Boolean = false,
        isSaved: Boolean = false
    ): TodayUiState = TodayUiState(
        dateText = dateLabel(date),
        positionCounter = positionCounter,
        movieTitle = movie.title,
        movieSubtitle = subtitle(movie),
        synopsis = movie.synopsis,
        isWatched = isWatched,
        isSaved = isSaved,
        posterUrl = movie.posterUrl.ifBlank { null }
    )

    /**
     * Movie → FilmUiState (pantalla de detalle). [date] es el día que representa
     * esa apertura de la película (el día del que proviene en el catálogo/queue).
     */
    fun toFilmState(
        movie: Movie,
        date: String,
        positionCounter: String = "",
        isWatched: Boolean = false,
        isSaved: Boolean = false
    ): FilmUiState = FilmUiState(
        dateText = dateLabel(date),
        positionCounter = positionCounter,
        movieTitle = movie.title,
        movieSubtitle = subtitle(movie),
        synopsis = movie.synopsis,
        isWatched = isWatched,
        isSaved = isSaved,
        posterUrl = movie.posterUrl.ifBlank { null }
    )

    /**
     * Movie → CatalogMovieUi (item del grid). [date] es el día del mes al que
     * corresponde esa película en la queue del usuario.
     */
    fun toCatalogItem(movie: Movie, date: String, watched: Boolean): CatalogMovieUi = CatalogMovieUi(
        id = movie.id,
        dateLabel = dateLabel(date),
        title = movie.title,
        watched = watched,
        posterUrl = movie.posterUrl.ifBlank { null }
    )
}
