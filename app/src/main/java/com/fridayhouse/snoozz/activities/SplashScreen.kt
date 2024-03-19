package com.fridayhouse.snoozz.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import com.fridayhouse.snoozz.adapters.SongAdapter
import com.fridayhouse.snoozz.databinding.ActivitySplashScreenBinding
import com.fridayhouse.snoozz.others.Constants
import com.fridayhouse.snoozz.others.Status
import com.fridayhouse.snoozz.ui.viewmodels.MainViewModel
import com.fridayhouse.snoozz.utilities.PrefrenceUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_splash_screen.fridayHouse
import kotlinx.android.synthetic.main.activity_splash_screen.snoozzTitle
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

@AndroidEntryPoint
class SplashScreen : AppCompatActivity() {
    private lateinit var binding: ActivitySplashScreenBinding
    private lateinit var mainViewModel: MainViewModel

    @Inject
    lateinit var songAdapter: SongAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        val isDarkMode = PrefrenceUtils.retriveDataInBoolean(this, Constants.DARK_MODE_ENABLED)
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        fridayHouse.alpha = 0f
        fridayHouse.animate().setDuration(1750).alpha(1f).withEndAction {
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
        checkDataLoadingStatus()
        snoozzTitle.alpha = 0f
        snoozzTitle.animate().setDuration(1500).alpha(1f).withEndAction {}
    }

    private fun checkDataLoadingStatus(){
        val future = CompletableFuture<Boolean>()
        mainViewModel.mediaItems.observe(this) { result ->
            when (result.status) {
                Status.SUCCESS -> {
                    result.data?.let { sound ->
                        songAdapter.sounds = sound
                    }
                    future.complete(true)
                }
                Status.ERROR -> future.complete(false)
                Status.LOADING -> Unit
            }
        }
            if (!future.isDone) {
                future.complete(false)
            }
    }
}