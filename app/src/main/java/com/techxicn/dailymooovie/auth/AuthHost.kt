package com.techxicn.dailymooovie.auth

/**
 * Contrato que el host (MainActivity) implementa para que las pantallas de
 * autenticación notifiquen eventos de navegación, sin acoplarse a la Activity.
 */
interface AuthHost {
    /** Autenticación correcta → mostrar la app (Today). */
    fun onAuthSuccess()

    /** Ir a la pantalla de registro. */
    fun showRegister()

    /** Ir a la pantalla de inicio de sesión. */
    fun showLogin()

    /** Cerrar sesión y volver a Login. */
    fun onSignOut()
}
