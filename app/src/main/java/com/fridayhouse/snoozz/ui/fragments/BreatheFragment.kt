package com.fridayhouse.snoozz.ui.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.value.SimpleLottieValueCallback
import com.fridayhouse.snoozz.R
import com.fridayhouse.snoozz.adapters.HorizontalAdapter
import com.fridayhouse.snoozz.data.HorizontalItem
import com.fridayhouse.snoozz.databinding.FragmentBreatheBinding
import com.fridayhouse.snoozz.databinding.FragmentHomeBinding
import com.fridayhouse.snoozz.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_breathe.lottieAnimationView
import kotlinx.android.synthetic.main.fragment_home.rvAllBreathe

@AndroidEntryPoint
class BreatheFragment : Fragment() {

    lateinit var mainViewModel: MainViewModel

    private var _binding: FragmentBreatheBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentBreatheBinding.inflate(inflater, container, false);
        val view = binding.root;
        return view;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        val layoutManager = LinearLayoutManager(requireContext())
        rvAllBreathe.layoutManager = layoutManager

        binding.lottieAnimationView.visibility = View.VISIBLE
        val horizontalItems = listOf(
            HorizontalItem("bellows breathing", R.drawable.bellows_icon, R.raw.bellows_breathe_anim),
            HorizontalItem("bee breathing", R.drawable.bee_icon, R.raw.bee_breathe_anim),
            HorizontalItem("awake breathing", R.drawable.awake_icon, R.raw.awake_breathe_anim),
            HorizontalItem("belly breathing", R.drawable.belly_icon, R.raw.belly_breathe_anim),
            HorizontalItem("anulom-vilom Breathing", R.drawable.alt_nostril_icon, R.raw.alt_nostril_anim)
        )

        val horizontalAdapter = HorizontalAdapter(horizontalItems) { position ->
            val selectedAnimationResource = horizontalItems[position].animationResource
            // Handle the item click, e.g., show the corresponding Lottie animation
            showLottieAnimation(selectedAnimationResource)
            //scrollToTop() [if your recyclerView is far from lottieAnimationView implement a function to return to lottieAnimationView]
        }
        binding.rvAllBreathe.adapter = horizontalAdapter
        binding.rvAllBreathe.also {
            it.setHasFixedSize(true)
            it.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        }
        binding.lottieAnimationView.setOnClickListener {
            binding.apply {
                lottieAnimationView.setAnimation(R.raw.animation_leaf_homepage)
                //instructionTextView.visibility = View.INVISIBLE
                lottieAnimationView.loop(false)
                lottieAnimationView.playAnimation()
            }
        }

        mainViewModel.currentBitmap.observe(viewLifecycleOwner) { bitmap ->
            // Check if the bitmap is not null
            bitmap?.let { currentBitmap ->
                // Perform color extraction logic using Palette library
                Palette.from(currentBitmap).generate { palette ->
                    // Extract the muted color from the palette
                    val vibrantSwatch = palette?.vibrantSwatch
                    val mutedSwatch = palette?.mutedSwatch
                    val mutedColor = getMutedColor(vibrantSwatch?.rgb ?: mutedSwatch?.rgb)
                    val lightMutedColor = getLightMutedColor(vibrantSwatch?.rgb ?: mutedSwatch?.rgb)

                    val colorStateList = ColorStateList.valueOf(mutedColor)
                    binding.animationPlayPauseFab.backgroundTintList = colorStateList
                    binding.titleTextView.setTextColor(mutedColor)

                    binding.lottieAnimationView.addValueCallback(
                        KeyPath("**"),
                        LottieProperty.COLOR_FILTER
                    ) { PorterDuffColorFilter(lightMutedColor, PorterDuff.Mode.SRC_ATOP) }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.lottieAnimationView.visibility = View.VISIBLE
    }

    private fun getLightMutedColor(color: Int?): Int {
        if (color != null) {
            val hsv = FloatArray(3)
            Color.colorToHSV(color, hsv)

            // Decrease saturation and brightness to mute the color
            hsv[1] *= 0.7f // Decrease saturation by 30%
            hsv[2] *= 0.7f // Decrease brightness by 30%

            return Color.HSVToColor(hsv)
        }
        return Color.GRAY // Return a default grey color if the provided color is null
    }

    fun getMutedColor(color: Int?): Int {
        if (color != null) {
            val hsv = FloatArray(3)
            Color.colorToHSV(color, hsv)

            // Decrease saturation and brightness to mute the color
            hsv[1] *= 0.4f // Decrease saturation by 60%
            hsv[2] *= 0.4f // Decrease brightness by 60%

            return Color.HSVToColor(hsv)
        }
        return Color.GRAY // Return a default grey color if the provided color is null
    }

    private fun scrollToTop(){
       // binding.exampleScrollView.smoothScrollTo(0, 0)
    }

    private fun showLottieAnimation(animationResource: Int) {
        // Use the animation resource to show the Lottie animation
        val animationView = view?.findViewById<LottieAnimationView>(R.id.lottieAnimationView)
        val pauseInstruction = view?.findViewById<TextView>(R.id.instructionTextView)
        animationView?.loop(true)
        animationView?.setAnimation(animationResource)
        animationView?.playAnimation()
        animationView?.visibility = View.VISIBLE

        if (pauseInstruction != null) {
            pauseInstruction.visibility = View.VISIBLE
        }
    }

}