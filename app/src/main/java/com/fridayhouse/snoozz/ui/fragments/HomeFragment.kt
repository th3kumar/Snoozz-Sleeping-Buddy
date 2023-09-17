package com.fridayhouse.snoozz.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.LottieAnimationView
import com.fridayhouse.snoozz.R
import com.fridayhouse.snoozz.adapters.HorizontalAdapter
import com.fridayhouse.snoozz.adapters.SongAdapter
import com.fridayhouse.snoozz.data.HorizontalItem
import com.fridayhouse.snoozz.databinding.FragmentHomeBinding
import com.fridayhouse.snoozz.others.Status
import com.fridayhouse.snoozz.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_home.loadingAnimationViewHome
import kotlinx.android.synthetic.main.fragment_home.rvAllBreathe
import kotlinx.android.synthetic.main.fragment_home.rvAllSongs
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    lateinit var mainViewModel: MainViewModel

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

        // Set up the LinearLayoutManager with horizontal orientation
        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvAllBreathe.layoutManager = layoutManager

        val horizontalItems = listOf(
            HorizontalItem("Bellows", R.drawable.bellows_icon, R.raw.bellows_breathe_anim),
            HorizontalItem("Bee", R.drawable.bee_icon, R.raw.bee_breathe_anim),
            HorizontalItem("Awake", R.drawable.awake_icon, R.raw.awake_breathe_anim),
            HorizontalItem("Belly", R.drawable.belly_icon, R.raw.belly_breathe_anim),
            HorizontalItem("Anu-lom", R.drawable.alt_nostril_icon, R.raw.alt_nostril_anim)
        )

        val horizontalAdapter = HorizontalAdapter(horizontalItems) { position ->
            val selectedAnimationResource = horizontalItems[position].animationResource
            // Handle the item click, e.g., show the corresponding Lottie animation
            showLottieAnimation(selectedAnimationResource)
            scrollToTop()
        }
        binding.rvAllBreathe.adapter = horizontalAdapter
        binding.homePageAnimation.setOnClickListener {
            binding.apply {
                homePageAnimation.setAnimation(R.raw.animation_leaf_homepage)
                instructionTextView.visibility = View.INVISIBLE
                homePageAnimation.loop(false)
                homePageAnimation.playAnimation()
            }
        }

        songAdapter.setItemClickListener {
            mainViewModel.playOrToggleSound(it)
        }
    }

    private fun scrollToTop() {
        binding.scrollViewHome.smoothScrollTo(0, 0)
    }

    private fun showLottieAnimation(animationResource: Int) {
        // Use the animation resource to show the Lottie animation
        val animationView = view?.findViewById<LottieAnimationView>(R.id.homePageAnimation)
        val pauseInstruction = view?.findViewById<TextView>(R.id.instructionTextView)
        animationView?.loop(true)
        animationView?.setAnimation(animationResource)
        animationView?.playAnimation()
        animationView?.visibility = View.VISIBLE

        if (pauseInstruction != null) {
            pauseInstruction.visibility = View.VISIBLE
        }
    }

    private fun setupRecyclerView() = rvAllSongs.apply {
        adapter = songAdapter
        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
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