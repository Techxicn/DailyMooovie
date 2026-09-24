package com.techxicn.dailymooovie.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.techxicn.dailymooovie.R
import com.techxicn.dailymooovie.data.DateUtils
import com.techxicn.dailymooovie.data.Movie
import com.techxicn.dailymooovie.data.MovieRepository
import com.techxicn.dailymooovie.data.MovieStatus
import com.techxicn.dailymooovie.data.MovieUiMapper
import com.techxicn.dailymooovie.data.UserMovieRepository
import com.techxicn.dailymooovie.databinding.FragmentFilmBinding
import com.techxicn.dailymooovie.model.FilmUiState
import kotlinx.coroutines.launch

/**
 * Pantalla FILM — detalle de una película.
 *
 * Reutiliza el mismo contenido visual que TODAY (view_movie_content), con un
 * header propio de la película abierta y flecha "atrás".
 *
 * Recibe el id de la película por arguments (ver [newInstance]) y carga sus datos
 * reales desde /movies (MovieRepository) + el estado del usuario (/users/{uid}),
 * igual que TodayFragment. Los botones Watched/Watchlist escriben en RTDB.
 *
 * La película ya NO tiene una fecha intrínseca: la etiqueta de fecha corresponde
 * al DÍA que representa esta apertura. Ese día llega por arguments (p. ej. desde
 * el catálogo, el día concreto del mes). Si no se pasa una fecha (p. ej. desde la
 * watchlist), se usa la fecha de hoy como fallback razonable.
 */
class FilmFragment : Fragment() {

    private var _binding: FragmentFilmBinding? = null
    private val binding get() = _binding!!

    private val movieRepo = MovieRepository()
    private val userRepo = UserMovieRepository()

    private var currentMovie: Movie? = null
    private var isWatched = false
    private var isSaved = false

    /** Id recibido por arguments (vacío = sin id → placeholder). */
    private val movieId: String get() = arguments?.getString(ARG_MOVIE_ID).orEmpty()

    /** Día que representa esta apertura ("yyyy-MM-dd"); si falta, hoy. */
    private val date: String
        get() = arguments?.getString(ARG_DATE)?.ifBlank { null } ?: DateUtils.today()

    companion object {
        private const val ARG_MOVIE_ID = "arg_movie_id"
        private const val ARG_DATE = "arg_date"

        /**
         * Crea un FilmFragment que mostrará la película [movieId] etiquetada con el
         * día [date] ("yyyy-MM-dd") que representa. Si [date] es null se usará hoy.
         */
        fun newInstance(movieId: String, date: String? = null): FilmFragment = FilmFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_MOVIE_ID, movieId)
                putString(ARG_DATE, date)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Flecha atrás → vuelve al fragment anterior del back stack.
        binding.btnBack.setOnClickListener { navigateBack() }

        binding.movieContent.btnWatched.setOnClickListener { onWatchedClicked() }
        binding.movieContent.btnWatchlist.setOnClickListener { onWatchlistClicked() }
        binding.movieContent.btnWatchTrailer.setOnClickListener {
            com.techxicn.dailymooovie.util.TrailerLauncher.open(requireContext(), currentMovie?.trailerUrl)
        }

        loadMovie()
    }

    /** Carga la película por id y su estado en el usuario, luego renderiza. */
    private fun loadMovie() {
        val id = movieId
        if (id.isBlank()) {
            render(FilmUiState())
            return
        }
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val movie = movieRepo.getMovieById(id)
                currentMovie = movie
                if (movie == null) {
                    render(FilmUiState())
                    return@launch
                }
                val status = userRepo.getStatus(movie.id)
                isWatched = status?.status == MovieStatus.WATCHED
                isSaved = status?.status == MovieStatus.WATCHLIST
                render(MovieUiMapper.toFilmState(movie, date, isWatched = isWatched, isSaved = isSaved))
            } catch (e: Exception) {
                toast(R.string.data_error_generic)
            }
        }
    }

    private fun onWatchedClicked() {
        val movie = currentMovie ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                userRepo.markAsWatched(movie.id)
                isWatched = true
                isSaved = false
                render(MovieUiMapper.toFilmState(movie, date, isWatched = true, isSaved = false))
                toast(R.string.today_marked_watched)
            } catch (e: Exception) {
                toast(R.string.data_error_generic)
            }
        }
    }

    private fun onWatchlistClicked() {
        val movie = currentMovie ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                userRepo.addToWatchlist(movie.id)
                isSaved = true
                isWatched = false
                render(MovieUiMapper.toFilmState(movie, date, isWatched = false, isSaved = true))
                toast(R.string.today_added_watchlist)
            } catch (e: Exception) {
                toast(R.string.data_error_generic)
            }
        }
    }

    private fun toast(@StringRes msgRes: Int) {
        if (isAdded) Toast.makeText(requireContext(), getString(msgRes), Toast.LENGTH_SHORT).show()
    }

    /** Punto único de entrada de datos a la pantalla. */
    fun render(state: FilmUiState) {
        binding.bind(state)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
