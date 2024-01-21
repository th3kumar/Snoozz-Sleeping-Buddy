package com.fridayhouse.snoozz.utilities

import android.animation.Animator
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.Interpolator
import androidx.annotation.AnimRes
import com.fridayhouse.snoozz.R

class AnimationHelper {

    companion object {
        /**
         * Changes the visibility of a view with a fade in or fade out animation.
         *
         * @param view The view to change visibility.
         * @param visibility The desired visibility (View.GONE or View.VISIBLE).
         * @param context The context, used to load the animation.
         * @param fadeInAnimRes The resource ID of the fade-in animation.
         * @param fadeOutAnimRes The resource ID of the fade-out animation.
         */
        fun setVisibilityWithAnimation(
            view: View,
            visibility: Int,
            context: Context,
            @AnimRes fadeInAnimRes: Int = R.anim.fade_in,
            @AnimRes fadeOutAnimRes: Int = R.anim.fade_out
        ) {
            when (visibility) {
                View.VISIBLE -> {
                    if (view.visibility != View.VISIBLE) {
                        val fadeIn = AnimationUtils.loadAnimation(context, fadeInAnimRes)
                        view.startAnimation(fadeIn)
                        view.visibility = View.VISIBLE
                    }
                }
                View.GONE -> {
                    if (view.visibility != View.GONE) {
                        val fadeOut = AnimationUtils.loadAnimation(context, fadeOutAnimRes)
                        fadeOut.setAnimationListener(object : Animation.AnimationListener {
                            override fun onAnimationStart(animation: Animation?) {}
                            override fun onAnimationEnd(animation: Animation?) {
                                view.visibility = View.GONE
                            }
                            override fun onAnimationRepeat(animation: Animation?) {}
                        })
                        view.startAnimation(fadeOut)
                    }
                }
            }
        }

        /**
         * Animates the background color of a view.
         *
         * @param view The view whose background color will be animated.
         * @param fromColor The starting color.
         * @param toColor The ending color.
         * @param duration The duration of the animation in milliseconds.
         * @param interpolator The interpolator to be used for the animation.
         * @param onAnimationEnd Callback to be executed when the animation ends.
         */
        fun animateColorChange(
            view: View,
            fromColor: Int,
            toColor: Int,
            duration: Long = 500,
            interpolator: Interpolator = AccelerateDecelerateInterpolator(),
            onAnimationEnd: () -> Unit = {}
        ) {
            val colorAnimator = ValueAnimator.ofObject(ArgbEvaluator(), fromColor, toColor)
            colorAnimator.addUpdateListener { animator ->
                view.setBackgroundColor(animator.animatedValue as Int)
            }
            colorAnimator.duration = duration
            colorAnimator.interpolator = interpolator
            colorAnimator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}

                override fun onAnimationEnd(animation: Animator) {
                    onAnimationEnd.invoke()
                }
                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
            colorAnimator.start()
        }
    }
}
