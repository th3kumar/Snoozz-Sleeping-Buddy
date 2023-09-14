package com.fridayhouse.snoozz.activities

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.RequestManager
import com.fridayhouse.snoozz.R
import com.fridayhouse.snoozz.adapters.SwipeSongAdapter
import com.fridayhouse.snoozz.data.entities.sound
import com.fridayhouse.snoozz.exoplayer.isPlaying
import com.fridayhouse.snoozz.exoplayer.toSong
import com.fridayhouse.snoozz.others.Status
import com.fridayhouse.snoozz.ui.viewmodels.MainViewModel
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    private var isAnimationPlaying = false

    private lateinit var loadingAnimationView: LottieAnimationView


    private val requestCustomActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}


    private lateinit var appUpdateManager: AppUpdateManager
   private val updateType = AppUpdateType.IMMEDIATE

    private val REQUEST_CUSTOM_ACTIVITY = 1

    private lateinit var progressBar: ProgressBar

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

        loadingAnimationView = findViewById(R.id.loadingAnimationView)

        subscribeToObservers()
        appUpdateManager = AppUpdateManagerFactory.create(applicationContext)
        checkForAppUpdates()
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        val isTapTargetShown = sharedPreferences.getBoolean("isTapTargetShown", false)

        if(!isTapTargetShown) {
            // Delay showing the TapTargetView by 2 seconds
            Handler().postDelayed({
                showTapTargetView()
            }, 2000)
        }

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


        imageCustom.setOnClickListener {
            showLoadingAnimation()
            val intent = Intent(this, CustomActivity::class.java)
            startActivityForResult(intent, REQUEST_CUSTOM_ACTIVITY)
        }

        imageSetting.setOnClickListener {
            val i = Intent(this, SettingActivity::class.java)
            startActivity(i)
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

    private fun showTapTargetView() {
        val isTapTargetShown = sharedPreferences.getBoolean("isTapTargetShown", false)

        if (!isTapTargetShown) {
            val tapTarget = TapTarget.forView(imageCustom, "Music Creation: Your Unique Sound", "Tap to create captivating music that's uniquely yours.")
                .cancelable(true)
                .outerCircleColor(R.color.target_view_outer)
                .targetCircleColor(android.R.color.white)
                .titleTextColor(android.R.color.white)
                .descriptionTextColor(android.R.color.white)
                .transparentTarget(true)

            TapTargetSequence(this)
                .targets(tapTarget)
                .listener(object : TapTargetSequence.Listener {
                    override fun onSequenceFinish() {
                        // Update shared preferences when the sequence is finished
                        sharedPreferences.edit().putBoolean("isTapTargetShown", true).apply()

//                        // Launch the CustomActivity
//                        showLoadingAnimation()
//                        val intent = Intent(this@MainActivity, CustomActivity::class.java)
//                        startActivityForResult(intent, REQUEST_CUSTOM_ACTIVITY)
                    }

                    override fun onSequenceStep(lastTarget: TapTarget?, targetClicked: Boolean) {}

                    override fun onSequenceCanceled(lastTarget: TapTarget?) {
                        // Update shared preferences if the sequence is canceled
                        sharedPreferences.edit().putBoolean("isTapTargetShown", true).apply()
                    }
                })
                .start()
        }
    }


    private fun checkForAppUpdates() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener {info ->
            val isUpdateAvailable = info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
            val isUpdateAllowed = when(updateType) {
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



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CUSTOM_ACTIVITY) {
            hideLoadingAnimation()
        }

        if(requestCode == 123) {
            if(resultCode != RESULT_OK) {
                println("something went wrong updating...")
            }
        }
    }

    private fun hideLoadingAnimation() {
        loadingAnimationView.visibility = View.GONE
        //loadingAnimationView.pauseAnimation()
    }

    private fun showLoadingAnimation() {
        loadingAnimationView.visibility = View.VISIBLE
        //loadingAnimationView.playAnimation()
    }

    private fun hideBottombar() {
        iv_divider.isVisible = false
       ivCurSongImage.isVisible = false
    vpSong.isVisible = false
    ivPlayPause.isVisible = false
    }

    private fun showBottombar() {
        iv_divider.isVisible = true
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
                if (playbackState?.isPlaying == true) R.drawable.ic_round_pause else R.drawable.ic_play_round
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












