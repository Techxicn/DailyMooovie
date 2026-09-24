package com.techxicn.dailymooovie

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.techxicn.dailymooovie.auth.AuthHost
import com.techxicn.dailymooovie.auth.AuthManager
import com.techxicn.dailymooovie.data.DailyQueueRepository
import com.techxicn.dailymooovie.databinding.ActivityMainBinding
import com.techxicn.dailymooovie.notifications.DailyReminderScheduler
import com.techxicn.dailymooovie.notifications.ReminderPreferences
import com.techxicn.dailymooovie.theme.ThemePreferences
import com.techxicn.dailymooovie.ui.ExploreFragment
import com.techxicn.dailymooovie.ui.LoginFragment
import com.techxicn.dailymooovie.ui.MyMoviesFragment
import com.techxicn.dailymooovie.ui.ProfileFragment
import com.techxicn.dailymooovie.ui.RegisterFragment
import com.techxicn.dailymooovie.ui.TodayFragment
import kotlinx.coroutines.launch

/**
 * Shell principal de MOOOVIE.
 *
 * Decide al arrancar entre:
 *   • Login (si no hay sesión de Firebase) — bottom nav oculto.
 *   • App con los 4 tabs (si hay sesión) — bottom nav visible, tab Today.
 *
 * Implementa [AuthHost] para que Login/Register naveguen sin acoplarse a la Activity.
 *
 * También coordina el recordatorio diario local (notificación de la película del
 * día vía WorkManager): programa el trabajo al iniciar sesión, lo cancela al
 * cerrarla, pide el permiso de notificaciones (Android 13+) tras el login sin
 * insistir, y maneja el deep-link de la notificación para abrir Today.
 */
class MainActivity : AppCompatActivity(), AuthHost {

    private lateinit var binding: ActivityMainBinding
    private lateinit var authManager: AuthManager
    private val queueRepo = DailyQueueRepository()

    /**
     * Lanzador del permiso POST_NOTIFICATIONS (Android 13+). Se registra siempre
     * (requisito de la API), pero solo se dispara la primera vez tras el login.
     * La respuesta del usuario no reintenta nada: se respeta su decisión.
     */
    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* granted o no: sin insistir */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Aplica el modo de tema (claro/oscuro/sistema) persistido ANTES de inflar la
        // UI, para que la app abra directamente en el tema elegido por el usuario.
        ThemePreferences(applicationContext).apply()
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
                // Deep-link al arrancar desde la notificación con la app cerrada.
                if (intent?.getBooleanExtra(EXTRA_OPEN_TODAY, false) == true) {
                    goToToday()
                }
            } else {
                showLogin()
            }
        }
    }

    /**
     * La app usa launchMode="singleTop", así que si ya está viva y se toca la
     * notificación, el nuevo Intent llega aquí (no se recrea la Activity). Si trae
     * EXTRA_OPEN_TODAY y hay sesión, se navega a Today.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra(EXTRA_OPEN_TODAY, false) && authManager.isLoggedIn()) {
            goToToday()
        }
    }

    // ── AuthHost ───────────────────────────────────────────────────────────

    override fun onAuthSuccess() {
        // Programa el recordatorio diario solo si el usuario no lo ha desactivado
        // (preferencia enabled, por defecto true). Idempotente vía WorkManager.
        if (ReminderPreferences(applicationContext).enabled) {
            DailyReminderScheduler.schedule(applicationContext)
        }
        // Pide el permiso de notificaciones (13+) en un momento con contexto: justo
        // tras autenticarse, y solo la primera vez (no insiste si ya se preguntó).
        maybeRequestNotificationPermission()
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
        // Cancela el recordatorio: no debe notificar a un usuario sin sesión.
        DailyReminderScheduler.cancel(applicationContext)
        authManager.signOut()
        showLogin()
    }

    /**
     * Entra a la app autenticada: primero asegura la cola diaria del usuario
     * (genera en el primer acceso + sincroniza ids nuevos del catálogo), luego
     * muestra el bottom nav y el tab Today.
     *
     * El init de la cola se hace ANTES de mostrar Today para que la película del
     * día ya se pueda resolver en el primer login. Ante fallo de red se muestra
     * Today igualmente (mostrará su placeholder si aún no hay cola).
     */
    private fun enterApp() {
        setAuthChromeVisible(true)
        binding.bottomNav.selectedItemId = R.id.navToday
        lifecycleScope.launch {
            // Idempotente: genera la cola solo si no existía; syncCatalog agrega al
            // final los ids nuevos de /movies sin tocar el orden previo.
            runCatching {
                queueRepo.ensureQueueGenerated()
                queueRepo.syncCatalog()
            }
            showFragment(TodayFragment())
        }
    }

    /** Navega al tab Today (usado por el deep-link de la notificación). */
    private fun goToToday() {
        setAuthChromeVisible(true)
        // Al setear el id seleccionado se dispara el listener que muestra Today; si
        // ya estaba seleccionado, se fuerza explícitamente el fragmento.
        if (binding.bottomNav.selectedItemId == R.id.navToday) {
            showFragment(TodayFragment())
        } else {
            binding.bottomNav.selectedItemId = R.id.navToday
        }
    }

    /**
     * Pide POST_NOTIFICATIONS solo en Android 13+ y solo la PRIMERA vez (se recuerda
     * en SharedPreferences). No reintenta ni insiste si el usuario ya respondió:
     * respeta su decisión. Si más adelante quiere activarlas, podrá hacerlo desde
     * los ajustes del sistema (o un futuro toggle en Settings).
     */
    private fun maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        val alreadyGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (alreadyGranted) return

        val reminderPrefs = ReminderPreferences(applicationContext)
        if (reminderPrefs.permissionAsked) return

        // Marca que ya se preguntó ANTES de pedir, para no volver a insistir aunque
        // el usuario deniegue o descarte el diálogo.
        reminderPrefs.permissionAsked = true
        requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
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

    companion object {
        /**
         * Extra del Intent que indica abrir la app directamente en Today. Lo pone
         * el PendingIntent de la notificación del recordatorio diario.
         */
        const val EXTRA_OPEN_TODAY = "com.techxicn.dailymooovie.OPEN_TODAY"
    }
}
