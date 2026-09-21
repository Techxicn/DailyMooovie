package com.techxicn.dailymooovie.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.techxicn.dailymooovie.databinding.FragmentMyMoviesBinding
import com.techxicn.dailymooovie.model.MyMoviesUiState
import com.techxicn.dailymooovie.model.SampleData
import com.techxicn.dailymooovie.ui.adapter.CatalogMovieAdapter

/**
 * Tab WATCHLIST — películas guardadas por el usuario.
 *
 * Reutiliza el mismo item de grid que Film Catalog (item_catalog_movie) y su
 * adapter (CatalogMovieAdapter), ya que son visualmente idénticos.
 *
 * Conexión con backend:
 *   render(viewModel.watchlistState)   // MyMoviesUiState producido por la lógica
 *
 * Mientras no haya datos reales, render() usa el estado por defecto poblado con
 * SampleData.watchlistMovies.
 */
class MyMoviesFragment : Fragment() {

    private var _binding: FragmentMyMoviesBinding? = null
    private val binding get() = _binding!!

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
        // TODO: reemplazar por el estado del ViewModel cuando exista.
        render(MyMoviesUiState(movies = SampleData.watchlistMovies))
    }

    /** Punto único de entrada de datos a la pantalla. */
    fun render(state: MyMoviesUiState) {
        binding.bind(state)
        binding.rvWatchlist.adapter = CatalogMovieAdapter(state.movies) { /* movie ->
            // TODO: navigateTo(FilmFragment()) con la película seleccionada. */
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
