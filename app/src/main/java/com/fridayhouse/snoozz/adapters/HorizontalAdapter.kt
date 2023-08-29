package com.fridayhouse.snoozz.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.fridayhouse.snoozz.R
import com.fridayhouse.snoozz.data.HorizontalItem
import soup.neumorphism.NeumorphCardView

class HorizontalAdapter(private val items: List<HorizontalItem>, private val onItemClick: (Int) -> Unit) :
    RecyclerView.Adapter<HorizontalAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_horizontal, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d("Adapter", "Binding position $position")
        val item = items[position]
        holder.bind(item)

    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.itemTitle)
        private val iconImageView: ImageView = itemView.findViewById(R.id.itemIcon)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(position)
                }
            }
        }

        fun bind(item: HorizontalItem) {

            titleTextView.text = item.title
            iconImageView.setImageResource(item.iconResource)
        }
    }
}
