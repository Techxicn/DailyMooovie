package com.techxicn.dailymooovie.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.techxicn.dailymooovie.databinding.FragmentJourneyBinding
import com.techxicn.dailymooovie.model.JourneyUiState
import com.techxicn.dailymooovie.model.SampleData
import com.techxicn.dailymooovie.ui.adapter.RecentPostersAdapter

class JourneyFragment : Fragment() {

    private var _binding: FragmentJourneyBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJourneyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        render(JourneyUiState(heatmap = SampleData.sampleHeatmap()))
        setupRecent()
        wireNavigation()
    }

    private fun setupRecent() {
        binding.rvRecentPosters.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvRecentPosters.adapter = RecentPostersAdapter(SampleData.recentMovies)
    }

    /** Cablea los botones/filas de Journey hacia sus pantallas de detalle. */
    private fun wireNavigation() = with(binding) {
        // Fila "Año en películas" → pantalla YearInFilm.
        rowYearInFilm.setOnClickListener { navigateTo(YearInFilmFragment()) }
        // Banner de logro → Top Five (tipo de espectador / ranking).
        bannerAchievement.setOnClickListener { navigateTo(TopFiveFragment()) }
        // "VER TODO" de RECIENTEMENTE → Top Five como listado destacado.
        tvSeeAllRecent.setOnClickListener { navigateTo(TopFiveFragment()) }
        // Filas de menú restantes: reservadas para pantallas futuras
        // (Calendario, Resumen mensual, Logros). Listeners preparados.
        rowCalendar.setOnClickListener { /* TODO: pantalla Calendario */ }
        rowMonthlySummary.setOnClickListener { /* TODO: pantalla Resumen mensual */ }
        rowAchievements.setOnClickListener { /* TODO: pantalla Logros */ }
    }

    fun render(state: JourneyUiState) {
        binding.bind(state)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
