package com.fridayhouse.snoozz.ui.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.fridayhouse.snoozz.R
import com.fridayhouse.snoozz.adapters.SongAdapter
import com.fridayhouse.snoozz.databinding.FragmentAllMusicBinding
import com.fridayhouse.snoozz.databinding.FragmentHomeBinding
import com.fridayhouse.snoozz.others.Status
import com.fridayhouse.snoozz.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_home.loadingAnimationViewHome
import kotlinx.android.synthetic.main.fragment_home.rvAllSongs
import javax.inject.Inject

@AndroidEntryPoint
class AllMusicFragment : Fragment() {

    lateinit var mainViewModel: MainViewModel

    @Inject
    lateinit var songAdapter: SongAdapter

    private var _binding: FragmentAllMusicBinding? = null
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
        _binding = FragmentAllMusicBinding.inflate(inflater, container, false);
        val view = binding.root;
        return view;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        setupRecyclerView()
        subscribeToObservers()

        mainViewModel.currentBitmap.observe(viewLifecycleOwner) { bitmap ->
            // Check if the bitmap is not null
            bitmap?.let { currentBitmap ->
                // Perform color extraction logic using Palette library
                Palette.from(currentBitmap).generate { palette ->
                    // Extract the muted color from the palette
                    val vibrantSwatch = palette?.vibrantSwatch
                    val mutedSwatch = palette?.mutedSwatch
                    val mutedColor = getMutedColor(vibrantSwatch?.rgb ?: mutedSwatch?.rgb)
                   // val darkMutedColor = getDarkMutedColor(vibrantSwatch?.rgb ?: mutedSwatch?.rgb)

                    // Set the background color of the CardView
                    binding.allSongTitle.setTextColor(mutedColor)
                }
            }
        }

        songAdapter.setItemClickListener {
            mainViewModel.playOrToggleSound(it)
        }
    }
    private fun getMutedColor(color: Int?): Int {
        if (color != null) {
            val hsv = FloatArray(3)
            Color.colorToHSV(color, hsv)

            // Decrease saturation and brightness to mute the color
            hsv[1] *= 0.5f // Decrease saturation by 60%
            hsv[2] *= 0.5f // Decrease brightness by 60%

            return Color.HSVToColor(hsv)
        }
        return Color.GRAY // Return a default grey color if the provided color is null
    }

    private fun subscribeToObservers() {
        mainViewModel.mediaItems.observe(viewLifecycleOwner) { result ->
            when (result.status) {
                Status.SUCCESS -> {
                    //loadingAnimationViewHome.isVisible = false
                    result.data?.let { sound ->
                        songAdapter.sounds = sound
                    }
                }
                Status.ERROR -> Unit
                Status.LOADING -> loadingAnimationViewHome.isVisible = true
            }
        }
    }

    private fun setupRecyclerView() = rvAllSongs.apply {
        adapter = songAdapter
        layoutManager = GridLayoutManager(requireContext(), 2)
    }

}