package com.techxicn.dailymooovie.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.techxicn.dailymooovie.databinding.ItemCountryBarBinding
import com.techxicn.dailymooovie.model.CountryStatUi

/**
 * Adapter de la lista "MÁS VISTO" (World Exploration).
 * Cada fila: nombre del país + barra roja proporcional + conteo.
 *
 * La barra se escala respecto al país con más películas (maxCount),
 * de modo que el de mayor valor ocupa el 100% del ancho disponible.
 */
class CountryBarAdapter(
    private val items: List<CountryStatUi>
) : RecyclerView.Adapter<CountryBarAdapter.VH>() {

    // Máximo del dataset para normalizar la barra (evita división por cero).
    private val maxCount: Int = items.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1

    inner class VH(val binding: ItemCountryBarBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemCountryBarBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        with(holder.binding) {
            tvCountryName.text = item.name
            tvCountryCount.text = item.count.toString()
            // Barra: fracción del máximo, pivote a la izquierda.
            viewBar.scaleX = (item.count.toFloat() / maxCount).coerceIn(0f, 1f)
        }
    }

    override fun getItemCount(): Int = items.size
}
