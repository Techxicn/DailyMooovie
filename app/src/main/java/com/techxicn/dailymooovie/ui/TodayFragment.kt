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
import com.techxicn.dailymooovie.data.DailyQueueRepository
import com.techxicn.dailymooovie.data.DateUtils
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
 * La "película del día" ya NO depende de un releaseDate compartido: se resuelve
 * contra la dailyQueue del usuario (única y aleatoria por usuario) tomando la
 * fecha de hoy vía [DailyQueueRepository.resolveMovieIdForDate]. Refleja el
 * estado del usuario (watched / en watchlist) y los botones escriben en
 * /users/{uid}/movies/{movieId} vía UserMovieRepository.
 */
class TodayFragment : Fragment() {

    private var _binding: FragmentTodayBinding? = null
    private val binding get() = _binding!!

    private val movieRepo = MovieRepository()
    private val userRepo = UserMovieRepository()
    private val queueRepo = DailyQueueRepository()

    /** Fecha (hoy) que se está mostrando; se usa para la etiqueta de fecha. */
    private val today = DateUtils.today()

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
        binding.movieContent.btnWatchTrailer.setOnClickListener {
            com.techxicn.dailymooovie.util.TrailerLauncher.open(requireContext(), currentMovie?.trailerUrl)
        }

        loadMovieOfTheDay()
    }

    /** Resuelve la película de hoy vía la dailyQueue del usuario y renderiza. */
    private fun loadMovieOfTheDay() {
        showLoading()
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // El id del día sale de la queue del usuario para la fecha de hoy.
                val movieId = queueRepo.resolveMovieIdForDate(today)
                val movie = movieId?.let { movieRepo.getMovieById(it) }
                currentMovie = movie
                if (movie == null) {
                    // Sin cola generada aún o catálogo vacío: se mantiene el placeholder.
                    render(TodayUiState())
                    showContent()
                    toast(R.string.today_no_movie)
                    return@launch
                }
                val status = userRepo.getStatus(movie.id)
                isWatched = status?.status == MovieStatus.WATCHED
                isSaved = status?.status == MovieStatus.WATCHLIST
                render(MovieUiMapper.toTodayState(movie, today, isWatched = isWatched, isSaved = isSaved))
                showContent()
            } catch (e: Exception) {
                showError()
            }
        }
    }

    /** Muestra el loader y oculta contenido/error. */
    private fun showLoading() {
        binding.progressLoading.visibility = View.VISIBLE
        binding.contentScroll.visibility = View.GONE
        binding.tvError.visibility = View.GONE
    }

    /** Muestra el contenido y oculta loader/error. */
    private fun showContent() {
        binding.progressLoading.visibility = View.GONE
        binding.tvError.visibility = View.GONE
        binding.contentScroll.visibility = View.VISIBLE
    }

    /** Muestra el estado de error y oculta loader/contenido. */
    private fun showError() {
        binding.progressLoading.visibility = View.GONE
        binding.contentScroll.visibility = View.GONE
        binding.tvError.visibility = View.VISIBLE
    }

    /** Marca/actualiza la película del día como vista. */
    private fun onWatchedClicked() {
        val movie = currentMovie ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                userRepo.markAsWatched(movie.id)
                isWatched = true
                isSaved = false
                render(MovieUiMapper.toTodayState(movie, today, isWatched = true, isSaved = false))
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
                render(MovieUiMapper.toTodayState(movie, today, isWatched = false, isSaved = true))
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
