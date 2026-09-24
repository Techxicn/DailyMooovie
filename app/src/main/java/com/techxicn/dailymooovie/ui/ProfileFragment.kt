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
import com.techxicn.dailymooovie.data.MovieRepository
import com.techxicn.dailymooovie.data.UserMovieRepository
import com.techxicn.dailymooovie.databinding.FragmentProfileBinding
import com.techxicn.dailymooovie.model.MovieCardUi
import com.techxicn.dailymooovie.model.ProfileUiState
import com.techxicn.dailymooovie.ui.adapter.FavoritesAdapter
import kotlinx.coroutines.launch
import java.util.Calendar

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
    private val userRepo = UserMovieRepository()
    private val movieRepo = MovieRepository()
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

        // Recarga el perfil desde Firebase y luego renderiza con el nombre real
        // y las estadísticas reales del usuario (watched del año + streak).
        showLoading()
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                authManager.reloadUser()
                render(buildProfileState())
                showContent()
            } catch (e: Exception) {
                showError()
            }
        }

        binding.btnSignOut.setOnClickListener { host?.onSignOut() }

        // Ajustes → pantalla Settings (toggle del recordatorio diario, etc.).
        binding.btnSettings.setOnClickListener { navigateTo(SettingsFragment()) }

        // Nota (solo capa visual): btnAchievements / btnShare quedan sin lógica;
        // se conectarán en una fase posterior.
    }

    /**
     * Construye el estado de Profile: nombre e inicial del usuario real de Firebase
     * (displayName) y estadísticas reales desde Realtime Database:
     *   • statYear            → películas con status "watched" vistas este año.
     *   • statCurrentStreak   → streak.current del usuario.
     *   • statLongestStreak   → streak.longest del usuario.
     *   • watchedCountLabel   → total de películas vistas.
     * Los descubrimientos siguen usando datos de ejemplo (sin fuente aún).
     */
    private suspend fun buildProfileState(): ProfileUiState {
        val name = authManager.currentDisplayName() ?: getString(R.string.profile_name_fallback)
        val initial = authManager.currentInitial()

        // Estadísticas reales del usuario. Ante error de red se usan ceros por
        // defecto (getWatched*/getStreak ya devuelven vacío sin sesión).
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val watchedThisYear = runCatching { userRepo.getWatchedCountForYear(currentYear) }.getOrDefault(0)
        val totalWatched = runCatching { userRepo.getWatchedCount() }.getOrDefault(0)
        val streak = runCatching { userRepo.getStreak() }.getOrNull()

        // Discoveries = últimas 3-5 películas marcadas como "watched" (más reciente
        // primero por watchedAt). Se resuelven a Movie para obtener título + poster.
        val discoveries = runCatching {
            userRepo.getRecentWatchedMovieIds(limit = 5)
                .mapNotNull { id -> movieRepo.getMovieById(id) }
                .map { movie ->
                    MovieCardUi(
                        id = movie.id,
                        title = movie.title,
                        subtitle = movie.director,
                        posterUrl = movie.posterUrl.ifBlank { null }
                    )
                }
        }.getOrDefault(emptyList())

        return ProfileUiState(
            name = name,
            avatarInitial = initial,
            watchedCountLabel = getString(R.string.profile_watched_count_fmt, totalWatched),
            statYear = watchedThisYear,
            statCurrentStreak = streak?.current ?: 0,
            statLongestStreak = streak?.longest ?: 0,
            discoveriesTitle = getString(R.string.profile_discoveries_title_fmt, name),
            discoveries = discoveries
        )
    }

    /** Punto único de entrada de datos a la pantalla. */
    fun render(state: ProfileUiState) {
        binding.bind(state)
        binding.rvDiscoveries.adapter = FavoritesAdapter(state.discoveries)
    }

    private fun showLoading() {
        binding.progressLoading.visibility = View.VISIBLE
        binding.contentScroll.visibility = View.GONE
        binding.tvError.visibility = View.GONE
    }

    private fun showContent() {
        binding.progressLoading.visibility = View.GONE
        binding.tvError.visibility = View.GONE
        binding.contentScroll.visibility = View.VISIBLE
    }

    private fun showError() {
        binding.progressLoading.visibility = View.GONE
        binding.contentScroll.visibility = View.GONE
        binding.tvError.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
