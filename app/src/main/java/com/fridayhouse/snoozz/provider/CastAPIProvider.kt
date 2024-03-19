package com.fridayhouse.snoozz.provider

import android.content.Context
import android.view.Menu
import androidx.annotation.StringRes
import androidx.media.VolumeProviderCompat
import com.fridayhouse.snoozz.strategy.PlaybackStrategyFactory


/**
 * [CastAPIProvider] is an abstract declaration of the non-free Google Cast APIs that are used in the app.
 * It effectively hides upstream classes from its callers. This is ensure that F-Droid variant
 * remains free of non-free Google Cast API dependency.
 */
interface CastAPIProvider {

  /**
   * Adds a menu item to switch between local and cast playback to the given [menu].
   */
  fun addMenuItem(menu: Menu, @StringRes titleResId: Int)

  /**
   * Returns a [PlaybackStrategyFactory] that plays media on a cast device.
   */
  fun getPlaybackStrategyFactory(): PlaybackStrategyFactory

  /**
   * Returns a [VolumeProviderCompat] that controls volume of the cast device.
   */
  fun getVolumeProvider(): VolumeProviderCompat

  /**
   * Registers on cast session begin callback.
   */
  fun onSessionBegin(callback: () -> Unit)

  /**
   * Registers on cast session end callback.
   */
  fun onSessionEnd(callback: () -> Unit)

  /**
   * Clears all registered session callbacks.
   */
  fun clearSessionCallbacks()

  /**
   * A [Factory] to create new concrete [CastAPIProvider] instances.
   */
  interface Factory {
    fun newInstance(context: Context): CastAPIProvider
  }
}
