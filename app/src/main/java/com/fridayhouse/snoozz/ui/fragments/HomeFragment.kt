package com.fridayhouse.snoozz.ui.fragments

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.LottieAnimationView
import com.fridayhouse.snoozz.R
import com.fridayhouse.snoozz.adapters.HorizontalAdapter
import com.fridayhouse.snoozz.adapters.SongAdapter
import com.fridayhouse.snoozz.adapters.randomSongAdapter
import com.fridayhouse.snoozz.data.HorizontalItem
import com.fridayhouse.snoozz.databinding.FragmentHomeBinding
import com.fridayhouse.snoozz.others.Status
import com.fridayhouse.snoozz.ui.viewmodels.MainViewModel
import com.fridayhouse.snoozz.utilities.AnimationHelper
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.navHostFragment
import kotlinx.android.synthetic.main.fragment_home.loadingAnimationViewHome
import kotlinx.android.synthetic.main.fragment_home.rvAllBreathe
import kotlinx.android.synthetic.main.fragment_home.rvAllSongs
import kotlinx.android.synthetic.main.fragment_home.rvRandomSongs
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    lateinit var mainViewModel: MainViewModel
    @Inject
    lateinit var randomSongAdapter: randomSongAdapter
    @Inject
    lateinit var songAdapter: SongAdapter
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false);
        val view = binding.root;
        return view;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        setupRecyclerView()
        subscribeToObservers()
        setupRecommendRecylerView()
        subscribeToRecommendObservers()

        binding.breathingCardHomeFragment.setOnClickListener {
            navHostFragment.findNavController().navigate(R.id.action_currentFragment_to_breatheFragment)
        }

        binding.relaxingSoundCardHomeFragment.setOnClickListener{
            navHostFragment.findNavController().navigate(R.id.action_to_allMusicFragment)
        }
        val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM"))
        binding.dateText.text = currentDate

        binding.apply {
            tiredEmoji.setOnClickListener{ handleEmojiSelection(); showMoodAckg() }
            sadEmoji.setOnClickListener{ handleEmojiSelection(); showMoodAckg() }
            smileEmoji.setOnClickListener{ handleEmojiSelection(); showMoodAckg() }
            moreSmileEmoji.setOnClickListener{ handleEmojiSelection(); showMoodAckg() }
            grinningEmoji.setOnClickListener{ handleEmojiSelection(); showMoodAckg() }
        }

        binding.emojiSelectedChecked.setOnClickListener{
            revertEmojiSelection()
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
                    val darkMutedColor = getDarkMutedColor(vibrantSwatch?.rgb ?: mutedSwatch?.rgb)

                    // Set the background color of the CardView
                    binding.cardViewFeeling.setCardBackgroundColor(mutedColor)
                    binding.cardViewRecommend.setCardBackgroundColor(darkMutedColor)

                }
            }
        }

        songAdapter.setItemClickListener {
            mainViewModel.playOrToggleSound(it)
        }

        randomSongAdapter.setItemClickListener {
            mainViewModel.playOrToggleSound(it)
        }
    }

    private fun revertEmojiSelection() {
            val sharedPrefs = requireActivity().getSharedPreferences("EmojiPrefs", Context.MODE_PRIVATE)
            with(sharedPrefs.edit()) {
                remove("lastSelectionDate")
                apply()
            }
        updateEmojiLayoutVisibility()
    }

    private fun showMoodAckg() {

        scrollToRecommendedLayout()
        Handler(Looper.getMainLooper()).postDelayed({
            subscribeToRecommendObservers()
            Snackbar.make(requireView(), "Tunes Aligned with Your Emotions!", Snackbar.LENGTH_LONG).show()
        }, 750)
    }
    private fun scrollToRecommendedLayout() {
        val targetView = binding.cardViewRecommend
        smoothScrollToView(targetView)
    }
    private fun smoothScrollToView(targetView: View) {
        val scrollView = binding.scrollViewHome
        val targetTop = targetView.bottom
        val startScrollY = scrollView.scrollY

        // Duration of the scroll
        val duration = 700 // milliseconds

        // ValueAnimator for smooth scrolling
        val animator = ValueAnimator.ofInt(startScrollY, targetTop).apply {
            this.duration = duration.toLong()
            addUpdateListener { animation ->
                val animatedValue = animation.animatedValue as Int
                scrollView.scrollTo(0, animatedValue)
            }
        }
        // Start the animation
        animator.start()
    }
    private fun getDarkMutedColor(color: Int?): Int {
        if (color != null) {
            val hsv = FloatArray(3)
            Color.colorToHSV(color, hsv)
            // Decrease saturation and brightness to mute the color
            hsv[1] *= 0.3f // Decrease saturation by 70%
            hsv[2] *= 0.3f // Decrease brightness by 70%

            return Color.HSVToColor(hsv)
        }
        return Color.GRAY // Return a default grey color if the provided color is null
    }

    private fun setupRecommendRecylerView() = rvRandomSongs.apply {
        adapter = randomSongAdapter
        layoutManager = GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false)
    }

    private fun setupRecyclerView() = rvAllSongs.apply {
        adapter = songAdapter
        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }

    private fun subscribeToRecommendObservers() {
        mainViewModel.mediaItems.observe(viewLifecycleOwner) { result ->
            when (result.status) {
                Status.SUCCESS -> {
                    result.data?.let { sound ->
                        val randomSounds = sound.shuffled().take(4)
                        randomSongAdapter.sounds = randomSounds
                    }
                }
                Status.ERROR -> Unit
                Status.LOADING -> loadingAnimationViewHome.isVisible = true
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateEmojiLayoutVisibility()
    }

    private fun updateEmojiLayoutVisibility() {
        if (mainViewModel.isEmojiSelectionMadeToday()) {
            AnimationHelper.setVisibilityWithAnimation(binding.emojisLayout, View.GONE, requireContext())
            AnimationHelper.setVisibilityWithAnimation(binding.dateText, View.GONE, requireContext())
            binding.emojiSelectedChecked.visibility = View.VISIBLE
            playLottieAnimation()
        } else {
            AnimationHelper.setVisibilityWithAnimation(binding.emojisLayout, View.VISIBLE, requireContext())
            AnimationHelper.setVisibilityWithAnimation(binding.dateText, View.VISIBLE, requireContext())
            binding.emojiSelectedChecked.visibility = View.GONE
        }
    }

    private fun playLottieAnimation() {
        binding.emojiSelectedChecked.playAnimation()
    }

    private fun handleEmojiSelection() {
        saveEmojiSelection()
        updateEmojiLayoutVisibility()
    }

    private fun saveEmojiSelection() {
        val sharedPrefs = requireActivity().getSharedPreferences("EmojiPrefs", Context.MODE_PRIVATE)
        val currentDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        with(sharedPrefs.edit()) {
            putString("lastSelectionDate", currentDate)
            apply()
        }
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

    private fun subscribeToObservers() {
        mainViewModel.mediaItems.observe(viewLifecycleOwner) { result ->
            when (result.status) {
                Status.SUCCESS -> {
                    loadingAnimationViewHome.isVisible = false
                    result.data?.let { sound ->
                        songAdapter.sounds = sound
                    }
                }
                Status.ERROR -> Unit
                Status.LOADING -> loadingAnimationViewHome.isVisible = true
            }
        }
    }
}