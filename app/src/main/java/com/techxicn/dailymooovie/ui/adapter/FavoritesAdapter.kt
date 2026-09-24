package com.techxicn.dailymooovie.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.techxicn.dailymooovie.databinding.ItemFavMovieBinding
import com.techxicn.dailymooovie.model.MovieCardUi

/**
 * Adapter del carrusel "FAVORITAS" (Profile).
 * Muestra poster + título por cada película favorita.
 */
class FavoritesAdapter(
    private val items: List<MovieCardUi>
) : RecyclerView.Adapter<FavoritesAdapter.VH>() {

    inner class VH(val binding: ItemFavMovieBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemFavMovieBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.binding.tvFavTitle.text = item.title
        com.techxicn.dailymooovie.util.ImageLoader.loadPoster(holder.binding.ivFavPoster, item.posterUrl)
    }

    override fun getItemCount(): Int = items.size
}
