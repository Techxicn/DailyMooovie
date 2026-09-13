package com.techxicn.dailymooovie.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.techxicn.dailymooovie.R
import com.techxicn.dailymooovie.databinding.ItemPassportStampBinding
import com.techxicn.dailymooovie.model.PassportStampUi

/**
 * Adapter del grid de sellos (Movie Passport, GridLayoutManager 3 columnas).
 * Cada sello alterna su aspecto según esté desbloqueado o pendiente.
 */
class PassportStampAdapter(
    private val items: List<PassportStampUi>
) : RecyclerView.Adapter<PassportStampAdapter.VH>() {

    inner class VH(val binding: ItemPassportStampBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemPassportStampBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val ctx = holder.binding.root.context
        with(holder.binding) {
            if (item.unlocked) {
                tvStampCode.text = item.code
                tvStampCity.text = item.city
                tvStampCity.visibility = android.view.View.VISIBLE
                ivStampCircle.setBackgroundResource(R.drawable.shape_stamp_circle_active)
                tvStampCode.setTextColor(ContextCompat.getColor(ctx, R.color.colorAccentRed))
            } else {
                tvStampCode.text = "•••"
                tvStampCity.visibility = android.view.View.INVISIBLE
                ivStampCircle.setBackgroundResource(R.drawable.shape_stamp_circle_locked)
                tvStampCode.setTextColor(ContextCompat.getColor(ctx, R.color.colorOnSurfaceFaint))
            }
        }
    }

    override fun getItemCount(): Int = items.size
}
