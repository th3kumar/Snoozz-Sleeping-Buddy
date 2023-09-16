package com.fridayhouse.snoozz.data

data class HorizontalItem(
    val title: String,
    val iconResource: Int,
    val animationResource: Int,
    var isPressed: Boolean = false
)