package com.fridayhouse.snoozz.provider

import android.app.Activity
import android.content.Context
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.google.android.play.core.ktx.isImmediateUpdateAllowed

class AppUpdateManager(private val context: Context) {

    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(context)
    private val updateType = AppUpdateType.FLEXIBLE // or IMMEDIATE based on your need

    fun checkForAppUpdates(onUpdateAvailable: (updateInfo: AppUpdateInfo) -> Unit) {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            val isUpdateAvailable = info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
            val isUpdateAllowed = when (updateType) {
                AppUpdateType.FLEXIBLE -> info.isFlexibleUpdateAllowed
                AppUpdateType.IMMEDIATE -> info.isImmediateUpdateAllowed
                else -> false
            }
            if (isUpdateAvailable && isUpdateAllowed) {
                onUpdateAvailable(info)
            }
        }
    }

    fun startUpdateFlowForResult(updateInfo: AppUpdateInfo, requestCode: Int) {
        appUpdateManager.startUpdateFlowForResult(
            updateInfo,
            updateType,
            context as Activity, // Assuming context is an Activity
            requestCode
        )
    }

}
