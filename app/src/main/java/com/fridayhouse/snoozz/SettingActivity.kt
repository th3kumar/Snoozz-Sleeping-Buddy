package com.fridayhouse.snoozz

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.Toast
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {



        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        val openUrl: RelativeLayout = findViewById(R.id.buy_me_btn)
        openUrl.setOnClickListener{
          val openUrl = Intent(android.content.Intent.ACTION_VIEW)
            //here we will pass an URL to be opened
            openUrl.data = Uri.parse("https://www.buymeacoffee.com/mantukumar")
            startActivity(openUrl)
        }

        rating_btn.setOnClickListener{
            inAppReview()
        }

        aboutUs_btn.setOnClickListener{
            val i = Intent(this, AboutActivity::class.java)
            startActivity(i)
        }

        feedback_btn.setOnClickListener {
            val i = Intent(this, FeedbackActivity::class.java)
            startActivity(i)
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
                Log.d("Error: ", request.exception.toString())
                // There was some problem, continue regardless of the result.
            }
        }

    }
}