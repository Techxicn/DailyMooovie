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
import com.techxicn.dailymooovie.data.Movie
import com.techxicn.dailymooovie.data.MovieRepository
import com.techxicn.dailymooovie.data.MovieStatus
import com.techxicn.dailymooovie.data.MovieUiMapper
import com.techxicn.dailymooovie.data.UserMovieRepository
import com.techxicn.dailymooovie.databinding.FragmentTodayBinding
import com.techxicn.dailymooovie.model.TodayUiState
import kotlinx.coroutines.launch

/**
 * Tab TODAY — película del día.
 *
 * Carga desde Realtime Database la película cuyo releaseDate es hoy y refleja el
 * estado del usuario (watched / en watchlist). Los botones Watched y Watchlist
 * escriben en /users/{uid}/movies/{movieId} vía UserMovieRepository.
 */
class TodayFragment : Fragment() {

    private var _binding: FragmentTodayBinding? = null
    private val binding get() = _binding!!

    private val movieRepo = MovieRepository()
    private val userRepo = UserMovieRepository()

    /** Película del día actualmente mostrada (null mientras carga o si no hay). */
    private var currentMovie: Movie? = null
    private var isWatched = false
    private var isSaved = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTodayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.movieContent.btnWatched.setOnClickListener { onWatchedClicked() }
        binding.movieContent.btnWatchlist.setOnClickListener { onWatchlistClicked() }

        loadMovieOfTheDay()
    }

    /** Carga la película del día y su estado en el usuario, luego renderiza. */
    private fun loadMovieOfTheDay() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val movie = movieRepo.getMovieOfTheDay()
                currentMovie = movie
                if (movie == null) {
                    // Sin película programada para hoy: se mantiene el placeholder.
                    render(TodayUiState())
                    toast(R.string.today_no_movie)
                    return@launch
                }
                val status = userRepo.getStatus(movie.id)
                isWatched = status?.status == MovieStatus.WATCHED
                isSaved = status?.status == MovieStatus.WATCHLIST
                render(MovieUiMapper.toTodayState(movie, isWatched = isWatched, isSaved = isSaved))
            } catch (e: Exception) {
                toast(R.string.data_error_generic)
            }
        }
    }

    /** Marca/actualiza la película del día como vista. */
    private fun onWatchedClicked() {
        val movie = currentMovie ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                userRepo.markAsWatched(movie.id)
                isWatched = true
                isSaved = false
                render(MovieUiMapper.toTodayState(movie, isWatched = true, isSaved = false))
                toast(R.string.today_marked_watched)
            } catch (e: Exception) {
                toast(R.string.data_error_generic)
            }
        }
    }

    /** Agrega/actualiza la película del día en la watchlist. */
    private fun onWatchlistClicked() {
        val movie = currentMovie ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                userRepo.addToWatchlist(movie.id)
                isSaved = true
                isWatched = false
                render(MovieUiMapper.toTodayState(movie, isWatched = false, isSaved = true))
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
    fun render(state: TodayUiState) {
        binding.bind(state)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
