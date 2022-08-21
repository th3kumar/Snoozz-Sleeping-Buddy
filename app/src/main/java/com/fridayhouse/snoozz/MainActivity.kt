package com.fridayhouse.snoozz

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.fridayhouse.snoozz.adapters.SwipeSongAdapter
import com.fridayhouse.snoozz.data.entities.sound
import com.fridayhouse.snoozz.exoplayer.isPlaying
import com.fridayhouse.snoozz.exoplayer.toSong
import com.fridayhouse.snoozz.others.Status
import com.fridayhouse.snoozz.ui.viewmodels.MainViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_splash_screen.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    @Inject
    lateinit var swipeSongAdapter: SwipeSongAdapter

    @Inject
    lateinit var glide: RequestManager

    private var curPlayingSong: sound? = null


    private var playbackState: PlaybackStateCompat? = null


    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        subscribeToObservers()

        snoozz_title_main.alpha = 0f
        snoozz_title_main.animate().setDuration(2000).alpha(1f).withEndAction{}
//            val i = Intent(this, MainActivity::class.java)
//            startActivity(i)
//            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
//            finish()



        vpSong.adapter = swipeSongAdapter

        vpSong.registerOnPageChangeCallback(object  : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if(playbackState?.isPlaying == true){
                    mainViewModel.playOrToggleSound(swipeSongAdapter.sounds[position])
                } else {
                    curPlayingSong = swipeSongAdapter.sounds[position]
                }
            }
        })

        ivPlayPause.setOnClickListener {
            curPlayingSong?.let {
                mainViewModel.playOrToggleSound(it, true)
            }
        }
           swipeSongAdapter.setItemClickListener {
               navHostFragment.findNavController().navigate(
                   R.id.globalActionToSongFragment
               )
           }
        navHostFragment.findNavController().addOnDestinationChangedListener  { _, destination, _ ->
            when(destination.id) {
                R.id.songFragments -> hideBottombar()
                R.id.homeFragment -> showBottombar()
                else -> showBottombar()
            }

        }


    }

    private fun hideBottombar() {
       ivCurSongImage.isVisible = false
    vpSong.isVisible = false
    ivPlayPause.isVisible = false
    }

    private fun showBottombar() {
        ivCurSongImage.isVisible = true
        vpSong.isVisible = true
        ivPlayPause.isVisible = true
    }



    private fun switchViewPagerToCurrentSong(sound: sound){
        val newItemIndex = swipeSongAdapter.sounds.indexOf(sound)
        if (newItemIndex != -1) {
            vpSong.currentItem = newItemIndex
            curPlayingSong = sound
        }
    }

    private fun subscribeToObservers(){
        mainViewModel.mediaItems.observe(this){
            it?.let { result ->
                when(result.status) {
                    Status.SUCCESS -> {
                        result.data?.let { sounds ->
                            swipeSongAdapter.sounds = sounds
                            if (sounds.isNotEmpty()) {
                                glide.load((curPlayingSong ?: sounds[0]).imageUrl).into(ivCurSongImage)
                            }
                            switchViewPagerToCurrentSong(curPlayingSong ?: return@observe)
                        }
                    }
                    Status.ERROR -> Unit
                    Status.LOADING -> Unit
                }

            }
        }

        mainViewModel.curPlayingSound.observe(this) {
            if(it == null) return@observe

            curPlayingSong = it.toSong()
            glide.load(curPlayingSong?.imageUrl).into(ivCurSongImage)
            switchViewPagerToCurrentSong(curPlayingSong ?: return@observe)
        }
        mainViewModel.playbackState.observe(this) {
            playbackState = it
            ivPlayPause.setImageResource(
                if (playbackState?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play
            )
        }
        mainViewModel.isConnected.observe(this){
            it?.getContentIfNotHandled()?.let { result ->
                when(result.status) {
                    Status.ERROR -> Snackbar.make(
                        rootLayout,
                        result.message ?: "An  Unknown error occured",
                        Snackbar.LENGTH_LONG
                    ).show()
                    else -> Unit
                }
            }
        }
        mainViewModel.networkError.observe(this){
            it?.getContentIfNotHandled()?.let { result ->
                when(result.status) {
                    Status.ERROR -> Snackbar.make(
                        rootLayout,
                        result.message ?: "An  Unknown error occured",
                        Snackbar.LENGTH_LONG
                    ).show()
                    else -> Unit
                }
            }
        }
    }


}












