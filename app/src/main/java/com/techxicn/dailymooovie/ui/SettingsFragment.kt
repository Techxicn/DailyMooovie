package com.techxicn.dailymooovie.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.techxicn.dailymooovie.databinding.FragmentSettingsBinding
import com.techxicn.dailymooovie.notifications.DailyReminderScheduler
import com.techxicn.dailymooovie.notifications.ReminderPreferences
import com.techxicn.dailymooovie.theme.ThemePreferences

/**
 * Pantalla de AJUSTES (Settings).
 *
 * Por ahora contiene el toggle del recordatorio diario (notificación local de la
 * película del día ~8:00 AM). El estado combina dos cosas:
 *   • La preferencia del usuario ([ReminderPreferences.enabled]).
 *   • El permiso real POST_NOTIFICATIONS (Android 13+): sin él no se puede notificar.
 *
 * El switch aparece ACTIVO solo si el usuario lo quiere Y el permiso está concedido
 * (en < 13 el permiso es implícito). Así refleja el estado real del sistema.
 *
 * Interacciones:
 *   • Encender sin permiso (13+) → se pide el permiso. Si el usuario ya lo denegó de
 *     forma permanente, se le ofrece abrir los ajustes del sistema de la app.
 *   • Encender con permiso → se programa el trabajo y se guarda enabled = true.
 *   • Apagar → se cancela el trabajo y se guarda enabled = false.
 *   • onResume re-sincroniza el switch (p. ej. si revocó/concedió el permiso desde
 *     los ajustes del sistema y volvió a la app).
 */
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var reminderPrefs: ReminderPreferences
    private lateinit var themePrefs: ThemePreferences

    /**
     * Evita que el listener del switch reaccione mientras lo actualizamos por
     * código (sincronización), lo que provocaría acciones no deseadas.
     */
    private var updatingSwitch = false

    /** Lanzador del permiso POST_NOTIFICATIONS (13+) disparado desde el toggle. */
    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            reminderPrefs.permissionAsked = true
            if (granted) {
                // Concedido → activa realmente el recordatorio.
                enableReminder()
            }
            // Denegado → no se activa; syncUi dejará el switch apagado y la nota visible.
            syncUi()
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        reminderPrefs = ReminderPreferences(requireContext().applicationContext)
        themePrefs = ThemePreferences(requireContext().applicationContext)

        binding.btnBack.setOnClickListener { navigateBack() }

        binding.switchDailyReminder.setOnCheckedChangeListener { _, isChecked ->
            if (updatingSwitch) return@setOnCheckedChangeListener
            onReminderToggled(isChecked)
        }

        binding.tvPermissionHint.setOnClickListener { openAppNotificationSettings() }

        // ── Modo oscuro ──────────────────────────────────────────
        // El switch refleja si el modo persistido es oscuro. Al cambiarlo se guarda
        // y se aplica de inmediato (AppCompat recrea la Activity con el nuevo tema).
        binding.switchDarkMode.isChecked = themePrefs.isDark
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            val newMode = if (isChecked) ThemePreferences.MODE_DARK else ThemePreferences.MODE_LIGHT
            // Evita recrear la Activity si el modo no cambió (p. ej. al restaurar estado).
            if (newMode == themePrefs.mode) return@setOnCheckedChangeListener
            themePrefs.setAndApply(newMode)
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-sincroniza por si el usuario cambió el permiso desde los ajustes del
        // sistema mientras estaba fuera de la app.
        syncUi()
    }

    /** Acción del usuario sobre el switch. */
    private fun onReminderToggled(wantEnabled: Boolean) {
        if (!wantEnabled) {
            disableReminder()
            syncUi()
            return
        }

        // Quiere activarlo. En 13+ hace falta el permiso.
        if (needsNotificationPermission()) {
            when {
                // Aún se puede pedir con diálogo (primera vez o "denegar" simple).
                !reminderPrefs.permissionAsked || shouldShowRationale() ->
                    requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                // Denegado permanentemente: el diálogo ya no aparece → a los ajustes.
                else -> {
                    openAppNotificationSettings()
                    syncUi() // revierte el switch: aún sin permiso.
                }
            }
            return
        }

        // Permiso concedido (o < 13): activar directamente.
        enableReminder()
        syncUi()
    }

    /** Guarda la preferencia y programa el trabajo diario. */
    private fun enableReminder() {
        reminderPrefs.enabled = true
        DailyReminderScheduler.schedule(requireContext().applicationContext)
    }

    /** Guarda la preferencia y cancela el trabajo diario. */
    private fun disableReminder() {
        reminderPrefs.enabled = false
        DailyReminderScheduler.cancel(requireContext().applicationContext)
    }

    /**
     * Ajusta el estado visual del switch y de la nota de permiso al estado REAL:
     * el switch queda encendido solo si el usuario lo quiere Y hay permiso. La nota
     * de permiso se muestra cuando el usuario lo quiere pero el permiso falta.
     */
    private fun syncUi() {
        val binding = _binding ?: return
        val wantEnabled = reminderPrefs.enabled
        val hasPermission = !needsNotificationPermission()
        val effectivelyOn = wantEnabled && hasPermission

        // Si el usuario quería el recordatorio pero perdió el permiso, se refleja en
        // la preferencia y se cancela el trabajo (no tiene sentido mantenerlo).
        if (wantEnabled && !hasPermission) {
            DailyReminderScheduler.cancel(requireContext().applicationContext)
        }

        updatingSwitch = true
        binding.switchDailyReminder.isChecked = effectivelyOn
        updatingSwitch = false

        binding.tvPermissionHint.visibility =
            if (wantEnabled && !hasPermission) View.VISIBLE else View.GONE
    }

    /** True si estamos en 13+ y el permiso NO está concedido. */
    private fun needsNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return false
        return ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
    }

    private fun shouldShowRationale(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)

    /** Abre la pantalla de notificaciones de la app en los ajustes del sistema. */
    private fun openAppNotificationSettings() {
        val context = requireContext()
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.fromParts("package", context.packageName, null))
        }
        // Ante dispositivos sin la pantalla exacta, no romper.
        runCatching { startActivity(intent) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
