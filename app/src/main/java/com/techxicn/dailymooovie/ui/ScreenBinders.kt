package com.techxicn.dailymooovie.ui

import com.techxicn.dailymooovie.databinding.FragmentMyMoviesBinding
import com.techxicn.dailymooovie.databinding.FragmentProfileBinding
import com.techxicn.dailymooovie.databinding.FragmentTodayBinding
import com.techxicn.dailymooovie.databinding.FragmentFilmBinding
import com.techxicn.dailymooovie.model.MyMoviesUiState
import com.techxicn.dailymooovie.model.ProfileUiState
import com.techxicn.dailymooovie.model.TodayUiState
import com.techxicn.dailymooovie.model.FilmUiState
import com.techxicn.dailymooovie.util.ImageLoader

/**
 * ScreenBinders.kt
 * ════════════════
 * Funciones puente entre los modelos UiState y las vistas (View Binding).
 *
 * Cada bind() recibe el binding generado + el estado, y asigna cada dato a su
 * vista. Es el único punto donde el dato "aterriza" en la UI. El backend/ViewModel
 * solo tiene que:
 *
 *     binding.bind(TodayUiState(movieTitle = ..., ...))
 */

// ─────────────────────────────────────────────────────────────────────────
//  TODAY
// ─────────────────────────────────────────────────────────────────────────

fun FragmentTodayBinding.bind(state: TodayUiState) {
    tvDate.text = state.dateText
    tvPositionCounter.text = state.positionCounter
    tvSectionLabel.text = state.sectionLabel
    // El contenido de película vive en el include compartido (movieContent).
    movieContent.tvMovieTitle.text = state.movieTitle
    movieContent.tvMovieSubtitle.text = state.movieSubtitle
    movieContent.tvSynopsis.text = state.synopsis
    ImageLoader.loadPoster(movieContent.ivPoster, state.posterUrl)
    // isWatched / isSaved → (estado de botones se gestiona en el fragment).
}

// ─────────────────────────────────────────────────────────────────────────
//  FILM (detalle) — mismo contenido de película, header propio de la película
// ─────────────────────────────────────────────────────────────────────────

fun FragmentFilmBinding.bind(state: FilmUiState) {
    tvDate.text = state.dateText
    tvPositionCounter.text = state.positionCounter
    tvSectionLabel.text = state.sectionLabel
    movieContent.tvMovieTitle.text = state.movieTitle
    movieContent.tvMovieSubtitle.text = state.movieSubtitle
    movieContent.tvSynopsis.text = state.synopsis
    ImageLoader.loadPoster(movieContent.ivPoster, state.posterUrl)
    // isWatched / isSaved → idéntico manejo que Today (botones en el fragment).
}

// ─────────────────────────────────────────────────────────────────────────
//  WATCHLIST
// ─────────────────────────────────────────────────────────────────────────

fun FragmentMyMoviesBinding.bind(state: MyMoviesUiState) {
    tvWatchlistCount.text = state.countLabel
    tvPositionCounter.text = state.positionCounter
    tvSectionLabel.text = state.sectionLabel
    // state.movies → se asigna al adapter del RecyclerView rvWatchlist en el Fragment.
}

// ─────────────────────────────────────────────────────────────────────────
//  PROFILE
// ─────────────────────────────────────────────────────────────────────────

fun FragmentProfileBinding.bind(state: ProfileUiState) {
    tvSectionLabel.text = state.sectionLabel
    tvProfileName.text = state.name
    tvAvatarInitial.text = state.avatarInitial
    tvProfileRole.text = state.role
    tvProfileWatchedCount.text = state.watchedCountLabel
    tvStatYear.text = state.statYear.toString()
    tvStatCurrentStreak.text = state.statCurrentStreak.toString()
    tvStatLongestStreak.text = state.statLongestStreak.toString()
    tvDiscoveriesTitle.text = state.discoveriesTitle
    // state.discoveries → se asigna al adapter del RecyclerView rvDiscoveries en el Fragment.
}
