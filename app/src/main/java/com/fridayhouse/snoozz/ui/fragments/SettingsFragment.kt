package com.fridayhouse.snoozz.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.fridayhouse.snoozz.R
import com.fridayhouse.snoozz.activities.AboutActivity
import com.fridayhouse.snoozz.databinding.FragmentSettingsBinding
import com.fridayhouse.snoozz.others.Constants
import com.fridayhouse.snoozz.repository.PresetRepository
import com.fridayhouse.snoozz.repository.SettingsRepository
import com.fridayhouse.snoozz.utilities.PrefrenceUtils
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.gson.JsonIOException
import com.google.gson.JsonSyntaxException
import kotlinx.android.synthetic.main.fragment_settings.dark_mode_switch
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


class SettingsFragment : Fragment() {

    companion object {
        private val TAG = SettingsFragment::class.simpleName
    }

    private lateinit var binding: FragmentSettingsBinding

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var presetRepository: PresetRepository

    private val createDocumentActivityLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument(),
        this::onCreateDocumentResult
    )


    private val openDocumentActivityLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument(),
        this::onOpenDocumentResult
    )

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun onCreateDocumentResult(result: Uri?) {
        var success = false
        try {
            if (result == null) { // inside try to force run finally block.
                return
            }

            requireContext().contentResolver.openFileDescriptor(result, "w")?.use {
                val os = FileOutputStream(it.fileDescriptor)
                os.channel.truncate(0L)
                presetRepository.writeTo(os)
                os.close()
            }

            success = true
            showSnackBar(R.string.export_presets_successful)
        } catch (e: Throwable) {
            Log.w(TAG, "failed to export saved presets", e)
            when (e) {
                is FileNotFoundException,
                is IOException,
                is JsonIOException -> {
                    showSnackBar(R.string.failed_to_write_file)
                }
                else -> throw e
            }
        } finally {
           // analyticsProvider.logEvent("presets_export_complete", bundleOf("success" to success))
        }
    }
    private fun showSnackBar(@StringRes message: Int) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG)
            .setAction(R.string.dismiss) { }
            .show()
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun onOpenDocumentResult(result: Uri?) {
        var success = false
        try {
            if (result == null) { // inside try to force run finally block.
                return
            }

            requireContext().contentResolver.openFileDescriptor(result, "r")?.use {
                presetRepository.readFrom(FileInputStream(it.fileDescriptor))
            }
            success = true
            showSnackBar(R.string.import_presets_successful)
        } catch (e: Throwable) {
            Log.i(TAG, e.stackTraceToString())
            when (e) {
                is FileNotFoundException,
                is IOException,
                is JsonIOException -> {
                    showSnackBar(R.string.failed_to_read_file)
                }
                is JsonSyntaxException,
                is IllegalArgumentException -> showSnackBar(R.string.invalid_import_file_format)
                else -> throw e
            }
        } finally {
           // analyticsProvider.logEvent("presets_import_complete", bundleOf("success" to success))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingsRepository = SettingsRepository.newInstance(requireContext())
        presetRepository = PresetRepository.newInstance(requireContext())

        binding.apply {
            buyMeBtn.setOnClickListener {
                val openUrl = Intent(Intent.ACTION_VIEW)
                // here we will pass a URL to be opened
                openUrl.data = Uri.parse("https://www.buymeacoffee.com/mantukumar")
                startActivity(openUrl)
            }

            importBtn.setOnClickListener {
                openDocumentActivityLauncher.launch(arrayOf("application/json"))
            }

            exportBtn.setOnClickListener {
                createDocumentActivityLauncher.launch("snoozz-saved-presets.json")
            }


//            audioFocusSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
//                val ignoreAudioFocusChanges = shouldIgnoreAudioFocusChanges()
//
//                // Update the preference when the switch state changes
//                if (ignoreAudioFocusChanges != isChecked) {
//                    setIgnoreAudioFocusChanges(isChecked)
//                }
//
//                // Now, you can use the updated value as needed.
//                if (isChecked) {
//                    // Do something when the switch is ON
//                } else {
//                    // Do something when the switch is OFF
//                }
//            }
            ratingBtn.setOnClickListener {
                inAppReview()
            }
            aboutUsBtn.setOnClickListener {
                val i = Intent(requireContext(), AboutActivity::class.java)
                startActivity(i)
            }
        }

        val isDarkMode = (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
        dark_mode_switch.isChecked = isDarkMode

        dark_mode_switch.setOnCheckedChangeListener { _, isChecked ->
            toggleDayNightMode(isChecked)
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }

//    private fun setIgnoreAudioFocusChanges(checked: Boolean) {
//        val prefsEditor = prefs.edit()
//        prefsEditor.putBoolean(
//            context?.getString(R.string.ignore_audio_focus_changes_key),
//            shouldIgnore
//        )
//        prefsEditor.apply()
//    }

    private fun toggleDayNightMode(isDesabled: Boolean) {
        PrefrenceUtils.insertDataInBoolean(context,Constants.DARK_MODE_ENABLED, isDesabled)
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
