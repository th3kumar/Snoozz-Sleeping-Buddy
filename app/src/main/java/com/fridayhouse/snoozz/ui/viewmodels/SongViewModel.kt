package com.fridayhouse.snoozz.ui.viewmodels

import android.annotation.SuppressLint
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fridayhouse.snoozz.exoplayer.*
import com.fridayhouse.snoozz.others.Constants.UPDATE_PLAYER_POSITION_INTERVAL
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SongViewModel @ViewModelInject constructor(
    musicServiceConnection: MusicServiceConnection
) : ViewModel() {

    private val playbackState = musicServiceConnection.playBackState

    private  val _curSongDuration = MutableLiveData<Long>()
    val curSongDuration: LiveData<Long> = _curSongDuration

    private val _curPlayerPosition = MutableLiveData<Long>()
    val curPlayerPosition: LiveData<Long> = _curPlayerPosition

    init {
        updateCurrentplayerPostion()
    }


    private fun updateCurrentplayerPostion() {
        viewModelScope.launch {
            while(true) {
             val pos = playbackState.value?.currentPlaybackPosition
                if(curPlayerPosition.value != pos){
                    _curPlayerPosition.postValue(pos!!)
                    _curSongDuration.postValue(MusicService.curSoundDuration)
                }
                delay(UPDATE_PLAYER_POSITION_INTERVAL)
            }
        }
    }
}














