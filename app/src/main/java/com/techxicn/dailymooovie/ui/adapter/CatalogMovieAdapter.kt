package com.techxicn.dailymooovie.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.techxicn.dailymooovie.R
import com.techxicn.dailymooovie.databinding.ItemCatalogMovieBinding
import com.techxicn.dailymooovie.model.CatalogMovieUi

/**
 * Adapter del grid de FILM CATALOG (GridLayoutManager, 3 columnas).
 * Cada celda: fecha + poster + título truncado + estado (Watched / Not Watched).
 *
 * Se llena con las películas del mes seleccionado provistas por la lógica.
 * onItemClick queda listo para abrir la pantalla FILM al tocar un poster.
 */
class CatalogMovieAdapter(
    private val items: List<CatalogMovieUi>,
    private val onItemClick: (CatalogMovieUi) -> Unit = {},
    private val onItemLongClick: (CatalogMovieUi) -> Unit = {}
) : RecyclerView.Adapter<CatalogMovieAdapter.VH>() {

    inner class VH(val binding: ItemCatalogMovieBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemCatalogMovieBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val ctx = holder.binding.root.context
        with(holder.binding) {
            tvItemDate.text = item.dateLabel
            tvItemTitle.text = item.title
            tvItemStatus.text = ctx.getString(
                if (item.watched) R.string.catalog_status_watched
                else R.string.catalog_status_not_watched
            )
            root.setOnClickListener { onItemClick(item) }
            root.setOnLongClickListener {
                onItemLongClick(item)
                true
            }
            // ivItemPoster → cargar item.posterUrl con Glide/Picasso cuando exista.
        }
    }

    override fun getItemCount(): Int = items.size
}
