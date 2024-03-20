package com.fridayhouse.snoozz.ui.fragments

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.GridLayoutManager
import com.fridayhouse.snoozz.R
import com.fridayhouse.snoozz.adapters.SongAdapter
import com.fridayhouse.snoozz.adapters.randomSongAdapter
import com.fridayhouse.snoozz.databinding.FragmentHomeBinding
import com.fridayhouse.snoozz.others.Constants
import com.fridayhouse.snoozz.others.Status
import com.fridayhouse.snoozz.ui.viewmodels.MainViewModel
import com.fridayhouse.snoozz.utilities.AnimationHelper
import com.fridayhouse.snoozz.utilities.PrefrenceUtils
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.navHostFragment
import kotlinx.android.synthetic.main.fragment_home.rvRandomSongs
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment: Fragment() {

    private lateinit var mainViewModel: MainViewModel
    @Inject
    lateinit var randomSongAdapter: randomSongAdapter
    @Inject
    lateinit var songAdapter: SongAdapter
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var mutedColorCardView: Int = Color.GRAY

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        //setupRecyclerView()
        //subscribeToObservers()
        setupRecommendRecyclerview()
        subscribeToRecommendObservers()


        binding.cardViewFeeling.setShadowColorLight(mutedColorCardView)
        binding.breathingCardHomeFragment.setOnClickListener {
            navHostFragment.findNavController().navigate(R.id.action_currentFragment_to_breatheFragment)
        }

        binding.relaxingSoundCardHomeFragment.setOnClickListener{
            navHostFragment.findNavController().navigate(R.id.action_to_allMusicFragment)
        }
        val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM"))
        binding.dateText.text = currentDate

        binding.apply {
            tiredEmoji.setOnClickListener{ handleEmojiSelection(); refreshRecommendedList() }
            sadEmoji.setOnClickListener{ handleEmojiSelection(); refreshRecommendedList() }
            smileEmoji.setOnClickListener{ handleEmojiSelection(); refreshRecommendedList() }
            moreSmileEmoji.setOnClickListener{ handleEmojiSelection(); refreshRecommendedList() }
            grinningEmoji.setOnClickListener{ handleEmojiSelection(); refreshRecommendedList() }
        }

        binding.emojiSelectedChecked.setOnClickListener{
            revertEmojiSelection()
        }

        binding.refreshItems.setOnClickListener{
            binding.refreshItems.playAnimation()
            Handler(Looper.getMainLooper()).postDelayed({
                scrollToRecommendedLayout()
                subscribeToRecommendObservers()
            }, 650)
        }

        mainViewModel.currentBitmap.observe(viewLifecycleOwner) { bitmap ->
            bitmap?.let { currentBitmap ->
                assignColorsFromBitmap(currentBitmap)
            } ?: retryAssigningColorsAfterDelay()
        }

//        mainViewModel.currentBitmap.observe(viewLifecycleOwner) { bitmap ->
//            bitmap?.let { currentBitmap ->
//                Palette.from(currentBitmap).generate { palette ->
//                    val vibrantSwatch = palette?.vibrantSwatch
//                    val mutedSwatch = palette?.mutedSwatch
//                    val mutedColor = getMutedColor(vibrantSwatch?.rgb ?: mutedSwatch?.rgb)
//                    val dayMutedColor = getDayMutedColor(vibrantSwatch?.rgb ?: mutedSwatch?.rgb)
//                    val darkMutedColor = getDarkMutedColor(vibrantSwatch?.rgb ?: mutedSwatch?.rgb)
//                    val dayDarkMutedColor =  getDayDarkMutedColor(vibrantSwatch?.rgb ?: mutedSwatch?.rgb)
//                    val lightShadow = getLightShadow(vibrantSwatch?.rgb ?: mutedSwatch?.rgb)
//                    val darkShadow = getDarkShadow(vibrantSwatch?.rgb ?: mutedSwatch?.rgb)
//
//
//                   if(PrefrenceUtils.retriveDataInBoolean(context, Constants.DARK_MODE_ENABLED)){
//                       binding.apply {
//                           cardViewFeeling.setShadowColorLight(darkShadow)
//                           cardViewFeeling.setBackgroundColor(mutedColor)
//                           cardViewRecommend.setCardBackgroundColor(darkMutedColor)
//                       }
//                   } else {
//                       binding.apply {
//                           cardViewFeeling.setShadowColorLight(Color.WHITE)
//                           cardViewFeeling.setBackgroundColor(dayMutedColor)
//                           cardViewRecommend.setCardBackgroundColor(Color.WHITE)
//                       }
//                   }
//
//                }
//            }
//        }

        songAdapter.setItemClickListener {
            mainViewModel.playOrToggleSound(it)
        }

        randomSongAdapter.setItemClickListener {
            mainViewModel.playOrToggleSound(it)
        }
    }

    private fun assignColorsFromBitmap(bitmap: Bitmap) {
        Palette.from(bitmap).generate { palette ->
            val vibrantSwatch = palette?.vibrantSwatch
            val mutedSwatch = palette?.mutedSwatch
            val mutedColor = getMutedColor(vibrantSwatch?.rgb ?: mutedSwatch?.rgb)
            val dayMutedColor = getDayMutedColor(vibrantSwatch?.rgb ?: mutedSwatch?.rgb)
            val darkMutedColor = getDarkMutedColor(vibrantSwatch?.rgb ?: mutedSwatch?.rgb)
            val dayDarkMutedColor =  getDayDarkMutedColor(vibrantSwatch?.rgb ?: mutedSwatch?.rgb)
            val lightShadow = getLightShadow(vibrantSwatch?.rgb ?: mutedSwatch?.rgb)
            val darkShadow = getDarkShadow(vibrantSwatch?.rgb ?: mutedSwatch?.rgb)

            if(PrefrenceUtils.retriveDataInBoolean(context, Constants.DARK_MODE_ENABLED)){
                binding.apply {
                    cardViewFeeling.setShadowColorLight(darkShadow)
                    cardViewFeeling.setBackgroundColor(mutedColor)
                    cardViewRecommend.setCardBackgroundColor(darkMutedColor)
                }
            } else {
                binding.apply {
                    cardViewFeeling.setShadowColorLight(Color.WHITE)
                    cardViewFeeling.setBackgroundColor(dayMutedColor)
                    cardViewRecommend.setCardBackgroundColor(Color.WHITE)
                }
            }
        }
    }

    private fun retryAssigningColorsAfterDelay() {
        Handler(Looper.getMainLooper()).postDelayed({
            mainViewModel.currentBitmap.value?.let { bitmap ->
                assignColorsFromBitmap(bitmap)
            }
        }, 2000) // Adjust the delay as per your requirement
    }

    private fun revertEmojiSelection() {
            val sharedPrefs = requireActivity().getSharedPreferences("EmojiPrefs", Context.MODE_PRIVATE)
            with(sharedPrefs.edit()) {
                remove("lastSelectionDate")
                apply()
            }
        updateEmojiLayoutVisibility()
    }

    private fun refreshRecommendedList() {

        scrollToRecommendedLayout()
        Handler(Looper.getMainLooper()).postDelayed({
            subscribeToRecommendObservers()
            Snackbar.make(requireView(), "Tunes Aligned with Your Emotions!", Snackbar.LENGTH_LONG).show()
        }, 750)
    }
    private fun scrollToRecommendedLayout() {
        val targetView = binding.gapView
        smoothScrollToView(targetView)
    }
    private fun smoothScrollToView(targetView: View) {
        val scrollView = binding.scrollViewHome
        val targetTop = targetView.bottom
        val startScrollY = scrollView.scrollY
        val duration = 700

        val animator = ValueAnimator.ofInt(startScrollY, targetTop).apply {
            this.duration = duration.toLong()
            addUpdateListener { animation ->
                val animatedValue = animation.animatedValue as Int
                scrollView.scrollTo(0, animatedValue)
            }
        }
        animator.start()
    }
    private fun getDarkMutedColor(color: Int?): Int {
        if (color != null) {
            val hsv = FloatArray(3)
            Color.colorToHSV(color, hsv)
            hsv[1] *= 0.3f
            hsv[2] *= 0.3f

            return Color.HSVToColor(hsv)
        }
        return Color.GRAY
    }

    private fun getDayDarkMutedColor(color: Int?): Int {
        if (color != null) {
            return Color.WHITE
        }
        return Color.GRAY
    }

    private fun getLightShadow(color: Int?): Int {
        if (color != null) {
            val hsv = FloatArray(3)
            Color.colorToHSV(color, hsv)
            hsv[1] *= 0.05f
            hsv[2] *= 0.95f
           // return Color.HSVToColor(hsv)
            return Color.WHITE
        }
        return Color.GRAY
    }

    private fun getDarkShadow(color: Int?): Int {
        if (color != null) {
            val hsv = FloatArray(3)
            Color.colorToHSV(color, hsv)
            hsv[1] *= 0.2f
            hsv[2] *= 0.2f
            return Color.HSVToColor(hsv)
        }
        return Color.GRAY
    }

    private fun setupRecommendRecyclerview() = rvRandomSongs.apply {
        adapter = randomSongAdapter
        layoutManager = GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false)
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
                Status.LOADING -> Unit
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

    private fun getMutedColor(color: Int?): Int {
        if (color != null) {
            val hsv = FloatArray(3)
            Color.colorToHSV(color, hsv)
            hsv[1] *= 0.4f
            hsv[2] *= 0.4f
            return Color.HSVToColor(hsv)
        }
        return Color.GRAY
    }

    private fun getDayMutedColor(color: Int?): Int {
        if (color != null) {
            val hsv = FloatArray(3)
            Color.colorToHSV(color, hsv)
            hsv[1] *= 0.2f
            hsv[2] *= 0.9f
            return Color.HSVToColor(hsv)
        }
        return Color.GRAY
    }

}