package com.techxicn.dailymooovie.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.techxicn.dailymooovie.databinding.FragmentFilmBinding
import com.techxicn.dailymooovie.model.FilmUiState

/**
 * Pantalla FILM — detalle de una película.
 *
 * Reutiliza el mismo contenido visual que TODAY (view_movie_content), con un
 * header propio de la película abierta y flecha "atrás".
 *
 * Se navega desde Catalog / Watchlist / Profile al tocar un poster.
 *
 * Conexión con backend:
 *   render(viewModel.filmState)   // FilmUiState producido por la lógica
 *
 * Mientras no haya datos reales, render() usa el estado por defecto (placeholder).
 */
class FilmFragment : Fragment() {

    private var _binding: FragmentFilmBinding? = null
    private val binding get() = _binding!!

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
        // TODO: reemplazar por el estado del ViewModel cuando exista.
        render(FilmUiState())

        // Flecha atrás → vuelve al fragment anterior del back stack.
        binding.btnBack.setOnClickListener { navigateBack() }

        // Nota (solo capa visual): los botones de acción (btnWatchTrailer,
        // btnWatchlist, btnWatched) quedan sin lógica; se conectarán al
        // ViewModel/Repository en una fase posterior.
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
