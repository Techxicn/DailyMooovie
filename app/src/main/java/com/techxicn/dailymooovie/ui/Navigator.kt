package com.techxicn.dailymooovie.ui

import androidx.fragment.app.Fragment
import com.techxicn.dailymooovie.R

/**
 * Navigator.kt
 * ════════════
 * Helpers de navegación entre fragments dentro del contenedor principal
 * (R.id.fragmentContainer de activity_main).
 *
 * - navigateTo(): apila un fragment de detalle sobre el actual, añadiéndolo
 *   al back stack para que el botón atrás (y btnBack) pueda volver.
 * - navigateBack(): saca el fragment superior del back stack.
 *
 * Se usan desde cualquier Fragment hijo del contenedor.
 */

/** Apila [fragment] sobre el actual y lo añade al back stack. */
fun Fragment.navigateTo(fragment: Fragment, tag: String? = null) {
    parentFragmentManager.beginTransaction()
        .setCustomAnimations(
            android.R.anim.fade_in,
            android.R.anim.fade_out,
            android.R.anim.fade_in,
            android.R.anim.fade_out
        )
        .replace(R.id.fragmentContainer, fragment, tag)
        .addToBackStack(tag)
        .commit()
}

/** Vuelve al fragment anterior del back stack. */
fun Fragment.navigateBack() {
    parentFragmentManager.popBackStack()
}
