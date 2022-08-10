package com.fridayhouse.snoozz.exoplayer

import android.media.MediaMetadata.*
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import androidx.core.net.toUri
import com.fridayhouse.snoozz.data.remote.MusicDatabase
import com.fridayhouse.snoozz.exoplayer.State.*
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FirebaseMusicSource  @Inject constructor(
    private var musicDatabase: MusicDatabase
){

    var sounds = emptyList<MediaMetadataCompat>()

    suspend fun fetchMediaData() {
      state = STATE_INITIALIZING
        getAllSounds()
        state = STATE_INITIALIZED
    }

    suspend fun getAllSounds()  = withContext(Dispatchers.IO){
        val allSounds = musicDatabase.getAllSounds()
        sounds = allSounds.map { sound ->
            MediaMetadataCompat.Builder()
                .putString(METADATA_KEY_ARTIST, sound.subtitle)
                .putString(METADATA_KEY_MEDIA_ID, sound.mediaId)
                .putString(METADATA_KEY_TITLE, sound.title)
                .putString(METADATA_KEY_DISPLAY_TITLE, sound.title)
                .putString(METADATA_KEY_DISPLAY_ICON_URI, sound.imageUrl)
                .putString(METADATA_KEY_MEDIA_URI,sound.audioUrl)
                .putString(METADATA_KEY_ALBUM_ART_URI, sound.imageUrl)
                .putString(METADATA_KEY_DISPLAY_SUBTITLE, sound.subtitle)
                .putString(METADATA_KEY_DISPLAY_DESCRIPTION,sound.subtitle)
                .build()




        }
    }

    fun asMediaSource(dataSourceFactory: DefaultDataSourceFactory) : ConcatenatingMediaSource{
       val concatenatingMediaSource = ConcatenatingMediaSource()
        sounds.forEach { sound->
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(
                    MediaItem.fromUri(
                        sound.getString(METADATA_KEY_MEDIA_URI).toUri()
                    )
                )
                concatenatingMediaSource.addMediaSource(mediaSource)
        }
        return concatenatingMediaSource
    }

    fun asMediaItems() = sounds.map { sound->
        val desc = MediaDescriptionCompat.Builder()
            .setMediaUri(sound.getString(METADATA_KEY_MEDIA_URI).toUri())
            .setTitle(sound.description.title)
            .setSubtitle(sound.description.subtitle)
            .setMediaId(sound.description.mediaId)
            .setIconUri(sound.description.iconUri)
            .build()

        MediaBrowserCompat.MediaItem(desc, FLAG_PLAYABLE)

    }.toMutableList()

    private val onReadyListeners = mutableListOf<(Boolean) -> Unit>()

    private var state: State = STATE_CREATED

    set(value){
        if(value == STATE_INITIALIZED || value == STATE_ERROR){
            synchronized(onReadyListeners) {
                field = value
                onReadyListeners.forEach { listener ->
                    listener(state == STATE_INITIALIZED)
                }
            }
        } else {
            field = value
        }
    }

    fun whenReady(action: (Boolean) -> Unit): Boolean {
        if(state == STATE_CREATED || state == STATE_INITIALIZING) {
            onReadyListeners += action
            return false
        } else {
            action(state == STATE_INITIALIZED)
            return true
        }
    }
}

//private fun ProgressiveMediaSource.Factory.createMediaSource(toUri: Uri): ProgressiveMediaSource {
//
//}

enum class State{
    STATE_CREATED,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR
}