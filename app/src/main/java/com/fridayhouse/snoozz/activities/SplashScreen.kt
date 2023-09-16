package com.fridayhouse.snoozz.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fridayhouse.snoozz.databinding.ActivitySplashScreenBinding
import kotlinx.android.synthetic.main.activity_splash_screen.fridayHouse
import kotlinx.android.synthetic.main.activity_splash_screen.snoozzTitle

class SplashScreen : AppCompatActivity() {
    private lateinit var binding: ActivitySplashScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fridayHouse.alpha = 0f
        fridayHouse.animate().setDuration(2000).alpha(1f).withEndAction {
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
        snoozzTitle.alpha = 0f
        snoozzTitle.animate().setDuration(2000).alpha(1f).withEndAction {
        }
    }
}