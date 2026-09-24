package com.techxicn.dailymooovie.notifications

import android.content.Context

/**
 * ReminderPreferences
 * ═══════════════════
 * Fuente única de verdad (en SharedPreferences) del estado del recordatorio diario:
 *
 *   • enabled → si el usuario QUIERE recibir el recordatorio (preferencia explícita
 *     controlada desde el toggle de Settings). Por defecto true: se activa al primer
 *     login (junto con la petición de permiso), y el usuario puede desactivarlo.
 *
 *   • permissionAsked → si YA se pidió el permiso POST_NOTIFICATIONS (Android 13+)
 *     alguna vez, para no volver a insistir tras el primer login. El toggle de
 *     Settings puede volver a pedirlo de forma explícita (acción del usuario).
 *
 * Centralizar esto evita claves de preferencias sueltas repartidas entre
 * MainActivity y SettingsFragment.
 */
class ReminderPreferences(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** ¿El usuario quiere el recordatorio diario? Por defecto true. */
    var enabled: Boolean
        get() = prefs.getBoolean(KEY_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_ENABLED, value).apply()

    /** ¿Ya se solicitó alguna vez el permiso de notificaciones (13+)? */
    var permissionAsked: Boolean
        get() = prefs.getBoolean(KEY_PERMISSION_ASKED, false)
        set(value) = prefs.edit().putBoolean(KEY_PERMISSION_ASKED, value).apply()

    companion object {
        private const val PREFS_NAME = "notifications_prefs"
        private const val KEY_ENABLED = "daily_reminder_enabled"

        // Se conserva la MISMA clave que ya usaba MainActivity para no perder el
        // estado "ya preguntado" de instalaciones existentes.
        private const val KEY_PERMISSION_ASKED = "post_notifications_asked"
    }
}
