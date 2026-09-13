package com.techxicn.dailymooovie.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.techxicn.dailymooovie.databinding.ItemSealedMovieBinding
import com.techxicn.dailymooovie.model.MovieCardUi

/**
 * Adapter de la lista "SELLADO POR" (Passport Detail).
 * Cada fila: poster + título + director/año + código de país.
 *
 * Reutiliza MovieCardUi; el código de país se pasa en el campo dateLabel
 * (o crea un modelo propio si prefieres separar la semántica).
 */
class SealedMovieAdapter(
    private val items: List<MovieCardUi>
) : RecyclerView.Adapter<SealedMovieAdapter.VH>() {

    inner class VH(val binding: ItemSealedMovieBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemSealedMovieBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        with(holder.binding) {
            tvSealedTitle.text = item.title
            tvSealedMeta.text = item.subtitle
            tvSealedCountryCode.text = item.dateLabel  // aquí: código de país
            // ivSealedPoster → cargar item.posterUrl con Glide/Picasso.
        }
    }

    override fun getItemCount(): Int = items.size
}
