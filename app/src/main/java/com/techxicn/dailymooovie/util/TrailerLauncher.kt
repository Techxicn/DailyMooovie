package com.techxicn.dailymooovie.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.net.toUri
import com.techxicn.dailymooovie.R

/**
 * TrailerLauncher
 * ═══════════════
 * Abre la URL de un tráiler con un Intent.ACTION_VIEW, de forma segura:
 *   • Si la URL está vacía o malformada → muestra un mensaje simple (no crashea).
 *   • Si no hay app que pueda abrir el enlace → mensaje simple.
 *
 * Se considera "malformada" cualquier URL sin esquema http/https (Uri.parse casi
 * nunca lanza, pero un esquema ausente hará fallar el ACTION_VIEW).
 */
object TrailerLauncher {

    /** Intenta abrir [trailerUrl]. Devuelve true si se lanzó el intent. */
    fun open(context: Context, trailerUrl: String?): Boolean {
        val url = trailerUrl?.trim().orEmpty()
        if (!isValidWebUrl(url)) {
            toast(context, R.string.trailer_unavailable)
            return false
        }
        return try {
            val uri: Uri = url.toUri()
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            toast(context, R.string.trailer_no_app)
            false
        } catch (e: Exception) {
            toast(context, R.string.trailer_unavailable)
            false
        }
    }

    /** true si [url] tiene esquema http/https y algo después del esquema. */
    fun isValidWebUrl(url: String): Boolean {
        val lower = url.lowercase()
        return (lower.startsWith("http://") || lower.startsWith("https://")) &&
            url.length > "https://".length
    }

    private fun toast(context: Context, resId: Int) {
        Toast.makeText(context, context.getString(resId), Toast.LENGTH_SHORT).show()
    }
}
