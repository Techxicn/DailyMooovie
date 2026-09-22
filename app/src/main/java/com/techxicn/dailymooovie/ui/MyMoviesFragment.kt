package com.techxicn.dailymooovie.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.techxicn.dailymooovie.R
import com.techxicn.dailymooovie.data.MovieRepository
import com.techxicn.dailymooovie.data.MovieUiMapper
import com.techxicn.dailymooovie.data.UserMovieRepository
import com.techxicn.dailymooovie.databinding.FragmentMyMoviesBinding
import com.techxicn.dailymooovie.model.CatalogMovieUi
import com.techxicn.dailymooovie.model.MyMoviesUiState
import com.techxicn.dailymooovie.ui.adapter.CatalogMovieAdapter
import kotlinx.coroutines.launch

/**
 * Tab WATCHLIST — películas guardadas por el usuario.
 *
 * Lee los ids con status "watchlist" (/users/{uid}/movies) y resuelve cada uno a
 * su Movie completa (/movies) para poblar el grid. Reutiliza el item y adapter
 * del Film Catalog. Las películas en watchlist se muestran como "Not Watched".
 */
class MyMoviesFragment : Fragment() {

    private var _binding: FragmentMyMoviesBinding? = null
    private val binding get() = _binding!!

    private val movieRepo = MovieRepository()
    private val userRepo = UserMovieRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyMoviesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvWatchlist.layoutManager = GridLayoutManager(requireContext(), 3)
        loadWatchlist()
    }

    /** Carga los ids en watchlist y resuelve sus películas para el grid. */
    private fun loadWatchlist() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val ids = userRepo.getWatchlistMovieIds()
                // Resuelve cada id a su Movie; ignora ids huérfanos (película borrada).
                val movies = ids.mapNotNull { movieRepo.getMovieById(it) }
                // En watchlist se muestran como no vistas (watched = false).
                val items = movies.map { MovieUiMapper.toCatalogItem(it, watched = false) }

                val count = items.size
                render(
                    MyMoviesUiState(
                        countLabel = if (count == 0) {
                            getString(R.string.watchlist_empty)
                        } else {
                            getString(R.string.watchlist_count_fmt, count)
                        },
                        positionCounter = "%02d/%02d".format(if (count == 0) 0 else 1, count),
                        movies = items
                    )
                )
            } catch (e: Exception) {
                if (isAdded) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.data_error_generic),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    /** Punto único de entrada de datos a la pantalla. */
    fun render(state: MyMoviesUiState) {
        binding.bind(state)
        binding.rvWatchlist.adapter = CatalogMovieAdapter(
            items = state.movies,
            onItemClick = { movie -> navigateTo(FilmFragment.newInstance(movie.id)) },
            onItemLongClick = { movie -> confirmRemove(movie) }
        )
    }

    /**
     * Long-press sobre un item → confirma y quita la película de la watchlist.
     * Se usa un diálogo de confirmación para evitar borrados accidentales.
     */
    private fun confirmRemove(movie: CatalogMovieUi) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.watchlist_remove_title)
            .setMessage(getString(R.string.watchlist_remove_message, movie.title))
            .setPositiveButton(R.string.watchlist_remove_confirm) { _, _ -> removeFromWatchlist(movie) }
            .setNegativeButton(R.string.watchlist_remove_cancel, null)
            .show()
    }

    /** Quita [movie] de /users/{uid}/movies y refresca el grid. */
    private fun removeFromWatchlist(movie: CatalogMovieUi) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                userRepo.removeMovie(movie.id)
                if (isAdded) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.watchlist_removed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                // Recarga para que la película desaparezca de inmediato.
                loadWatchlist()
            } catch (e: Exception) {
                if (isAdded) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.data_error_generic),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
