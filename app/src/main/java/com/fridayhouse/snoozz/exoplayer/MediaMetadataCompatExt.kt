package com.fridayhouse.snoozz.exoplayer

import android.support.v4.media.MediaMetadataCompat
import com.fridayhouse.snoozz.data.entities.sound

fun MediaMetadataCompat.toSong(): sound? {
    return description?.let {
        sound(
            it.mediaId ?: "",
            it.title.toString(),
            it.subtitle.toString(),
            it.mediaUri.toString(),
            it.iconUri.toString()
        )
    }
}