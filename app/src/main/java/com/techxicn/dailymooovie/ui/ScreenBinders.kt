package com.techxicn.dailymooovie.ui

import android.view.View
import androidx.core.content.ContextCompat
import com.techxicn.dailymooovie.R
import com.techxicn.dailymooovie.databinding.FragmentJourneyBinding
import com.techxicn.dailymooovie.databinding.FragmentMyMoviesBinding
import com.techxicn.dailymooovie.databinding.FragmentProfileBinding
import com.techxicn.dailymooovie.databinding.FragmentTodayBinding
import com.techxicn.dailymooovie.model.DayState
import com.techxicn.dailymooovie.model.JourneyUiState
import com.techxicn.dailymooovie.model.MoviesTab
import com.techxicn.dailymooovie.model.MyMoviesUiState
import com.techxicn.dailymooovie.model.ProfileUiState
import com.techxicn.dailymooovie.model.TodayUiState

/**
 * ScreenBinders.kt
 * ════════════════
 * Funciones puente entre los modelos UiState y las vistas (View Binding).
 *
 * Cada bind() recibe el binding generado + el estado, y asigna cada dato a su
 * vista. Es el único punto donde el dato "aterriza" en la UI. El backend/ViewModel
 * solo tiene que:
 *
 *     binding.bind(TodayUiState(movieTitle = ..., streakDays = ..., ...))
 */

// ─────────────────────────────────────────────────────────────────────────
//  TODAY
// ─────────────────────────────────────────────────────────────────────────

fun FragmentTodayBinding.bind(state: TodayUiState) {
    tvDate.text = state.dateText
    tvDayLabel.text = state.dayLabel
    tvDayCounter.text = state.dayCounter.toString()
    tvDayCounterTotal.text = "/ ${state.dayCounterTotal}"
    tvBadgeDay.text = state.badgeDay
    tvBadgeMonth.text = state.badgeMonth
    tvMovieTitle.text = state.movieTitle
    tvDirector.text = state.director
    tvMetadata.text = state.metadata
    tvQuote.text = state.quote
    tvStreamPrimary.text = state.streamPrimaryLabel
    tvStreamSecondary.text = state.streamSecondaryLabel
    tvStreak.text = "${state.streakDays} días de racha"
    tvNextMovie.text = state.nextMovieText
    // state.posterUrl / isWatched / isSaved → cargar imagen y marcar chips con la
    // librería de imágenes del proyecto (Glide/Picasso) cuando esté integrada.
}

// ─────────────────────────────────────────────────────────────────────────
//  JOURNEY
// ─────────────────────────────────────────────────────────────────────────

fun FragmentJourneyBinding.bind(state: JourneyUiState) {
    tvJourneySubtitle.text = state.subtitle
    tvJourneyTitle.text = state.title
    tvJourneyYear.text = state.year.toString()

    tvHeatmapSummary.text = "${state.daysSeen} de ${state.daysTotal} días · ${state.percent}%"
    tvStreakJourney.text = "${state.currentStreak} días de racha"
    tvBestStreak.text = "MEJOR: ${state.bestStreak}"

    tvStatDiscovered.text = state.statDiscovered.toString()
    tvStatWatched.text = state.statWatched.toString()
    tvStatSaved.text = state.statSaved.toString()

    tvGoalLabel.text = state.goalLabel
    tvGoalProgress.text = "${state.goalCurrent} / ${state.goalTarget}"
    // Barra de progreso: scaleX = fracción, pivote a la izquierda.
    viewProgressFill.scaleX = state.goalProgress

    tvAchievementTitle.text = state.achievementTitle

    // Heatmap: colorea cada celda por su tag "day_N" según el estado.
    applyHeatmap(state.heatmap)
}

/**
 * Aplica el estado de cada día a su celda del heatmap, localizando la vista por
 * su tag "day_N". Seguro ante listas más cortas/largas que 365.
 */
fun FragmentJourneyBinding.applyHeatmap(states: List<DayState>) {
    val ctx = heatmapContainer.context
    for (day in states.indices) {
        val cell = heatmapContainer.findViewWithTag<View>("day_$day") ?: continue
        val colorRes = when (states[day]) {
            DayState.SEEN_HIGH -> R.color.heatmapSeenHigh
            DayState.SEEN_MID -> R.color.heatmapSeenMid
            DayState.SEEN_LOW -> R.color.heatmapSeenLow
            DayState.DISCOVERED -> R.color.heatmapDiscovered
            DayState.UNSEEN -> R.color.heatmapUnseen
            DayState.EMPTY -> R.color.heatmapEmpty
        }
        cell.setBackgroundColor(ContextCompat.getColor(ctx, colorRes))
    }
}

// ─────────────────────────────────────────────────────────────────────────
//  MY MOVIES
// ─────────────────────────────────────────────────────────────────────────

fun FragmentMyMoviesBinding.bind(state: MyMoviesUiState) {
    tvMyMoviesSubtitle.text = state.subtitle
    tabWatched.text = "VISTAS · ${state.watchedCount}"
    tabWatchlist.text = "QUIERO VER · ${state.watchlistCount}"
    tabFavorites.text = "FAVORITAS · ${state.favoritesCount}"

    // Resalta el tab activo (fondo + color de texto).
    val ctx = root.context
    fun style(tab: android.widget.TextView, active: Boolean) {
        tab.setBackgroundResource(
            if (active) R.drawable.shape_tab_active else R.drawable.shape_tab_inactive
        )
        tab.setTextColor(
            ContextCompat.getColor(
                ctx,
                if (active) R.color.colorAccentRed else R.color.colorOnSurfaceMuted
            )
        )
    }
    style(tabWatched, state.activeTab == MoviesTab.WATCHED)
    style(tabWatchlist, state.activeTab == MoviesTab.WATCHLIST)
    style(tabFavorites, state.activeTab == MoviesTab.FAVORITES)
    // state.movies → asignar al adapter del RecyclerView rvMoviesGrid.
}

// ─────────────────────────────────────────────────────────────────────────
//  PROFILE
// ─────────────────────────────────────────────────────────────────────────

fun FragmentProfileBinding.bind(state: ProfileUiState) {
    tvProfileName.text = state.name
    tvAvatarInitial.text = state.avatarInitial
    tvProfileSince.text = state.since
    tvViewerType.text = state.viewerType
    tvViewerDesc.text = state.viewerDesc
    tvStatDiscoveredProfile.text = state.statDiscovered.toString()
    tvStatWatchedProfile.text = state.statWatched.toString()
    tvStatSavedProfile.text = state.statSaved.toString()
    tvCurrentStreak.text = "${state.currentStreak} días"

    // Calificación promedio: atenúa las estrellas por encima de avgRating.
    val stars = listOf(
        rowAvgStars.getChildAt(0),
        rowAvgStars.getChildAt(1),
        rowAvgStars.getChildAt(2),
        rowAvgStars.getChildAt(3),
        rowAvgStars.getChildAt(4)
    )
    stars.forEachIndexed { index, star ->
        star?.alpha = if (index < state.avgRating) 1f else 0.25f
    }
    // state.genres → chips dinámicos; state.favorites → adapter de rvFavorites.
}
