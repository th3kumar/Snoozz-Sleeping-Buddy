package com.fridayhouse.snoozz

import android.app.Application
import android.content.Context
import com.fridayhouse.snoozz.provider.CastAPIProvider
import com.fridayhouse.snoozz.repository.SettingsRepository
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SnoozzApplication : Application() {


    companion object {
        /**
         * Convenience method that returns [SnoozzApplication] from the provided [context].
         */
        fun of(context: Context) {
            fun of(context: Context): SnoozzApplication = context.applicationContext as SnoozzApplication
        }
    }

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var castAPIProviderFactory: CastAPIProvider.Factory

    override fun onCreate() {
        super.onCreate()
        settingsRepository = SettingsRepository.newInstance(this)
    }


}
