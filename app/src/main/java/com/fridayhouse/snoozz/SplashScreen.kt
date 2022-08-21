package com.fridayhouse.snoozz

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_splash_screen.*

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        fridayHouse.alpha = 0f
        fridayHouse.animate().setDuration(2500).alpha(1f).withEndAction {
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
            finish()
        }


        sleeping_panda.alpha = 0f
        sleeping_panda.animate().setDuration(2500).alpha(1f).withEndAction {

        }

            snoozzTitle.alpha = 0f
        snoozzTitle.animate().setDuration(2500).alpha(1f).withEndAction {

        }

    }
}