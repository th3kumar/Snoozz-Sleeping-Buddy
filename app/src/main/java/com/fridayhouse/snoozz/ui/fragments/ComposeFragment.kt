package com.fridayhouse.snoozz.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.fridayhouse.snoozz.MediaPlayerService
import com.fridayhouse.snoozz.R
import com.fridayhouse.snoozz.SnoozzApplication
import com.fridayhouse.snoozz.databinding.FragmentComposeBinding
import com.fridayhouse.snoozz.ext.launchInCustomTab
import com.fridayhouse.snoozz.navigation.Navigable
import com.fridayhouse.snoozz.playback.PlaybackController
import com.fridayhouse.snoozz.repository.SettingsRepository
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ComposeFragment : Fragment(), Navigable {

    private lateinit var binding: FragmentComposeBinding
    private lateinit var navController: NavController
    private lateinit var childNavController: NavController
   // private lateinit var analyticsProvider: AnalyticsProvider
    // private lateinit var castAPIProvider: CastAPIProvider

    private var playerManagerState = PlaybackStateCompat.STATE_STOPPED

    private val childNavDestChangeListener = { _: NavController, _: NavDestination, _: Bundle? ->
        activity?.invalidateOptionsMenu() ?: Unit
    }

    // Do not refresh user preference when reconstructing this fragment from a previously saved state.
    // For whatever reasons, it makes the bottom navigation view go out of sync.
    private val shouldDisplaySavedPresetsAsHomeScreen by lazy {
        SettingsRepository.newInstance(requireContext())
            .shouldDisplaySavedPresetsAsHomeScreen()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        val shuffleAndPauseImageView = view?.findViewById<ImageView>(R.id.ivShuffleAndPause)
        shuffleAndPauseImageView?.setOnClickListener { onImageViewClick<ImageView>(shuffleAndPauseImageView) }

        val app = SnoozzApplication.of(requireContext())
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        binding = FragmentComposeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        navController = view.findNavController()
        val navHostFragment = requireNotNull(binding.navHostFragment.getFragment<NavHostFragment>())
        childNavController = navHostFragment.navController


        binding.bottomNav.setupWithNavController(childNavController)
        childNavController.addOnDestinationChangedListener(childNavDestChangeListener)

        // Set the home screen to the saved presets fragment if the user has selected this option
        view.findViewById<ImageView>(R.id.ivOpenPresetFragment)?.setOnClickListener {
            // Use the NavController to navigate to the SavedPresetsFragment
            navController.navigate(R.id.saved_presets)
        }

        val shuffleAndPauseImageView = view.findViewById<ImageView>(R.id.ivShuffleAndPause)
        shuffleAndPauseImageView?.setOnClickListener { onImageViewClick<ImageView>(shuffleAndPauseImageView) }

    }

    override fun onDestroyView() {
        childNavController.removeOnDestinationChangedListener(childNavDestChangeListener)
        super.onDestroyView()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        //castAPIProvider.addMenuItem(menu, R.string.cast_media)
        val displayPlaybackControls = childNavController.currentDestination?.id != null
        if (displayPlaybackControls && PlaybackStateCompat.STATE_STOPPED != playerManagerState) {
            addPlaybackToggleMenuItem(menu)
        }

        inflater.inflate(R.menu.home_menu, menu)
        menu.findItem(R.id.sleep_timer)?.isVisible = displayPlaybackControls
        menu.findItem(R.id.random_preset)?.isVisible = displayPlaybackControls
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun addPlaybackToggleMenuItem(menu: Menu): MenuItem {
        return menu.add(0, R.id.action_playback_toggle, 0, R.string.play_pause).apply {
            setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            if (PlaybackStateCompat.STATE_PLAYING == playerManagerState) {
                setIcon(R.drawable.ic_pause_24dp)
            } else {
                setIcon(R.drawable.ic_play_arrow_24dp)
            }

            setOnMenuItemClickListener {
                if (PlaybackStateCompat.STATE_PLAYING == playerManagerState) {
                    PlaybackController.pause(requireContext())
                } else {
                    PlaybackController.resume(requireContext())
                }

                //analyticsProvider.logEvent("playback_toggle_click", bundleOf())
                true
            }
        }
    }

    override fun onNavDestinationSelected(@IdRes destID: Int): Boolean {
        return binding.bottomNav.menu.findItem(destID)?.let {
            NavigationUI.onNavDestinationSelected(it, childNavController)
        } ?: false
    }

    override fun <T : ImageView> onImageViewClick(imageView: ImageView) {
        // Toggle between play and pause icons
        if (playerManagerState == PlaybackStateCompat.STATE_PLAYING) {
            // Change the ImageView source to the pause icon
            imageView.setImageResource(R.drawable.ic_pause_24dp)
            PlaybackController.pause(requireContext())
        } else {
            // Change the ImageView source to the play icon
            imageView.setImageResource(R.drawable.ic_play_arrow_24dp)
            PlaybackController.resume(requireContext())
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (NavigationUI.onNavDestinationSelected(item, childNavController)) {
            return true
        }

        if (NavigationUI.onNavDestinationSelected(item, navController)) {
            return true
        }

        when (item.itemId) {
            R.id.report_issue -> {
                var url = getString(R.string.app_issues_github_url)


                Uri.parse(url).launchInCustomTab(requireContext())
               // analyticsProvider.logEvent("issue_tracker_open", bundleOf())
            }

            R.id.submit_feedback -> {
                Uri.parse(getString(R.string.feedback_form_url)).launchInCustomTab(requireContext())
                //analyticsProvider.logEvent("feedback_form_open", bundleOf())
            }

            else -> return super.onOptionsItemSelected(item)
        }

        return true
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onPlayerManagerUpdate(event: MediaPlayerService.PlaybackUpdateEvent) {
        if (playerManagerState == event.state) {
            return
        }

        playerManagerState = event.state
        activity?.invalidateOptionsMenu()
    }

}
