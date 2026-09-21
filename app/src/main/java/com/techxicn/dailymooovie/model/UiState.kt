package com.techxicn.dailymooovie.model

/**
 * UiState.kt
 * ══════════
 * Modelos de estado de UI ("view state") para cada pantalla de MOOOVIE.
 *
 * Estos data classes son el CONTRATO entre la capa visual y el backend/ViewModel:
 * la lógica del proyecto produce una instancia de estos modelos y la pantalla
 * la renderiza vía las funciones bind() de ScreenBinders.kt.
 *
 * Los valores por defecto reproducen los datos de ejemplo del diseño, de modo que
 * si el backend aún no entrega datos, la pantalla muestra el placeholder original.
 *
 * NADA aquí depende del framework Android: son POJOs puros, testeables.
 */

// ─────────────────────────────────────────────────────────────────────────
//  MODELOS COMPARTIDOS
// ─────────────────────────────────────────────────────────────────────────

/** Película mínima para carruseles (poster + título). */
data class MovieCardUi(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",      // meta libre (director · año, etc.)
    val posterUrl: String? = null
)

// ─────────────────────────────────────────────────────────────────────────
//  TODAY
// ─────────────────────────────────────────────────────────────────────────

data class TodayUiState(
    val dateText: String = "22 SEP 2026",
    val positionCounter: String = "265/365",
    val sectionLabel: String = "Mooovie of the day!",
    val movieTitle: String = "El Jardín de las Sombras",
    // Subtítulo compuesto: título original · año · país
    val movieSubtitle: String = "The Garden of Shadows · 1988 · Argentina",
    val synopsis: String = "En un pueblo olvidado por el tiempo, una joven jardinera descubre que las flores de su invernadero guardan los recuerdos de quienes ya no están. Mientras el otoño avanza, deberá decidir entre conservar el pasado o dejar que el jardín florezca de nuevo. Una fábula melancólica sobre la memoria, la pérdida y la esperanza que renace con cada estación.",
    val isWatched: Boolean = false,
    val isSaved: Boolean = false,
    val posterUrl: String? = null
)

// ─────────────────────────────────────────────────────────────────────────
//  FILM (detalle de película)
//  Mismo contenido de película que Today, pero con header propio de la
//  película abierta (fecha + contador de ESA película) y label "of that day".
// ─────────────────────────────────────────────────────────────────────────

data class FilmUiState(
    val dateText: String = "14 MAR 1988",
    val positionCounter: String = "073/365",
    val sectionLabel: String = "Mooovie of that day!",
    val movieTitle: String = "El Jardín de las Sombras",
    val movieSubtitle: String = "The Garden of Shadows · 1988 · Argentina",
    val synopsis: String = "En un pueblo olvidado por el tiempo, una joven jardinera descubre que las flores de su invernadero guardan los recuerdos de quienes ya no están. Mientras el otoño avanza, deberá decidir entre conservar el pasado o dejar que el jardín florezca de nuevo. Una fábula melancólica sobre la memoria, la pérdida y la esperanza que renace con cada estación.",
    val isWatched: Boolean = false,
    val isSaved: Boolean = false,
    val posterUrl: String? = null
)

// ─────────────────────────────────────────────────────────────────────────
//  FILM CATALOG
//  Grid mensual de películas recomendadas con su estado (vista / no vista).
// ─────────────────────────────────────────────────────────────────────────

/** Película en el grid del catálogo: fecha, poster, título y estado visto. */
data class CatalogMovieUi(
    val id: String = "",
    val dateLabel: String = "",     // "22 SEP 2026"
    val title: String = "",
    val watched: Boolean = false,
    val posterUrl: String? = null
)

data class CatalogUiState(
    val sectionLabel: String = "Mooovies of the years!",
    val positionCounter: String = "05/30",
    val monthLabel: String = "SEPTEMBER 2026",
    val movies: List<CatalogMovieUi> = emptyList()
)

// ─────────────────────────────────────────────────────────────────────────
//  WATCHLIST  (tab de capas/stack → MyMoviesFragment)
//  Reutiliza CatalogMovieUi como item del grid (mismo item que Film Catalog).
// ─────────────────────────────────────────────────────────────────────────

data class MyMoviesUiState(
    val countLabel: String = "12 Moovies in your Watchlist",
    val positionCounter: String = "01/12",
    val sectionLabel: String = "Your Watchlist",
    val movies: List<CatalogMovieUi> = emptyList()
)

// ─────────────────────────────────────────────────────────────────────────
//  PROFILE
// ─────────────────────────────────────────────────────────────────────────

data class ProfileUiState(
    val sectionLabel: String = "Your Profile",
    val name: String = "Juan Antonio",
    val avatarInitial: String = "J",
    val role: String = "Movie Critic",
    val watchedCountLabel: String = "230 Moovies watched",
    val statYear: Int = 189,
    val statCurrentStreak: Int = 5,
    val statLongestStreak: Int = 40,
    val discoveriesTitle: String = "Juan Antonio's Mooovie Discoveries",
    val discoveries: List<MovieCardUi> = emptyList()
)

// ─────────────────────────────────────────────────────────────────────────
//  SAMPLE DATA — datos de ejemplo para poblar la UI sin backend.
//  Reemplazar por datos reales del ViewModel/Repository cuando existan.
// ─────────────────────────────────────────────────────────────────────────

object SampleData {

    // Catálogo mensual (Film Catalog) — datos de ejemplo de un mes.
    val catalogMovies = listOf(
        CatalogMovieUi(dateLabel = "01 SEP 2026", title = "The Girl with the Silver Lantern", watched = true),
        CatalogMovieUi(dateLabel = "04 SEP 2026", title = "Midnight in Havana", watched = true),
        CatalogMovieUi(dateLabel = "07 SEP 2026", title = "The Last Cartographer", watched = false),
        CatalogMovieUi(dateLabel = "10 SEP 2026", title = "Echoes of the North", watched = true),
        CatalogMovieUi(dateLabel = "13 SEP 2026", title = "A Quiet Rebellion", watched = false),
        CatalogMovieUi(dateLabel = "16 SEP 2026", title = "The Weight of Water", watched = false),
        CatalogMovieUi(dateLabel = "19 SEP 2026", title = "Paper Moons", watched = true),
        CatalogMovieUi(dateLabel = "22 SEP 2026", title = "El Jardín de las Sombras", watched = false),
        CatalogMovieUi(dateLabel = "25 SEP 2026", title = "The Salt of Distant Seas", watched = false),
        CatalogMovieUi(dateLabel = "28 SEP 2026", title = "Winter's Ledger", watched = true),
        CatalogMovieUi(dateLabel = "30 SEP 2026", title = "The Hollow Crown of Dawn", watched = false),
        CatalogMovieUi(dateLabel = "02 SEP 2026", title = "Letters to No One", watched = true)
    )

    // Watchlist del usuario — datos de ejemplo (mismo item que Film Catalog).
    val watchlistMovies = listOf(
        CatalogMovieUi(dateLabel = "22 SEP 2026", title = "El Jardín de las Sombras", watched = false),
        CatalogMovieUi(dateLabel = "18 SEP 2026", title = "The Weight of Water", watched = false),
        CatalogMovieUi(dateLabel = "11 SEP 2026", title = "A Quiet Rebellion", watched = false),
        CatalogMovieUi(dateLabel = "03 SEP 2026", title = "The Last Cartographer", watched = false),
        CatalogMovieUi(dateLabel = "27 AGO 2026", title = "Chinatown", watched = true),
        CatalogMovieUi(dateLabel = "21 AGO 2026", title = "Rashomon", watched = true),
        CatalogMovieUi(dateLabel = "14 AGO 2026", title = "The Salt of Distant Seas", watched = false),
        CatalogMovieUi(dateLabel = "09 AGO 2026", title = "Paper Moons", watched = true),
        CatalogMovieUi(dateLabel = "02 AGO 2026", title = "Winter's Ledger", watched = false),
        CatalogMovieUi(dateLabel = "28 JUL 2026", title = "Echoes of the North", watched = true),
        CatalogMovieUi(dateLabel = "19 JUL 2026", title = "The Hollow Crown of Dawn", watched = false),
        CatalogMovieUi(dateLabel = "12 JUL 2026", title = "Midnight in Havana", watched = true)
    )

    // Descubrimientos del usuario (Profile) — datos de ejemplo.
    val discoveries = listOf(
        MovieCardUi(title = "El Jardín de las Sombras"),
        MovieCardUi(title = "The Last Cartographer"),
        MovieCardUi(title = "Midnight in Havana")
    )
}
