package com.techxicn.dailymooovie.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.techxicn.dailymooovie.R
import com.techxicn.dailymooovie.data.MovieRepository
import com.techxicn.dailymooovie.data.MovieStatus
import com.techxicn.dailymooovie.data.MovieUiMapper
import com.techxicn.dailymooovie.data.UserMovieRepository
import com.techxicn.dailymooovie.databinding.FragmentExploreBinding
import com.techxicn.dailymooovie.model.CatalogUiState
import com.techxicn.dailymooovie.ui.adapter.CatalogMovieAdapter
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

/**
 * Tab CATALOG (Film Catalog) — grid mensual de películas recomendadas.
 *
 * Carga el catálogo del mes seleccionado desde /movies (MovieRepository) y cruza
 * cada película con el estado del usuario (/users/{uid}/movies) para mostrar
 * "Watched" / "Not Watched" real. Las flechas cambian de mes y recargan.
 */
class ExploreFragment : Fragment() {

    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!

    private val movieRepo = MovieRepository()
    private val userRepo = UserMovieRepository()

    // Mes actualmente mostrado (se inicializa al mes en curso).
    private var year = 0
    private var month = 0 // 1..12

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

        val now = Calendar.getInstance()
        year = now.get(Calendar.YEAR)
        month = now.get(Calendar.MONTH) + 1

        binding.btnPrevMonth.setOnClickListener { onMonthChange(-1) }
        binding.btnNextMonth.setOnClickListener { onMonthChange(+1) }

        loadCatalog()
    }

    /** Carga el catálogo del mes actual y cruza con el estado del usuario. */
    private fun loadCatalog() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val movies = movieRepo.getCatalogForMonth(year, month)
                val statuses = userRepo.getAllStatuses()
                val items = movies.map { movie ->
                    val watched = statuses[movie.id]?.status == MovieStatus.WATCHED
                    MovieUiMapper.toCatalogItem(movie, watched)
                }
                render(
                    CatalogUiState(
                        monthLabel = monthLabel(),
                        positionCounter = "%02d/%02d".format(0, items.size),
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

    /** Etiqueta "SEPTEMBER 2026" para el mes/año actuales. */
    private fun monthLabel(): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val monthName = cal.getDisplayName(
            Calendar.MONTH, Calendar.LONG, Locale.ENGLISH
        ).orEmpty().uppercase(Locale.ENGLISH)
        return "$monthName $year"
    }

    /** Punto único de entrada de datos a la pantalla. */
    fun render(state: CatalogUiState) {
        binding.tvSectionLabel.text = state.sectionLabel
        binding.tvPositionCounter.text = state.positionCounter
        binding.tvMonth.text = state.monthLabel
        binding.rvCatalog.adapter = CatalogMovieAdapter(state.movies) { movie ->
            navigateTo(FilmFragment.newInstance(movie.id))
        }
    }

    /** Cambio de mes: [delta] = -1 mes anterior, +1 siguiente. Recarga el estado. */
    private fun onMonthChange(delta: Int) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, delta)
        }
        year = cal.get(Calendar.YEAR)
        month = cal.get(Calendar.MONTH) + 1
        loadCatalog()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
