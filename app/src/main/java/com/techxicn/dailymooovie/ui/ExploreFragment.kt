package com.techxicn.dailymooovie.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.techxicn.dailymooovie.databinding.FragmentExploreBinding
import com.techxicn.dailymooovie.model.CatalogUiState
import com.techxicn.dailymooovie.model.SampleData
import com.techxicn.dailymooovie.ui.adapter.CatalogMovieAdapter

/**
 * Tab CATALOG (Film Catalog) — grid mensual de películas recomendadas.
 *
 * Conexión con backend:
 *   render(viewModel.catalogState)   // CatalogUiState producido por la lógica
 *
 * Mientras no haya datos reales, render() usa el estado por defecto poblado con
 * SampleData. Las flechas de mes están listas para disparar el cambio de mes:
 * hoy solo invocan onMonthChange(±1), que la lógica reemplazará por una recarga
 * real del estado.
 */
class ExploreFragment : Fragment() {

    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: reemplazar por el estado del ViewModel cuando exista.
        render(CatalogUiState(movies = SampleData.catalogMovies))

        // Flechas de mes: listas para disparar el cambio de mes.
        binding.btnPrevMonth.setOnClickListener { onMonthChange(-1) }
        binding.btnNextMonth.setOnClickListener { onMonthChange(+1) }
    }

    /** Punto único de entrada de datos a la pantalla. */
    fun render(state: CatalogUiState) {
        binding.tvSectionLabel.text = state.sectionLabel
        binding.tvPositionCounter.text = state.positionCounter
        binding.tvMonth.text = state.monthLabel
        binding.rvCatalog.adapter = CatalogMovieAdapter(state.movies) { /* movie ->
            // TODO: navigateTo(FilmFragment()) con la película seleccionada. */
        }
    }

    /**
     * Cambio de mes (solo capa visual). [delta] = -1 mes anterior, +1 siguiente.
     * La lógica del proyecto reemplazará esto por una recarga real del estado
     * del mes correspondiente.
     */
    private fun onMonthChange(delta: Int) {
        // TODO: viewModel.changeMonth(delta) → nuevo CatalogUiState → render(...)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
