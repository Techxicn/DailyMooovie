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

    /** Formato de cabecera "22 SEP 2026" a partir de un releaseDate "yyyy-MM-dd". */
    private val headerDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)

    /** "22 SEP 2026" (o el releaseDate crudo si no se puede parsear). */
    fun dateLabel(releaseDate: String): String {
        val date = DateUtils.parse(releaseDate) ?: return releaseDate
        return headerDateFormat.format(date).uppercase(Locale.ENGLISH)
    }

    /** Subtítulo compuesto: "Título original · Año · País" (omite campos vacíos). */
    fun subtitle(movie: Movie): String =
        listOf(movie.originalTitle, movie.year.takeIf { it > 0 }?.toString(), movie.country)
            .filterNot { it.isNullOrBlank() }
            .joinToString(" · ")

    /** Movie → TodayUiState, preservando etiquetas de sección/contador dadas. */
    fun toTodayState(
        movie: Movie,
        positionCounter: String = "",
        isWatched: Boolean = false,
        isSaved: Boolean = false
    ): TodayUiState = TodayUiState(
        dateText = dateLabel(movie.releaseDate),
        positionCounter = positionCounter,
        movieTitle = movie.title,
        movieSubtitle = subtitle(movie),
        synopsis = movie.synopsis,
        isWatched = isWatched,
        isSaved = isSaved,
        posterUrl = movie.posterUrl.ifBlank { null }
    )

    /** Movie → FilmUiState (pantalla de detalle). */
    fun toFilmState(
        movie: Movie,
        positionCounter: String = "",
        isWatched: Boolean = false,
        isSaved: Boolean = false
    ): FilmUiState = FilmUiState(
        dateText = dateLabel(movie.releaseDate),
        positionCounter = positionCounter,
        movieTitle = movie.title,
        movieSubtitle = subtitle(movie),
        synopsis = movie.synopsis,
        isWatched = isWatched,
        isSaved = isSaved,
        posterUrl = movie.posterUrl.ifBlank { null }
    )

    /** Movie → CatalogMovieUi (item del grid), con el estado watched del usuario. */
    fun toCatalogItem(movie: Movie, watched: Boolean): CatalogMovieUi = CatalogMovieUi(
        id = movie.id,
        dateLabel = dateLabel(movie.releaseDate),
        title = movie.title,
        watched = watched,
        posterUrl = movie.posterUrl.ifBlank { null }
    )
}
