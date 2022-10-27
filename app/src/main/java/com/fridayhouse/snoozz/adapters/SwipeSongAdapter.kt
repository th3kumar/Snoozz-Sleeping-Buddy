package com.fridayhouse.snoozz.adapters

import androidx.recyclerview.widget.AsyncListDiffer
import com.fridayhouse.snoozz.R
import kotlinx.android.synthetic.main.list_item.view.*
import kotlinx.android.synthetic.main.list_item.view.tvPrimary
import kotlinx.android.synthetic.main.swipe_item.view.*

class SwipeSongAdapter: BaseSongAdapter(R.layout.swipe_item) {

    override var differ = AsyncListDiffer(this,diffCallback)

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
       val sound = sounds[position]
        holder.itemView.apply {
            //val text = "${sound.title} - ${sound.subtitle}"
            val text = "${sound.title}"
            val textD = "${sound.subtitle}"
            tvPrimary.text = text
            tvDescription.text = textD
            setOnClickListener {
                onItemClickListener?.let { click ->
                    click(sound)
                }
            }
        }
    }


}