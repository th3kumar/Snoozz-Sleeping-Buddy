package com.fridayhouse.snoozz.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.RequestManager
import com.fridayhouse.snoozz.MediaPlayerService
import com.fridayhouse.snoozz.R
import com.fridayhouse.snoozz.adapters.SwipeSongAdapter
import com.fridayhouse.snoozz.data.entities.sound
import com.fridayhouse.snoozz.databinding.ActivityMainBinding
import com.fridayhouse.snoozz.exoplayer.isPlaying
import com.fridayhouse.snoozz.exoplayer.toSong
import com.fridayhouse.snoozz.others.Status
import com.fridayhouse.snoozz.repository.SettingsRepository
import com.fridayhouse.snoozz.ui.viewmodels.MainViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.ivCurSongImage
import kotlinx.android.synthetic.main.activity_main.ivPlayPause
import kotlinx.android.synthetic.main.activity_main.navHostFragment
import kotlinx.android.synthetic.main.activity_main.rootLayout
import kotlinx.android.synthetic.main.activity_main.vpSong
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var appUpdateManager: AppUpdateManager
    private val updateType = AppUpdateType.IMMEDIATE
    private val REQUEST_CUSTOM_ACTIVITY = 1
    private val mainViewModel: MainViewModel by viewModels()


    @Inject
    lateinit var swipeSongAdapter: SwipeSongAdapter

    @Inject
    lateinit var glide: RequestManager
    private var curPlayingSong: sound? = null
    private var playbackState: PlaybackStateCompat? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.navHostFragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_compose, R.id.navigation_settings
            )
        )
        navView.setupWithNavController(navController)

        // Add the destination changed listener here
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.songFragments -> {
                    hideBottomBar()
                    hideNavigationBar()
                }

                R.id.navigation_home -> {
                    showBottomBar()
                    showNavigationBar()
                }

                R.id.navigation_compose -> {
                    showBottomBar()
                    showNavigationBar()
                }

                else -> {
                    hideBottomBar()
                    showNavigationBar()
                }
            }
        }

        subscribeToObservers()
        appUpdateManager = AppUpdateManagerFactory.create(applicationContext)
        checkForAppUpdates()
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)


        binding.apply {
             //snoozzTitleMain.alpha = 0f
             //snoozzTitleMain.animate().setDuration(2000).alpha(1f).withEndAction {}
            vpSong.adapter = swipeSongAdapter
            vpSong.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    if (playbackState?.isPlaying == true) {
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
        }
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    private fun hideNavigationBar() {
        binding.apply {
            navView.isVisible = false
        }
    }

    private fun showNavigationBar() {
        binding.apply {
            navView.isVisible = true
        }
    }


    private fun checkForAppUpdates() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            val isUpdateAvailable = info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
            val isUpdateAllowed = when (updateType) {
                AppUpdateType.FLEXIBLE -> info.isFlexibleUpdateAllowed
                AppUpdateType.IMMEDIATE -> info.isImmediateUpdateAllowed
                else -> false
            }
            if (isUpdateAvailable && isUpdateAllowed) {
                appUpdateManager.startUpdateFlowForResult(
                    info,
                    updateType,
                    this,
                    123
                )
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CUSTOM_ACTIVITY) {
            hideLoadingAnimation()
        }

        if (requestCode == 123) {
            if (resultCode != RESULT_OK) {
                println("something went wrong updating...")
            }
        }
    }

    private fun hideLoadingAnimation() {
        binding.loadingAnimationView.visibility = View.GONE
    }

    private fun showLoadingAnimation() {
        binding.loadingAnimationView.visibility = View.VISIBLE
    }

    private fun hideBottomBar() {

        binding.MusicBar.isVisible = false

    }

    private fun showBottomBar() {
        binding.MusicBar.isVisible = true

    }

    private fun switchViewPagerToCurrentSong(sound: sound) {
        val newItemIndex = swipeSongAdapter.sounds.indexOf(sound)
        if (newItemIndex != -1) {
            vpSong.currentItem = newItemIndex
            curPlayingSong = sound
        }
    }

    private fun subscribeToObservers() {
        mainViewModel.mediaItems.observe(this) {
            it?.let { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        result.data?.let { sounds ->
                            swipeSongAdapter.sounds = sounds
                            if (sounds.isNotEmpty()) {
                                glide.load((curPlayingSong ?: sounds[0]).imageUrl)
                                    .into(ivCurSongImage)
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
            if (it == null) return@observe
            curPlayingSong = it.toSong()
            glide.load(curPlayingSong?.imageUrl).into(ivCurSongImage)
            switchViewPagerToCurrentSong(curPlayingSong ?: return@observe)
        }
        mainViewModel.playbackState.observe(this) {
            playbackState = it
            ivPlayPause.setImageResource(
                if (playbackState?.isPlaying == true) R.drawable.ic_round_pause else R.drawable.ic_play_round
            )
        }
        mainViewModel.isConnected.observe(this) {
            it?.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    Status.ERROR -> Snackbar.make(
                        rootLayout,
                        result.message ?: "An  Unknown error occured",
                        Snackbar.LENGTH_LONG
                    ).show()

                    else -> Unit
                }
            }
        }
        mainViewModel.networkError.observe(this) {
            it?.getContentIfNotHandled()?.let { result ->
                when (result.status) {
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