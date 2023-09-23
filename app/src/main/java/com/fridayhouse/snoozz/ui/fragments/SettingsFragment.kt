package com.fridayhouse.snoozz.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.fridayhouse.snoozz.R
import com.fridayhouse.snoozz.activities.AboutActivity
import com.fridayhouse.snoozz.databinding.FragmentSettingsBinding
import com.google.android.play.core.review.ReviewManagerFactory


class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            buyMeBtn.setOnClickListener {
                val openUrl = Intent(Intent.ACTION_VIEW)
                // here we will pass a URL to be opened
                openUrl.data = Uri.parse("https://www.buymeacoffee.com/mantukumar")
                startActivity(openUrl)
            }
            ratingBtn.setOnClickListener {
                inAppReview()
            }
            aboutUsBtn.setOnClickListener {
                val i = Intent(requireContext(), AboutActivity::class.java)
                startActivity(i)
            }
        }
    }

    private fun inAppReview() {
        val reviewManager = ReviewManagerFactory.create(requireContext())
        val requestReviewFlow = reviewManager.requestReviewFlow()
        requestReviewFlow.addOnCompleteListener { request ->
            if (request.isSuccessful) {
                // We got the ReviewInfo object
                val reviewInfo = request.result
                val flow = reviewManager.launchReviewFlow(requireActivity(), reviewInfo)
                flow.addOnCompleteListener {
                    // The flow has finished. The API does not indicate whether the user
                    // reviewed or not, or even whether the review dialog was shown. Thus, no
                    // matter the result, we continue our app flow.
                    val text = " Reviewed "
                    val duration = Toast.LENGTH_SHORT
                    val toast = Toast.makeText(requireContext(), text, duration)
                    toast.show()
                }
            } else {
                // no-op
            }
        }
    }
}
