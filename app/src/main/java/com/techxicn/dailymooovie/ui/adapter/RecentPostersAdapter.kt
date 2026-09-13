package com.techxicn.dailymooovie.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.techxicn.dailymooovie.databinding.ItemPosterRecentBinding
import com.techxicn.dailymooovie.model.MovieCardUi

/**
 * Adapter del carrusel "RECIENTEMENTE" (Journey).
 * Muestra poster + fecha por cada película vista recientemente.
 *
 * Uso:
 *   rv.layoutManager = LinearLayoutManager(ctx, HORIZONTAL, false)
 *   rv.adapter = RecentPostersAdapter(state.recentMovies)
 */
class RecentPostersAdapter(
    private val items: List<MovieCardUi>
) : RecyclerView.Adapter<RecentPostersAdapter.VH>() {

    inner class VH(val binding: ItemPosterRecentBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemPosterRecentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.binding.tvPosterDate.text = item.dateLabel
        // holder.binding.ivPosterRecent → cargar item.posterUrl con Glide/Picasso.
    }

    override fun getItemCount(): Int = items.size
}
