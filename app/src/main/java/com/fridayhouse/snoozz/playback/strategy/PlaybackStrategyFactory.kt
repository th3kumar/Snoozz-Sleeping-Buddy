package com.fridayhouse.snoozz.strategy

import com.fridayhouse.snoozz.model.Sound


/**
 * [PlaybackStrategyFactory] declares an abstract factory that can be implemented to create new
 * [PlaybackStrategy] instances.
 */
interface PlaybackStrategyFactory {

  /**
   * Returns a instance of a concrete implementation of [PlaybackStrategy].
   */
  fun newInstance(sound: Sound): PlaybackStrategy
}
