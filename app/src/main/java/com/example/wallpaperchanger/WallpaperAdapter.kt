package com.example.wallpaperchanger

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class WallpaperAdapter(
    private val items: List<WallpaperItem>,
    private val onClick: (WallpaperItem, Int) -> Unit,
    private val onLongClickDelete: (WallpaperItem.UserPhoto) -> Unit
) : RecyclerView.Adapter<WallpaperAdapter.VH>() {

    private var selectedPosition = -1

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val imgThumb: ImageView = view.findViewById(R.id.imgThumb)
        val imgCheck: ImageView = view.findViewById(R.id.imgSelectedCheck)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wallpaper, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        when (val item = items[position]) {
            is WallpaperItem.Builtin -> holder.imgThumb.setImageResource(item.drawableRes)
            is WallpaperItem.UserPhoto -> holder.imgThumb.setImageURI(item.uri)
        }
        holder.imgCheck.visibility = if (position == selectedPosition) View.VISIBLE else View.GONE
        holder.itemView.setOnClickListener {
            val previous = selectedPosition
            selectedPosition = position
            notifyItemChanged(previous)
            notifyItemChanged(selectedPosition)
            onClick(item, position)
        }

        holder.itemView.setOnLongClickListener {
            if (item is WallpaperItem.UserPhoto) {
                onLongClickDelete(item)
                true
            } else {
                false
            }
        }
    }

    override fun getItemCount(): Int = items.size
}
