package com.techxicn.dailymooovie.theme

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

/**
 * ThemePreferences
 * ════════════════
 * Persiste la preferencia de tema del usuario (claro / oscuro / seguir sistema) y
 * la aplica vía [AppCompatDelegate.setDefaultNightMode].
 *
 * AppCompatDelegate recrea las Activities visibles al cambiar el modo, así que el
 * toggle surte efecto de inmediato. Persistir el valor permite reaplicarlo al
 * arrancar (antes de inflar la UI) para que la app abra en el tema elegido.
 *
 * Modos soportados (guardados como Int estable, no como el valor de AppCompat, que
 * podría cambiar entre versiones):
 *   • MODE_SYSTEM (0) → sigue el modo del sistema (por defecto).
 *   • MODE_LIGHT  (1) → siempre claro.
 *   • MODE_DARK   (2) → siempre oscuro.
 */
class ThemePreferences(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Modo persistido (MODE_SYSTEM por defecto). */
    var mode: Int
        get() = prefs.getInt(KEY_MODE, MODE_SYSTEM)
        set(value) = prefs.edit().putInt(KEY_MODE, value).apply()

    /** ¿El usuario eligió explícitamente modo oscuro? (útil para el switch). */
    val isDark: Boolean get() = mode == MODE_DARK

    /**
     * Traduce el modo guardado al valor de AppCompatDelegate correspondiente.
     */
    private fun toDelegateMode(value: Int): Int = when (value) {
        MODE_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
        MODE_DARK -> AppCompatDelegate.MODE_NIGHT_YES
        else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }

    /** Aplica el modo actualmente persistido (sin cambiarlo). */
    fun apply() {
        AppCompatDelegate.setDefaultNightMode(toDelegateMode(mode))
    }

    /**
     * Guarda [newMode] y lo aplica. AppCompat recreará las Activities visibles para
     * reflejar el nuevo tema.
     */
    fun setAndApply(newMode: Int) {
        mode = newMode
        AppCompatDelegate.setDefaultNightMode(toDelegateMode(newMode))
    }

    companion object {
        const val MODE_SYSTEM = 0
        const val MODE_LIGHT = 1
        const val MODE_DARK = 2

        private const val PREFS_NAME = "theme_prefs"
        private const val KEY_MODE = "theme_mode"
    }
}
