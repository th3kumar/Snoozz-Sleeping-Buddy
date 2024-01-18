package com.fridayhouse.snoozz.activities

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.palette.graphics.Palette
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.RequestManager
import com.fridayhouse.snoozz.R
import com.fridayhouse.snoozz.adapters.SwipeSongAdapter
import com.fridayhouse.snoozz.data.entities.sound
import com.fridayhouse.snoozz.databinding.ActivityMainBinding
import com.fridayhouse.snoozz.exoplayer.isPlaying
import com.fridayhouse.snoozz.exoplayer.toSong
import com.fridayhouse.snoozz.others.Status
import com.fridayhouse.snoozz.ui.viewmodels.MainViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.ivCurSongImage
import kotlinx.android.synthetic.main.activity_main.ivPlayPause
import kotlinx.android.synthetic.main.activity_main.navHostFragment
import kotlinx.android.synthetic.main.activity_main.rootLayout
import kotlinx.android.synthetic.main.activity_main.vpSong
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ParentActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences


    private val REQUEST_UPDATE = 100
    private val REQUEST_CUSTOM_ACTIVITY = 1
    private val mainViewModel: MainViewModel by viewModels()


    @Inject
    lateinit var swipeSongAdapter: SwipeSongAdapter

    @Inject
    lateinit var glide: RequestManager
    private var curPlayingSong: sound? = null
    private var playbackState: PlaybackStateCompat? = null


    fun getMutedColor(color: Int?): Int {
        if (color != null) {
            val hsv = FloatArray(3)
            Color.colorToHSV(color, hsv)

            // Decrease saturation and brightness to mute the color
            hsv[1] *= 0.8f // Decrease saturation by 20%
            hsv[2] *= 0.8f // Decrease brightness by 20%

            return Color.HSVToColor(hsv)
        }
        return Color.GRAY // Return a default grey color if the provided color is null
    }

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
                    hideBottomBar()
                    showNavigationBar()
                }

                R.id.navigation_breathe -> {
                    showBottomBar()
                    hideNavigationBar()
                }

                R.id.navigation_all_music -> {
                    showBottomBar()
                    hideNavigationBar()
                }
                else -> {
                    showBottomBar()
                    showNavigationBar()
                }
            }
        }

        mainViewModel.currentBitmap.observe(this) { bitmap ->
            // Check if the bitmap is not null
            bitmap?.let { currentBitmap ->
                // Perform color extraction logic using Palette library
                Palette.from(currentBitmap).generate { palette ->
                    // Extract the muted color from the palette
                    val vibrantSwatch = palette?.vibrantSwatch
                    val mutedSwatch = palette?.mutedSwatch
                    val mutedColor = getMutedColor(vibrantSwatch?.rgb ?: mutedSwatch?.rgb)
                    val darkMutedColor = getDarkMutedColor(vibrantSwatch?.rgb ?: mutedSwatch?.rgb)

                    val colorStateList = ColorStateList.valueOf(mutedColor)
                    val darkColorStateList = ColorStateList.valueOf(darkMutedColor)
                    navView.itemTextColor = colorStateList
                    navView.itemRippleColor = darkColorStateList
                    // Set the background color of the CardView
                }
            }
        }

        AppIntroActivity.maybeStart(this)
        subscribeToObservers()
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


    override fun onResume() {
        super.onResume()
    }
    private fun getDarkMutedColor(color: Int?): Int {
        if (color != null) {
            val hsv = FloatArray(3)
            Color.colorToHSV(color, hsv)

            // Decrease saturation and brightness to mute the color
            hsv[1] *= 0.4f // Decrease saturation by 60%
            hsv[2] *= 0.4f // Decrease brightness by 60%

            return Color.HSVToColor(hsv)
        }
        return Color.GRAY // Return a default grey color if the provided color is null
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

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CUSTOM_ACTIVITY) {
            hideLoadingAnimation()
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

        fun getMutedColor(color: Int?): Int {
            if (color != null) {
                val hsv = FloatArray(3)
                Color.colorToHSV(color, hsv)

                // Decrease saturation and brightness to mute the color
                hsv[1] *= 0.7f // Decrease saturation by 30%
                hsv[2] *= 0.7f // Decrease brightness by 30%

                return Color.HSVToColor(hsv)
            }
            return Color.GRAY // Return a default grey color if the provided color is null
        }

        fun setConstraintLayoutBackgroundColorFromImage(bitmap: Bitmap) {
            // Generate the palette asynchronously
            Palette.from(bitmap).generate { palette ->
                // Extract different swatches of colors
                val vibrantSwatch = palette?.vibrantSwatch
                val mutedSwatch = palette?.mutedSwatch
                val backgroundColor = getMutedColor(vibrantSwatch?.rgb ?: mutedSwatch?.rgb)
                // Set the background color of the ConstraintLayout
                binding.underMusicBar.setBackgroundColor(backgroundColor)
            }
        }

        mainViewModel.curPlayingSound.observe(this) {
            if (it == null) return@observe
            curPlayingSong = it.toSong()
            glide.load(curPlayingSong?.imageUrl).into(ivCurSongImage)
            // assigning colors to the music bar w.r.t imageColor
            val drawable = ivCurSongImage.drawable
            if (drawable is BitmapDrawable) {
                val bitmap = drawable.bitmap
                mainViewModel.setCurrentBitmap(bitmap) // Store the bitmap in ViewModel
                setConstraintLayoutBackgroundColorFromImage(bitmap)
            }
            switchViewPagerToCurrentSong(curPlayingSong ?: return@observe)
        }
        mainViewModel.playbackState.observe(this) {
            playbackState = it
           // if(playbackState?.isPlaying == false) songViewController.alpha = 0.4f else songViewController.alpha = 1f
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