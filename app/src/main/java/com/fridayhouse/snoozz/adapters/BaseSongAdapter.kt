package com.fridayhouse.snoozz.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.fridayhouse.snoozz.data.entities.sound

abstract class BaseSongAdapter(
    private val layoutId: Int
) : RecyclerView.Adapter<BaseSongAdapter.SongViewHolder>() {
    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    protected val diffCallback = object : DiffUtil.ItemCallback<sound>() {
        override fun areItemsTheSame(oldItem: sound, newItem: sound): Boolean {
            return oldItem.mediaId == newItem.mediaId
        }

        override fun areContentsTheSame(oldItem: sound, newItem: sound): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    protected abstract var differ: AsyncListDiffer<sound>

    var sounds: List<sound>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        return SongViewHolder(
            LayoutInflater.from(parent.context).inflate(
                layoutId,
                parent,
                false
            )
        )
    }

    protected var onItemClickListener: ((sound) -> Unit)? = null
    fun setItemClickListener(listener: (sound) -> Unit) {
        onItemClickListener = listener
    }

    override fun getItemCount(): Int {
        return sounds.size
    }
}