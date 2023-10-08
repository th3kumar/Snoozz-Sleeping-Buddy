package com.fridayhouse.snoozz.playback.strategy

import android.content.Context
import androidx.media.AudioAttributesCompat
import com.fridayhouse.snoozz.model.Sound
import com.fridayhouse.snoozz.strategy.PlaybackStrategy
import com.fridayhouse.snoozz.strategy.PlaybackStrategyFactory

/**
 * [LocalPlaybackStrategyFactory] implements [PlaybackStrategyFactory] for creating [LocalPlaybackStrategy]
 * instances.
 */
class LocalPlaybackStrategyFactory(
  private val context: Context,
  private val audioAttributes: AudioAttributesCompat
) : PlaybackStrategyFactory {

  override fun newInstance(sound: Sound): PlaybackStrategy {
    return LocalPlaybackStrategy(context, audioAttributes, sound)
  }
}
