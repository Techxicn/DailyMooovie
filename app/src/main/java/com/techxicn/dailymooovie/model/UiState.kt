package com.techxicn.dailymooovie.model

/**
 * UiState.kt
 * ══════════
 * Modelos de estado de UI ("view state") para cada pantalla de DailyMooovie.
 *
 * Estos data classes son el CONTRATO entre la capa visual y el backend/ViewModel:
 * la lógica del proyecto produce una instancia de estos modelos y la pantalla
 * la renderiza vía las funciones bind() de ScreenBinders.kt.
 *
 * Los valores por defecto reproducen los datos de ejemplo del diseño, de modo que
 * si el backend aún no entrega datos, la pantalla muestra el placeholder original.
 * Cuando el backend esté listo, basta con construir el modelo con datos reales.
 *
 * NADA aquí depende del framework Android: son POJOs puros, testeables.
 */

// ─────────────────────────────────────────────────────────────────────────
//  MODELOS COMPARTIDOS
// ─────────────────────────────────────────────────────────────────────────

/** Estado de un día en el heatmap de Journey. */
enum class DayState {
    EMPTY,        // sin datos: pre-registro o día sin recomendación
    UNSEEN,       // recomendado pero no visto
    SEEN_LOW,     // visto (intensidad baja)
    SEEN_MID,     // visto (intensidad media)
    SEEN_HIGH,    // visto (intensidad alta)
    DISCOVERED    // descubierta / destacada
}

/** Película mínima para carruseles y grids (solo datos de presentación). */
data class MovieCardUi(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",      // director · año, o meta libre
    val posterUrl: String? = null,  // el binder decide cómo cargarla (Glide/Picasso)
    val rating: Int = 0,            // 0..5 estrellas
    val dateLabel: String = ""      // "31 AGO" para carrusel reciente
)

/** Estadística grande genérica (valor + etiqueta). */
data class StatUi(
    val value: String = "0",
    val label: String = ""
)

// ─────────────────────────────────────────────────────────────────────────
//  TODAY
// ─────────────────────────────────────────────────────────────────────────

data class TodayUiState(
    val dateText: String = "31 AGO 2026",
    val dayLabel: String = "PELÍCULA DEL DÍA",
    val dayCounter: Int = 243,
    val dayCounterTotal: Int = 365,
    val badgeDay: String = "31",
    val badgeMonth: String = "AGO",
    val movieTitle: String = "La Ilusión Perdida",
    val director: String = "Dirigida por Alejandro Fuentes",
    val metadata: String = "1974  ·  México  ·  Drama",
    val quote: String = "\"El amor no se pierde, simplemente\naprende a vivir en silencio.\"",
    val streamPrimaryLabel: String = "Ver en MUBI",
    val streamSecondaryLabel: String = "Disponible en Criterion Channel",
    val streakDays: Int = 23,
    val nextMovieText: String = "Mañana, 08:00",
    val isWatched: Boolean = true,
    val isSaved: Boolean = false,
    val posterUrl: String? = null
)

// ─────────────────────────────────────────────────────────────────────────
//  JOURNEY
// ─────────────────────────────────────────────────────────────────────────

data class JourneyUiState(
    val year: Int = 2026,
    val subtitle: String = "TU RECORRIDO",
    val title: String = "YOUR VIEWING JOURNEY",
    /** Estado por día del año, índice 0..364. Alinea con los tags "day_N" del heatmap. */
    val heatmap: List<DayState> = List(365) { DayState.EMPTY },
    val daysSeen: Int = 243,
    val daysTotal: Int = 365,
    val percent: Int = 67,
    val currentStreak: Int = 23,
    val bestStreak: Int = 41,
    val statDiscovered: Int = 243,
    val statWatched: Int = 183,
    val statSaved: Int = 64,
    val goalLabel: String = "Ver 200 películas",
    val goalCurrent: Int = 183,
    val goalTarget: Int = 200,
    val recentMovies: List<MovieCardUi> = emptyList(),
    val achievementTitle: String = "Cinéfilo de los 90s",
    // Filas de menú (valor a la derecha):
    val calendarValue: String = "Agosto 2026",
    val monthlySummaryValue: String = "Agosto",
    val yearInFilmValue: String = "2026",
    val achievementsValue: String = "4 obtenidos"
) {
    /** Progreso de la meta anual como fracción 0f..1f para la barra. */
    val goalProgress: Float
        get() = if (goalTarget == 0) 0f else (goalCurrent.toFloat() / goalTarget).coerceIn(0f, 1f)
}

// ─────────────────────────────────────────────────────────────────────────
//  MY MOVIES
// ─────────────────────────────────────────────────────────────────────────

enum class MoviesTab { WATCHED, WATCHLIST, FAVORITES }

data class MyMoviesUiState(
    val watchedCount: Int = 9,
    val watchlistCount: Int = 6,
    val favoritesCount: Int = 4,
    val activeTab: MoviesTab = MoviesTab.WATCHED,
    val movies: List<MovieCardUi> = emptyList()
) {
    val subtitle: String get() = "$watchedCount vistas · $watchlistCount guardadas"
}

// ─────────────────────────────────────────────────────────────────────────
//  PROFILE
// ─────────────────────────────────────────────────────────────────────────

data class ProfileUiState(
    val name: String = "ELENA",
    val avatarInitial: String = "E",
    val since: String = "Viendo desde 2026",
    val viewerType: String = "EL EXPLORADOR CURIOSO",
    val viewerDesc: String = "Te mueves entre géneros constantemente y rara vez te quedas en una década por mucho tiempo.",
    val statDiscovered: Int = 243,
    val statWatched: Int = 183,
    val statSaved: Int = 64,
    val genres: List<String> = listOf("DRAMA", "ACCIÓN", "CIENCIA FICCIÓN"),
    val avgRating: Int = 4,           // 0..5 estrellas
    val currentStreak: Int = 23,
    val favorites: List<MovieCardUi> = emptyList()
)

// ─────────────────────────────────────────────────────────────────────────
//  YEAR IN FILM
// ─────────────────────────────────────────────────────────────────────────

data class GenreCountUi(val name: String = "", val count: Int = 0)

data class YearInFilmUiState(
    val year: Int = 2026,
    val description: String = "183 películas. 6 géneros. 18 países.\nUna película al día.",
    val statDiscovered: Int = 243,
    val statWatched: Int = 183,
    val genres: List<GenreCountUi> = listOf(
        GenreCountUi("Drama", 44),
        GenreCountUi("Acción", 32),
        GenreCountUi("Comedia", 27),
        GenreCountUi("Ciencia ficción", 24),
        GenreCountUi("Suspenso", 19),
        GenreCountUi("Terror", 14)
    )
)

// ─────────────────────────────────────────────────────────────────────────
//  WORLD (mapa + lista)
// ─────────────────────────────────────────────────────────────────────────

data class CountryStatUi(
    val code: String = "",     // "USA"
    val name: String = "",     // "Estados Unidos"
    val count: Int = 0
)

data class WorldMapUiState(
    val countriesSeen: Int = 18,
    val moviesCount: Int = 172,
    val continents: Int = 5,
    val legendMin: Int = 1,
    val legendMax: Int = 42,
    val topCountry: CountryStatUi = CountryStatUi("USA", "Estados Unidos", 42),
    val countries: List<CountryStatUi> = emptyList()
)

// ─────────────────────────────────────────────────────────────────────────
//  MOVIE PASSPORT
// ─────────────────────────────────────────────────────────────────────────

data class PassportStampUi(
    val code: String = "",       // "USA"
    val city: String = "",       // "New York"
    val unlocked: Boolean = false
)

data class MoviePassportUiState(
    val stampedCount: Int = 5,
    val totalCountries: Int = 18,
    val stamps: List<PassportStampUi> = emptyList(),
    val sealedByFirst: CountryStatUi = CountryStatUi("USA", "Estados Unidos", 0),
    val sealedByFirstMovie: String = "Chinatown"
)

// ─────────────────────────────────────────────────────────────────────────
//  SAMPLE DATA — datos de ejemplo para poblar la UI sin backend.
//  Reemplazar por datos reales del ViewModel/Repository cuando existan.
// ─────────────────────────────────────────────────────────────────────────

object SampleData {

    val recentMovies = listOf(
        MovieCardUi(title = "Rashomon", dateLabel = "31 AGO", rating = 5),
        MovieCardUi(title = "Amélie", dateLabel = "30 AGO", rating = 4),
        MovieCardUi(title = "Ciudad de Dios", dateLabel = "29 AGO", rating = 5),
        MovieCardUi(title = "El Séptimo Sello", dateLabel = "28 AGO", rating = 4),
        MovieCardUi(title = "Oldboy", dateLabel = "27 AGO", rating = 5)
    )

    val gridMovies = listOf(
        MovieCardUi(title = "Chinatown", subtitle = "Roman Polanski", rating = 5),
        MovieCardUi(title = "Rashomon", subtitle = "Akira Kurosawa", rating = 5),
        MovieCardUi(title = "Amélie", subtitle = "Jean-Pierre Jeunet", rating = 4),
        MovieCardUi(title = "Ciudad de Dios", subtitle = "Fernando Meirelles", rating = 5),
        MovieCardUi(title = "Oldboy", subtitle = "Park Chan-wook", rating = 4),
        MovieCardUi(title = "El Séptimo Sello", subtitle = "Ingmar Bergman", rating = 4),
        MovieCardUi(title = "Metrópolis", subtitle = "Fritz Lang", rating = 3),
        MovieCardUi(title = "Tokio Story", subtitle = "Yasujirō Ozu", rating = 5),
        MovieCardUi(title = "Persona", subtitle = "Ingmar Bergman", rating = 4)
    )

    val favorites = listOf(
        MovieCardUi(title = "Rashomon"),
        MovieCardUi(title = "Chinatown"),
        MovieCardUi(title = "Amélie"),
        MovieCardUi(title = "Oldboy")
    )

    val stamps = listOf(
        PassportStampUi("USA", "New York", unlocked = true),
        PassportStampUi("GBR", "London", unlocked = true),
        PassportStampUi("BRA", "Rio", unlocked = true),
        PassportStampUi("JPN", "Tokyo", unlocked = true),
        PassportStampUi("FRA", "Paris", unlocked = true),
        PassportStampUi("", "", unlocked = false),
        PassportStampUi("", "", unlocked = false),
        PassportStampUi("", "", unlocked = false),
        PassportStampUi("", "", unlocked = false)
    )

    val countries = listOf(
        CountryStatUi("USA", "Estados Unidos", 42),
        CountryStatUi("GBR", "Reino Unido", 37),
        CountryStatUi("BRA", "Brasil", 18),
        CountryStatUi("JPN", "Japón", 15),
        CountryStatUi("FRA", "Francia", 12),
        CountryStatUi("NGA", "Nigeria", 9)
    )

    // Películas que dieron el sello (Passport Detail). dateLabel = código país.
    val sealedMovies = listOf(
        MovieCardUi(title = "Rashomon", subtitle = "Akira Kurosawa · 1950", dateLabel = "JPN"),
        MovieCardUi(title = "Tokio Story", subtitle = "Yasujirō Ozu · 1953", dateLabel = "JPN"),
        MovieCardUi(title = "Ran", subtitle = "Akira Kurosawa · 1985", dateLabel = "JPN"),
        MovieCardUi(title = "Perfect Days", subtitle = "Wim Wenders · 2023", dateLabel = "JPN")
    )

    /** Heatmap de ejemplo: patrón determinístico para demostrar los estados. */
    fun sampleHeatmap(): List<DayState> = List(365) { day ->
        when {
            day >= 243 -> DayState.EMPTY          // futuro / sin datos
            day % 9 == 0 -> DayState.DISCOVERED
            day % 9 in listOf(1, 2, 5, 7) -> DayState.SEEN_HIGH
            day % 9 in listOf(3, 6) -> DayState.SEEN_MID
            day % 9 == 4 -> DayState.SEEN_LOW
            else -> DayState.UNSEEN
        }
    }
}
