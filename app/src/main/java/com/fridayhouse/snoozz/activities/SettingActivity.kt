package com.fridayhouse.snoozz.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fridayhouse.snoozz.databinding.ActivitySettingBinding
import com.google.android.play.core.review.ReviewManagerFactory

class SettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            buyMeBtn.setOnClickListener {
                val openUrl = Intent(Intent.ACTION_VIEW)
                //here we will pass an URL to be opened
                openUrl.data = Uri.parse("https://www.buymeacoffee.com/mantukumar")
                startActivity(openUrl)
            }
            ratingBtn.setOnClickListener {
                inAppReview()
            }
            aboutUsBtn.setOnClickListener {
                val i = Intent(this@SettingActivity, AboutActivity::class.java)
                startActivity(i)
            }
        }
    }

    private fun inAppReview() {
        val reviewManager = ReviewManagerFactory.create(this)
        val requestReviewFlow = reviewManager.requestReviewFlow()
        requestReviewFlow.addOnCompleteListener { request ->
            if (request.isSuccessful) {
                // We got the ReviewInfo object
                val reviewInfo = request.result
                val flow = reviewManager.launchReviewFlow(this, reviewInfo)
                flow.addOnCompleteListener {
                    // The flow has finished. The API does not indicate whether the user
                    // reviewed or not, or even whether the review dialog was shown. Thus, no
                    // matter the result, we continue our app flow.
                    val text = " Reviewed "
                    val duration = Toast.LENGTH_SHORT
                    val toast = Toast.makeText(this, text, duration)
                    toast.show()
                }
            } else {
                // no-op
            }
        }
    }
}