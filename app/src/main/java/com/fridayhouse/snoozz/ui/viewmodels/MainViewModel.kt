package com.fridayhouse.snoozz.ui.viewmodels

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fridayhouse.snoozz.SnoozzApplication
import com.fridayhouse.snoozz.data.entities.sound
import com.fridayhouse.snoozz.exoplayer.MusicServiceConnection
import com.fridayhouse.snoozz.exoplayer.isPlayEnabled
import com.fridayhouse.snoozz.exoplayer.isPlaying
import com.fridayhouse.snoozz.exoplayer.isPrepared
import com.fridayhouse.snoozz.others.Constants.MEDIA_ROOT_ID
import com.fridayhouse.snoozz.others.Resource
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val musicServiceConnection: MusicServiceConnection,
    private val application: Application
) : ViewModel() {
    private val _mediaItems = MutableLiveData<Resource<List<sound>>>()
    val mediaItems: LiveData<Resource<List<sound>>> = _mediaItems
    val isConnected = musicServiceConnection.isConnected
    val networkError = musicServiceConnection.networkError
    val curPlayingSound = musicServiceConnection.curPlayingSound
    val playbackState = musicServiceConnection.playBackState

    init {
        _mediaItems.postValue(Resource.loading(null))
        musicServiceConnection.subscribe(
            MEDIA_ROOT_ID,
            object : MediaBrowserCompat.SubscriptionCallback() {

                override fun onChildrenLoaded(
                    parentId: String,
                    children: MutableList<MediaBrowserCompat.MediaItem>
                ) {
                    super.onChildrenLoaded(parentId, children)
                    val items = children.map {
                        sound(
                            it.mediaId!!,
                            it.description.title.toString(),
                            it.description.subtitle.toString(),
                            it.description.mediaUri.toString(),
                            it.description.iconUri.toString()
                        )
                    }
                    _mediaItems.postValue(Resource.success(items))
                }
            })
    }

    fun skipToNextSound() {
        musicServiceConnection.transportControls.skipToNext()
    }

    fun skipToPreviousSound() {
        musicServiceConnection.transportControls.skipToPrevious()
    }

    fun seekTo(pos: Long) {
        musicServiceConnection.transportControls.seekTo(pos)
    }

    // LiveData to hold the current bitmap
    private val _currentBitmap = MutableLiveData<Bitmap>()
    val currentBitmap: LiveData<Bitmap>
        get() = _currentBitmap

    // Function to set the bitmap value
    fun setCurrentBitmap(bitmap: Bitmap) {
        _currentBitmap.value = bitmap
    }

    fun isEmojiSelectionMadeToday(): Boolean {
        val sharedPreferences = application.getSharedPreferences("EmojiPrefs", Context.MODE_PRIVATE)
        val lastSelectionDate = sharedPreferences.getString("lastSelectionDate", "")
        val currentDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        return lastSelectionDate == currentDate
    }


    fun playOrToggleSound(mediaItem: sound, toggle: Boolean = false) {
        val isPrepared = playbackState.value?.isPrepared ?: false
        if (isPrepared && mediaItem.mediaId ==
            curPlayingSound.value?.getString(METADATA_KEY_MEDIA_ID)
        ) {
            playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> if (toggle) musicServiceConnection.transportControls.pause()
                    playbackState.isPlayEnabled -> musicServiceConnection.transportControls.play()
                    else -> Unit
                }
            }
        } else {
            musicServiceConnection.transportControls.playFromMediaId(mediaItem.mediaId, null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        musicServiceConnection.unsubscribe(
            MEDIA_ROOT_ID,
            object : MediaBrowserCompat.SubscriptionCallback() {})
    }
}