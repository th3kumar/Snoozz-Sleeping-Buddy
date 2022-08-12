package com.fridayhouse.snoozz.exoplayer

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fridayhouse.snoozz.others.Constants.NETWORK_ERROR
import com.fridayhouse.snoozz.others.Events
import com.fridayhouse.snoozz.others.Resource

class MusicServiceConnection (
    context: Context
        ) {
    private val _isConnected = MutableLiveData<Events<Resource<Boolean>>>()
    val isConnected: LiveData<Events<Resource<Boolean>>> = _isConnected

    private val _networkError = MutableLiveData<Events<Resource<Boolean>>>()
    val networkError: LiveData<Events<Resource<Boolean>>> = _networkError

    private val _playBackState = MutableLiveData<PlaybackStateCompat?>()
    val playBackState: LiveData<PlaybackStateCompat?> = _playBackState

    private val _curPlayingSound = MutableLiveData<MediaMetadataCompat?>()
    val curPlayingSound: LiveData<MediaMetadataCompat?> = _curPlayingSound

    lateinit var mediaController: MediaControllerCompat

    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(context)

    private val mediaBrowser = MediaBrowserCompat(
        context,
        ComponentName(
            context,
            MusicService::class.java
        ),
        mediaBrowserConnectionCallback,
        null
    ).apply { connect() }

    val transportControls : MediaControllerCompat.TransportControls
         get() = mediaController.transportControls

    fun subscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.subscribe(parentId, callback)
    }
    fun unsubscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback)
    {
        mediaBrowser.unsubscribe(parentId, callback)
    }

    private inner class MediaBrowserConnectionCallback(
        private val context: Context
    ) : MediaBrowserCompat.ConnectionCallback(){

        override fun onConnected() {
            mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken ).apply {
                registerCallback(MediaContollerCallback())
            }
            _isConnected.postValue(Events(Resource.success(true)))
        }

        override fun onConnectionSuspended() {
            _isConnected.postValue(Events(Resource.error(
                "the connection was  suspended", false
            )))
        }

        override fun onConnectionFailed() {
             _isConnected.postValue(Events(Resource.error(
                 "Couldn't connect to media Browser", false
             )))
        }
    }

    private inner class MediaContollerCallback : MediaControllerCompat.Callback(){

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            _playBackState.postValue(state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
           _curPlayingSound.postValue(metadata)
        }

        override fun onSessionEvent(event: String?, extras: Bundle?) {
            super.onSessionEvent(event, extras)
            when(event){
                NETWORK_ERROR -> _networkError.postValue(
                    Events(
                        Resource.error(
                            "Couldn't connect to the server. Please check your internet connection.",
                            null
                        )
                    )
                )
            }

        }

        override fun onSessionDestroyed() {
            mediaBrowserConnectionCallback.onConnectionSuspended()
        }
    }

}