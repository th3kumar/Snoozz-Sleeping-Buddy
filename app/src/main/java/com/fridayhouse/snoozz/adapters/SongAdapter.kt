package com.fridayhouse.snoozz.adapters

import android.graphics.Color
import androidx.recyclerview.widget.AsyncListDiffer
import com.bumptech.glide.RequestManager
import com.fridayhouse.snoozz.R
import kotlinx.android.synthetic.main.list_item.view.ivItemImage
import kotlinx.android.synthetic.main.list_item.view.tvPrimary
import kotlinx.android.synthetic.main.list_item.view.tvSecondary
import kotlinx.android.synthetic.main.list_item_exp.view.songCardView
import javax.inject.Inject

class  SongAdapter @Inject constructor(
    private val glide: RequestManager
) : BaseSongAdapter(R.layout.list_item_exp) {


    val darkShadowDarkColor = Color.parseColor("#0F0F12")
    val lightShadowDarkColor = Color.parseColor("#1B1B1C")
    var mutedLightShadow: Int = lightShadowDarkColor
    var mutedDarkShadow: Int = darkShadowDarkColor

    override var differ = AsyncListDiffer(this, diffCallback)

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val sound = sounds[position]
        holder.itemView.apply {
            tvPrimary.text = sound.title
            tvSecondary.text = sound.subtitle
            glide.load(sound.imageUrl).into(ivItemImage)

            songCardView.setShadowColorLight(mutedLightShadow)
            songCardView.setShadowColorDark(mutedDarkShadow)

            setOnClickListener {
                onItemClickListener?.let { click ->
                    click(sound)
                }
            }
        }
    }
}