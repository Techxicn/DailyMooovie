package com.techxicn.dailymooovie.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.techxicn.dailymooovie.databinding.ItemMovieGridBinding
import com.techxicn.dailymooovie.model.MovieCardUi

/**
 * Adapter del grid de "Mis Películas" (GridLayoutManager, 3 columnas).
 * Muestra poster + título + director + fila de 5 estrellas según rating.
 */
class MovieGridAdapter(
    private val items: List<MovieCardUi>
) : RecyclerView.Adapter<MovieGridAdapter.VH>() {

    inner class VH(val binding: ItemMovieGridBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemMovieGridBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        with(holder.binding) {
            tvGridTitle.text = item.title
            tvGridDirector.text = item.subtitle
            // Estrellas: llenas hasta item.rating, atenuadas el resto.
            val stars = listOf(starGrid1, starGrid2, starGrid3, starGrid4, starGrid5)
            stars.forEachIndexed { index, star ->
                star.alpha = if (index < item.rating) 1f else 0.25f
            }
            // ivGridPoster → cargar item.posterUrl con Glide/Picasso.
        }
    }

    override fun getItemCount(): Int = items.size
}
