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

class SongAdapter @Inject constructor(
    private val glide: RequestManager
): RecyclerView.Adapter<SongAdapter.SongViewHolder>() {
    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private val diffCallback = object : DiffUtil.ItemCallback<sound>(){
        override fun areItemsTheSame(oldItem: sound, newItem: sound): Boolean {
            return oldItem.mediaId == newItem.mediaId
        }

        override fun areContentsTheSame(oldItem: sound, newItem: sound): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

  var sounds: List<sound>
  get() = differ.currentList
    set(value) = differ.submitList(value)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        return SongViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.list_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
       val sound = sounds[position]
        holder.itemView.apply {
            tvPrimary.text = sound.title
            tvSecondary.text = sound.subtitle
            glide.load(sound.imageUrl) .into(ivItemImage)

            setOnItemClickListener {
                onItemClickListener?.let { click ->
                    click(sound)
                }
            }
        }
    }

    private var onItemClickListener: ((sound) -> Unit)? = null
    fun setOnItemClickListener(listener: (sound) -> Unit) {
        onItemClickListener = listener
    }

    override fun getItemCount(): Int {
   return sounds.size
    }
}