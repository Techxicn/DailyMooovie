package com.techxicn.dailymooovie.util

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.techxicn.dailymooovie.R

/**
 * ImageLoader
 * ═══════════
 * Punto único para cargar posters remotos con Glide. Centraliza:
 *   • placeholder mientras carga (gris liso) y error (borde punteado) para que a
 *     simple vista se distinga "cargando" de "esta URL falló".
 *   • Un User-Agent de NAVEGADOR en la petición. Algunos hosts (notablemente
 *     m.media-amazon.com, de donde salen muchos posters) responden 403 al User-Agent
 *     por defecto del cliente HTTP; con un UA de navegador sirven la imagen.
 *
 * Se inyecta el header por-petición vía GlideUrl + LazyHeaders, así no hace falta
 * un AppGlideModule ni un annotation processor (KSP/kapt) extra.
 */
object ImageLoader {

    /** User-Agent de navegador de escritorio para evitar bloqueos por UA. */
    private const val BROWSER_USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"

    /**
     * Carga [url] en [target] con placeholder/error. Si [url] es null o vacío,
     * limpia cualquier request previo y muestra el placeholder (evita reciclado
     * de imágenes viejas en celdas de RecyclerView).
     */
    fun loadPoster(target: ImageView, url: String?) {
        val glide = Glide.with(target)
        if (url.isNullOrBlank()) {
            glide.clear(target)
            target.setImageResource(R.drawable.poster_placeholder)
            return
        }

        val model = GlideUrl(
            url,
            LazyHeaders.Builder()
                .addHeader("User-Agent", BROWSER_USER_AGENT)
                .build()
        )

        glide.load(model)
            .placeholder(R.drawable.poster_placeholder)
            .error(R.drawable.poster_error)
            .centerCrop()
            .into(target)
    }
}
