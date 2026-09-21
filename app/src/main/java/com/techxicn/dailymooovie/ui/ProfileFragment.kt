package com.techxicn.dailymooovie.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.techxicn.dailymooovie.R
import com.techxicn.dailymooovie.auth.AuthHost
import com.techxicn.dailymooovie.auth.AuthManager
import com.techxicn.dailymooovie.databinding.FragmentProfileBinding
import com.techxicn.dailymooovie.model.ProfileUiState
import com.techxicn.dailymooovie.model.SampleData
import com.techxicn.dailymooovie.ui.adapter.FavoritesAdapter
import kotlinx.coroutines.launch

/**
 * Tab PROFILE — perfil del usuario.
 *
 * El nombre y la inicial del avatar se toman del NOMBRE con el que se registró el
 * usuario (displayName de Firebase). Nunca se usa el correo como nombre visible.
 * Antes de leerlo se recarga el perfil (reloadUser) para asegurar que displayName
 * esté propagado tras el primer login.
 *
 * Stats y descubrimientos aún no tienen fuente real: usan datos de ejemplo hasta
 * que exista el ViewModel/Repository que los provea.
 */
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var authManager: AuthManager
    private val host get() = activity as? AuthHost

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        authManager = AuthManager(requireContext().applicationContext)

        binding.rvDiscoveries.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        // Recarga el perfil desde Firebase y luego renderiza con el nombre real.
        viewLifecycleOwner.lifecycleScope.launch {
            authManager.reloadUser()
            render(buildProfileState())
        }

        binding.btnSignOut.setOnClickListener { host?.onSignOut() }

        // Nota (solo capa visual): btnAchievements / btnShare / btnSettings
        // quedan sin lógica; se conectarán en una fase posterior.
    }

    /**
     * Construye el estado de Profile: nombre e inicial del usuario real de Firebase
     * (displayName), y datos de ejemplo para lo que aún no tiene backend.
     */
    private fun buildProfileState(): ProfileUiState {
        val name = authManager.currentDisplayName() ?: getString(R.string.profile_name_fallback)
        val initial = authManager.currentInitial()
        return ProfileUiState(
            name = name,
            avatarInitial = initial,
            discoveriesTitle = getString(R.string.profile_discoveries_title_fmt, name),
            discoveries = SampleData.discoveries
            // role, watchedCountLabel y stats mantienen sus valores de ejemplo
            // hasta que exista una fuente real (Firestore/Repository).
        )
    }

    /** Punto único de entrada de datos a la pantalla. */
    fun render(state: ProfileUiState) {
        binding.bind(state)
        binding.rvDiscoveries.adapter = FavoritesAdapter(state.discoveries)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
