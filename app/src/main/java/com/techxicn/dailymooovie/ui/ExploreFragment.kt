package com.techxicn.dailymooovie.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.techxicn.dailymooovie.R
import com.techxicn.dailymooovie.data.DailyQueueRepository
import com.techxicn.dailymooovie.data.DateUtils
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
 * Para cada día del mes mostrado se resuelve QUÉ película corresponde usando la
 * dailyQueue del usuario (única por usuario), no una query por releaseDate. Los
 * días anteriores a queueStartDate (el usuario aún no tenía cuenta) se omiten.
 * Cada película se cruza con el estado del usuario (/users/{uid}/movies) para
 * mostrar "Watched" / "Not Watched" real. Las flechas cambian de mes y recargan.
 */
class ExploreFragment : Fragment() {

    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!

    private val movieRepo = MovieRepository()
    private val userRepo = UserMovieRepository()
    private val queueRepo = DailyQueueRepository()

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

    /**
     * Carga el catálogo del mes: para cada día del mes resuelve el movieId vía la
     * dailyQueue del usuario y lo cruza con su estado. Omite los días previos a
     * queueStartDate (sin cuenta aún) y los que no resuelvan a una película.
     */
    private fun loadCatalog() {
        showLoading()
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Se leen queue, startDate y estados UNA vez; el resto es en memoria.
                val queue = queueRepo.getQueue()
                val startDate = queueRepo.getQueueStartDate()
                val statuses = userRepo.getAllStatuses()

                val items = mutableListOf<CatalogItem>()
                if (queue.isNotEmpty() && startDate != null) {
                    // Caché local de películas ya resueltas por id (evita relecturas).
                    val movieCache = HashMap<String, com.techxicn.dailymooovie.data.Movie?>()
                    for (isoDate in datesOfMonth(year, month)) {
                        val movieId = queueRepo.resolveMovieIdForDate(isoDate, queue, startDate)
                            ?: continue // día anterior a queueStartDate → sin película
                        val movie = movieCache.getOrPut(movieId) { movieRepo.getMovieById(movieId) }
                            ?: continue
                        val watched = statuses[movie.id]?.status == MovieStatus.WATCHED
                        items += CatalogItem(
                            date = isoDate,
                            ui = MovieUiMapper.toCatalogItem(movie, isoDate, watched)
                        )
                    }
                }

                // Contador tipo "vistas / total del mes" (mismo criterio dinámico que
                // usa Watchlist para su contador, no un valor fijo). El numerador es
                // el número de películas del mes ya marcadas como vistas.
                val watchedCount = items.count { it.ui.watched }
                render(
                    CatalogUiState(
                        monthLabel = monthLabel(),
                        positionCounter = "%02d/%02d".format(watchedCount, items.size),
                        movies = items.map { it.ui }
                    ),
                    items
                )
                showContent()
            } catch (e: Exception) {
                showError()
            }
        }
    }

    /** Muestra el loader y oculta contenido/error. */
    private fun showLoading() {
        binding.progressLoading.visibility = View.VISIBLE
        binding.contentRoot.visibility = View.GONE
        binding.tvError.visibility = View.GONE
    }

    private fun showContent() {
        binding.progressLoading.visibility = View.GONE
        binding.tvError.visibility = View.GONE
        binding.contentRoot.visibility = View.VISIBLE
    }

    private fun showError() {
        binding.progressLoading.visibility = View.GONE
        binding.contentRoot.visibility = View.GONE
        binding.tvError.visibility = View.VISIBLE
    }

    /** Lista de fechas ISO ("yyyy-MM-dd") de todos los días de [year]-[month] (1..12). */
    private fun datesOfMonth(year: Int, month: Int): List<String> {
        val cal = Calendar.getInstance().apply {
            clear()
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        return (1..daysInMonth).map { day ->
            cal.set(Calendar.DAY_OF_MONTH, day)
            DateUtils.format(cal.time)
        }
    }

    /** Item del catálogo con su fecha ISO (para navegar a Film con el día correcto). */
    private data class CatalogItem(val date: String, val ui: com.techxicn.dailymooovie.model.CatalogMovieUi)

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
    private fun render(state: CatalogUiState, items: List<CatalogItem>) {
        binding.tvSectionLabel.text = state.sectionLabel
        binding.tvPositionCounter.text = state.positionCounter
        binding.tvMonth.text = state.monthLabel
        // Mapa id (del item clicado) → fecha ISO del día que representa, para que
        // Film muestre la etiqueta de fecha del día correcto (no una fecha propia).
        binding.rvCatalog.adapter = CatalogMovieAdapter(
            items = state.movies,
            onItemClick = { movie ->
                val date = items.firstOrNull { it.ui.id == movie.id }?.date
                navigateTo(FilmFragment.newInstance(movie.id, date))
            }
        )
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
