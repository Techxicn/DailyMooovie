package com.techxicn.dailymooovie

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.techxicn.dailymooovie.auth.AuthHost
import com.techxicn.dailymooovie.auth.AuthManager
import com.techxicn.dailymooovie.databinding.ActivityMainBinding
import com.techxicn.dailymooovie.ui.ExploreFragment
import com.techxicn.dailymooovie.ui.LoginFragment
import com.techxicn.dailymooovie.ui.MyMoviesFragment
import com.techxicn.dailymooovie.ui.ProfileFragment
import com.techxicn.dailymooovie.ui.RegisterFragment
import com.techxicn.dailymooovie.ui.TodayFragment

/**
 * Shell principal de MOOOVIE.
 *
 * Decide al arrancar entre:
 *   • Login (si no hay sesión de Firebase) — bottom nav oculto.
 *   • App con los 4 tabs (si hay sesión) — bottom nav visible, tab Today.
 *
 * Implementa [AuthHost] para que Login/Register naveguen sin acoplarse a la Activity.
 */
class MainActivity : AppCompatActivity(), AuthHost {

    private lateinit var binding: ActivityMainBinding
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        authManager = AuthManager(applicationContext)

        // Aplica insets del sistema al root y al bottom nav.
        ViewCompat.setOnApplyWindowInsetsListener(binding.rootLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = systemBars.top)
            binding.bottomNav.updatePadding(bottom = systemBars.bottom)
            insets
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.navToday -> TodayFragment()
                R.id.navExplore -> ExploreFragment()
                R.id.navMovies -> MyMoviesFragment()
                R.id.navProfile -> ProfileFragment()
                else -> return@setOnItemSelectedListener false
            }
            showFragment(fragment)
            true
        }

        // Estado inicial: sesión activa → app; si no → login.
        if (savedInstanceState == null) {
            if (authManager.isLoggedIn()) {
                enterApp()
            } else {
                showLogin()
            }
        }
    }

    // ── AuthHost ───────────────────────────────────────────────────────────

    override fun onAuthSuccess() {
        enterApp()
    }

    override fun showRegister() {
        setAuthChromeVisible(false)
        showFragment(RegisterFragment())
    }

    override fun showLogin() {
        setAuthChromeVisible(false)
        showFragment(LoginFragment())
    }

    override fun onSignOut() {
        authManager.signOut()
        showLogin()
    }

    /** Entra a la app autenticada: muestra el bottom nav y el tab Today. */
    private fun enterApp() {
        setAuthChromeVisible(true)
        showFragment(TodayFragment())
        binding.bottomNav.selectedItemId = R.id.navToday
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /** Muestra/oculta el bottom nav y su divisoria (ocultos en pantallas de auth). */
    private fun setAuthChromeVisible(visible: Boolean) {
        val visibility = if (visible) View.VISIBLE else View.GONE
        binding.bottomNav.visibility = visibility
        binding.navDivider.visibility = visibility
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
