package com.fridayhouse.snoozz.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.fridayhouse.snoozz.R
import com.fridayhouse.snoozz.data.entities.sound
import kotlinx.android.synthetic.main.list_item.view.*
import javax.inject.Inject

class SwipeSongAdapter: BaseSongAdapter(R.layout.swipe_item) {

    override var differ = AsyncListDiffer(this,diffCallback)

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
       val sound = sounds[position]
        holder.itemView.apply {
            val text = "${sound.title} - ${sound.subtitle}"
            tvPrimary.text = text
            setOnClickListener {
                onItemClickListener?.let { click ->
                    click(sound)
                }
            }
        }
    }


}