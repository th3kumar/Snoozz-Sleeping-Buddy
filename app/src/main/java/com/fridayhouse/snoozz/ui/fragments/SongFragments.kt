package com.fridayhouse.snoozz.ui.fragments

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.fridayhouse.snoozz.R
import com.fridayhouse.snoozz.data.entities.sound
import com.fridayhouse.snoozz.exoplayer.isPlaying
import com.fridayhouse.snoozz.exoplayer.toSong
import com.fridayhouse.snoozz.others.Status
import com.fridayhouse.snoozz.ui.viewmodels.MainViewModel
import com.fridayhouse.snoozz.ui.viewmodels.SongViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_song.ivPlayPauseDetail
import kotlinx.android.synthetic.main.fragment_song.ivSkip
import kotlinx.android.synthetic.main.fragment_song.ivSkipPrevious
import kotlinx.android.synthetic.main.fragment_song.ivSongImage
import kotlinx.android.synthetic.main.fragment_song.seekBar
import kotlinx.android.synthetic.main.fragment_song.tvCurTime
import kotlinx.android.synthetic.main.fragment_song.tvSongDuration
import kotlinx.android.synthetic.main.fragment_song.tvSongName
import kotlinx.android.synthetic.main.fragment_song_custom.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class SongFragments : Fragment(R.layout.fragment_song_custom) {

    @Inject
    lateinit var glide: RequestManager

    private lateinit var mainViewModel: MainViewModel

    private val songViewModel: SongViewModel by viewModels()


    private var curplayingSong: sound? = null

    private var playbackState: PlaybackStateCompat? = null

    private var shouldUpdateSeekbar = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        subscribeToObservers()


        ivPlayPauseDetail.setOnClickListener {
            curplayingSong?.let {
                mainViewModel.playOrToggleSound(it, true)
            }
        }



       seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
           override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
               if (fromUser) {
                   setCurPlayerTimeToTextView(progress.toLong())
               }
           }

           override fun onStartTrackingTouch(seekBar: SeekBar?) {
              shouldUpdateSeekbar = false

           }

           override fun onStopTrackingTouch(seekBar: SeekBar?) {
              seekBar?.let {
                  mainViewModel.seekTo(it.progress.toLong())
                  shouldUpdateSeekbar = true
              }
           }

       })

        ivSkipPrevious.setOnClickListener {
            mainViewModel.skipToPreviousSound()
        }
        ivSkip.setOnClickListener{
            mainViewModel.skipToNextSound()
        }
        imageShuffle.setOnClickListener {
            mainViewModel.skipToNextSound()
        }
        imageSearch.setOnClickListener {
            val text = "currently unavailable !"
            val duration = Toast.LENGTH_SHORT

            val toast = Toast.makeText(context, text, duration)
            toast.show()
        }
        imageMenu.setOnClickListener {
            val text = "currently in beta version !"
            val duration = Toast.LENGTH_SHORT

            val toast = Toast.makeText(context, text, duration)
            toast.show()
        }
        imageLoop.setOnClickListener{
            val text = " looped "
            val duration = Toast.LENGTH_SHORT

            val toast = Toast.makeText(context, text, duration)
            toast.show()
        }
    }



    private fun updateTitleAndSongImage(sound: sound) {
        val title = "${sound.title} - ${sound.subtitle}"
        tvSongName.text = title
        glide.load(sound.imageUrl).into(ivSongImage)
    }

    private fun subscribeToObservers() {
        mainViewModel.mediaItems.observe(viewLifecycleOwner){
            it?.let { result ->
                when(result.status){
                    Status.SUCCESS -> {
                       result.data?.let { sounds ->
                           if (curplayingSong == null && sounds.isNotEmpty()) {
                               curplayingSong = sounds[0]
                               updateTitleAndSongImage(sounds[0])
                           }
                       }
                    }
                    else -> Unit
                }
            }
        }
     mainViewModel.curPlayingSound.observe(viewLifecycleOwner) {
         if(it == null) return@observe
         curplayingSong = it.toSong()
         updateTitleAndSongImage(curplayingSong!!)
     }

        mainViewModel.playbackState.observe(viewLifecycleOwner) {
          playbackState = it
            ivPlayPauseDetail.setImageResource(
                if (playbackState?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play
            )
            seekBar.progress = it?.position?.toInt() ?: 0
        }

        songViewModel.curPlayerPosition.observe(viewLifecycleOwner) {
           if (shouldUpdateSeekbar) {
               seekBar.progress = it.toInt()
               setCurPlayerTimeToTextView(it)
           }
        }
        songViewModel.curSongDuration.observe(viewLifecycleOwner) {
            seekBar.max = it.toInt()
            val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
            tvSongDuration.text = dateFormat.format(it)
        }

    }

    private fun  setCurPlayerTimeToTextView(ms: Long) {
        val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
        tvCurTime.text = dateFormat.format(ms)
    }


}