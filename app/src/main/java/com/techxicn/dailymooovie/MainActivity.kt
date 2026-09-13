package com.techxicn.dailymooovie

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.techxicn.dailymooovie.databinding.ActivityMainBinding
import com.techxicn.dailymooovie.ui.ExploreFragment
import com.techxicn.dailymooovie.ui.JourneyFragment
import com.techxicn.dailymooovie.ui.MyMoviesFragment
import com.techxicn.dailymooovie.ui.ProfileFragment
import com.techxicn.dailymooovie.ui.TodayFragment

/**
 * Shell principal de DailyMooovie.
 * Aloja los 5 fragments de los tabs y gestiona la navegación por el
 * BottomNavigationView. Cada tab es responsable de recibir su UiState.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Aplica insets del sistema al root y al bottom nav.
        ViewCompat.setOnApplyWindowInsetsListener(binding.rootLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = systemBars.top)
            binding.bottomNav.updatePadding(bottom = systemBars.bottom)
            insets
        }

        if (savedInstanceState == null) {
            showFragment(TodayFragment())
            binding.bottomNav.selectedItemId = R.id.navToday
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.navToday -> TodayFragment()
                R.id.navExplore -> ExploreFragment()
                R.id.navJourney -> JourneyFragment()
                R.id.navMovies -> MyMoviesFragment()
                R.id.navProfile -> ProfileFragment()
                else -> return@setOnItemSelectedListener false
            }
            showFragment(fragment)
            true
        }
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    /**
     * Selecciona un tab del bottom nav por su id (R.id.navJourney, etc.).
     * Permite que un fragment cambie de tab (p. ej. Today → Journey).
     */
    fun selectTab(itemId: Int) {
        binding.bottomNav.selectedItemId = itemId
    }
}
