package com.techxicn.dailymooovie.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.techxicn.dailymooovie.MainActivity
import com.techxicn.dailymooovie.R
import com.techxicn.dailymooovie.databinding.FragmentTodayBinding
import com.techxicn.dailymooovie.model.TodayUiState

/**
 * Tab TODAY — película del día.
 *
 * Conexión con backend:
 *   render(viewModel.todayState)   // TodayUiState producido por la lógica
 *
 * Mientras no haya datos reales, render() usa el estado por defecto (placeholder).
 */
class TodayFragment : Fragment() {

    private var _binding: FragmentTodayBinding? = null
    private val binding get() = _binding!!

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
        // TODO: reemplazar por el estado del ViewModel cuando exista.
        render(TodayUiState())

        // "Tu Journey →" cambia al tab Journey del bottom nav.
        binding.linkJourney.setOnClickListener {
            (activity as? MainActivity)?.selectTab(R.id.navJourney)
        }
        // "LEER LA HISTORIA" — pantalla de detalle de película (pendiente).
        binding.btnReadStory.setOnClickListener { /* TODO: pantalla detalle película */ }
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
