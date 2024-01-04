package com.fridayhouse.snoozz.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.core.content.res.ResourcesCompat.getColor
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fridayhouse.snoozz.MediaPlayerService
import com.fridayhouse.snoozz.R
import com.fridayhouse.snoozz.databinding.LibraryFragmentBinding
import com.fridayhouse.snoozz.databinding.SoundGroupListItemBinding
import com.fridayhouse.snoozz.databinding.SoundListItemBinding
import com.fridayhouse.snoozz.model.Sound
import com.fridayhouse.snoozz.playback.PlaybackController
import com.fridayhouse.snoozz.playback.Player
import com.fridayhouse.snoozz.repository.PresetRepository
import com.fridayhouse.snoozz.repository.SettingsRepository
import com.fridayhouse.snoozz.ui.viewmodels.MainViewModel
import com.github.ashutoshgngwr.noice.model.Preset
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.navHostFragment
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
@AndroidEntryPoint
class LibraryFragment : Fragment() {

  lateinit var mainViewModel: MainViewModel
  private lateinit var binding: LibraryFragmentBinding
  private lateinit var presetRepository: PresetRepository
  private lateinit var settingsRepository: SettingsRepository
  private var dynamicStrokeColor: ColorStateList? = null
  private var adapter: SoundListAdapter? = null
  private var players = emptyMap<String, Player>()

  private val eventBus = EventBus.getDefault()

  private val dataSet by lazy {
    arrayListOf<SoundListItem>().also { list ->
      var lastDisplayGroupResID = -1
      Sound.LIBRARY.toSortedMap(
        compareBy(
          { getString(Sound.get(it).displayGroupResID) },
          { getString(Sound.get(it).titleResID) }
        )
      ).forEach {
        if (lastDisplayGroupResID != it.value.displayGroupResID) {
          lastDisplayGroupResID = it.value.displayGroupResID
          list.add(
            SoundListItem(
              R.layout.sound_group_list_item, getString(lastDisplayGroupResID)
            )
          )
        }
        list.add(SoundListItem(R.layout.sound_list_item, it.key))
      }
    }
  }

  @Subscribe(sticky = true, threadMode = ThreadMode.ASYNC)
  fun onPlayerManagerUpdate(event: MediaPlayerService.PlaybackUpdateEvent) {
    this.players = event.players
    val showSavePresetFAB = !presetRepository.list().contains(Preset.from("", players.values))

    view?.post {
      adapter?.notifyDataSetChanged()
      if (showSavePresetFAB && event.state == PlaybackStateCompat.STATE_PLAYING) {
        binding.savePresetButton.show()
      } else {
        binding.savePresetButton.hide()
      }
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = LibraryFragmentBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    presetRepository = PresetRepository.newInstance(requireContext())
    settingsRepository = SettingsRepository.newInstance(requireContext())
    mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
    //analyticsProvider = NoiceApplication.of(requireContext()).getAnalyticsProvider()
    adapter = SoundListAdapter(requireContext())
    binding.soundList.also {
      it.adapter = adapter
      it.setHasFixedSize(true)
      it.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
    }

    binding.randomPresetButton.setOnLongClickListener {
      Toast.makeText(requireContext(), R.string.random_preset, Toast.LENGTH_LONG).show()
      true
    }

    binding.randomPresetButton.setOnClickListener {
      findNavController().navigate(R.id.random_preset)
        //navHostFragment.findNavController().navigate(R.id.navigation_random_preset)
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
          val mutedLightColor = getMutedLightColor(vibrantSwatch?.rgb ?: mutedSwatch?.rgb)

          val colorStateList = ColorStateList.valueOf(mutedColor)
          binding.randomPresetButton.backgroundTintList = colorStateList
          binding.savePresetButton.backgroundTintList = colorStateList
          dynamicStrokeColor = ColorStateList.valueOf(mutedLightColor)
          adapter?.notifyDataSetChanged()
        }
      }
    }

    binding.savePresetButton.setOnLongClickListener {
      Toast.makeText(requireContext(), R.string.save_preset, Toast.LENGTH_LONG).show()
      true
    }

    binding.savePresetButton.setOnClickListener {
      val params = bundleOf("success" to false)
      DialogFragment.show(childFragmentManager) {
        val presets = presetRepository.list()
        title(R.string.save_preset)
        input(hintRes = R.string.name, validator = {
          when {
            it.isBlank() -> R.string.preset_name_cannot_be_empty
            presets.any { p -> it == p.name } -> R.string.preset_already_exists
            else -> 0
          }
        })

        negativeButton(R.string.cancel)
        positiveButton(R.string.save) {
          val name = getInputText()
          val preset = Preset.from(name, players.values)
          presetRepository.create(preset)
          binding.savePresetButton.hide()
          showPresetSavedMessage()
          PlaybackController.requestUpdateEvent(requireContext())

          params.putBoolean("success", true)
          //analyticsProvider.logEvent("preset_name", bundleOf("item_length" to name.length))
          val soundCount = preset.playerStates.size
         // analyticsProvider.logEvent("preset_sounds", bundleOf("items_count" to soundCount))
          // maybe show in-app review dialog to the user
//          SnoozzApplication.of(requireContext())
//            .getReviewFlowProvider()
//            .maybeAskForReview(requireActivity())
        }

        onDismiss { //analyticsProvider.logEvent("preset_create", params)
         }
      }
    }

    eventBus.register(this)
    //analyticsProvider.setCurrentScreen("library", LibraryFragment::class)
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

  override fun onDestroyView() {
    eventBus.unregister(this)
    super.onDestroyView()
  }

  private fun showPresetSavedMessage() {
    Snackbar.make(requireView(), R.string.preset_saved, Snackbar.LENGTH_LONG).show()
  }

  private class SoundListItem(@LayoutRes val layoutID: Int, val data: String)

  private inner class SoundListAdapter(private val context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val layoutInflater = LayoutInflater.from(context)

    private val spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
      override fun getSpanSize(position: Int): Int {
        Log.d("LibraryFragment", "getSpanSize: $position")
        return when (adapter?.getItemViewType(position)) {
          R.layout.sound_group_list_item -> 3// Number of columns for sound_group_list_item
          R.layout.sound_list_item -> 1 // Number of columns for sound_list_item
          else -> 3 // Default span size
        }
      }
    }

    init {
      val layoutManager = GridLayoutManager(context, 3) // Set your grid layout manager with 3 columns
      layoutManager.spanSizeLookup = spanSizeLookup
      binding.soundList.layoutManager = layoutManager
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
      return when (viewType) {
        R.layout.sound_group_list_item -> SoundGroupListItemViewHolder(
          SoundGroupListItemBinding.inflate(
            layoutInflater,
            parent,
            false
          )
        )
        R.layout.sound_list_item -> SoundListItemViewHolder(
          SoundListItemBinding.inflate(
            layoutInflater,
            parent,
            false
          )
        )
        else -> throw IllegalArgumentException("unknown view type: $viewType")
      }
    }

    override fun getItemCount(): Int {
      return dataSet.size
    }

    override fun getItemViewType(position: Int): Int {
      return dataSet[position].layoutID
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
      if (dataSet[position].layoutID == R.layout.sound_group_list_item) {
        holder as SoundGroupListItemViewHolder
        holder.binding.root.text = dataSet[position].data
        return
      }

      val soundKey = dataSet[position].data
      val sound = Sound.get(soundKey)
      val isPlaying = players.containsKey(soundKey)
      var volume = Player.DEFAULT_VOLUME
      var timePeriod = Player.DEFAULT_TIME_PERIOD
      if (isPlaying) {
        requireNotNull(players[soundKey]).also {
          volume = it.volume
          timePeriod = it.timePeriod
        }
      }

      with((holder as SoundListItemViewHolder).binding) {
        // Create a ColorStateList with a single color (e.g., magenta)
        var blueColor = ColorStateList.valueOf(getColor(resources, R.color.torquise_mid, null))
        val blackColor = ColorStateList.valueOf(Color.BLACK)
        title.text = context.getString(sound.titleResID)
        if (isPlaying) {
          //playIndicator.visibility = View.VISIBLE
          dynamicStrokeColor?.let {
            soundItemCardView.setStrokeColor(it)
          }
          //soundItemCardView.setStrokeColor(blueColor)
          volumeButton.visibility = View.VISIBLE
          timePeriodButton.visibility = View.VISIBLE
        } else {
         // playIndicator.visibility = View.INVISIBLE
          soundItemCardView.setStrokeColor(blackColor)
          volumeButton.visibility = View.INVISIBLE
          timePeriodButton.visibility = View.INVISIBLE
        }

        if (settingsRepository.shouldDisplaySoundIcons()) {
          icon.visibility = View.VISIBLE
          icon.setImageResource(sound.iconID)
        } else {
          icon.visibility = View.GONE
        }

        @SuppressLint("SetTextI18n")
        //volumeButton.text = "${(volume * 100) / Player.MAX_VOLUME}%"
        volumeButton.isEnabled = isPlaying

        @SuppressLint("SetTextI18n")
       // timePeriodButton.text = "${timePeriod / 60}m ${timePeriod % 60}s"
        timePeriodButton.isEnabled = isPlaying
        if (sound.isLooping) {
          timePeriodButton.visibility = View.GONE
        } else {
          timePeriodButton.visibility = View.VISIBLE
        }
      }
    }
  }
  private fun getMutedLightColor(color: Int?): Int {
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

  inner class SoundGroupListItemViewHolder(val binding: SoundGroupListItemBinding) :
    RecyclerView.ViewHolder(binding.root)

  inner class SoundListItemViewHolder(val binding: SoundListItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    init {
      binding.root.setOnClickListener {
        dataSet.getOrNull(bindingAdapterPosition)?.also {
          if (players.containsKey(it.data)) {
            PlaybackController.stop(requireContext(), it.data)
          } else {
            PlaybackController.play(requireContext(), it.data)
          }
        }
      }

      binding.volumeButton.setOnClickListener { showSoundControlDialog() }
      binding.timePeriodButton.setOnClickListener { showSoundControlDialog() }
    }

    private fun showSoundControlDialog() {
      val listItem = dataSet.getOrNull(bindingAdapterPosition) ?: return
      val player = players[listItem.data] ?: return
      val sound = Sound.get(listItem.data)

      DialogFragment.show(childFragmentManager) {
        title(sound.titleResID)
        message(R.string.volume)
        slider(
          viewID = R.id.volume_slider,
          to = Player.MAX_VOLUME.toFloat(),
          value = player.volume.toFloat(),
          labelFormatter = { "${(it * 100).toInt() / Player.MAX_VOLUME}%" },
          changeListener = { player.setVolume(it.toInt()) }
        )

        if (!sound.isLooping) {
          message(R.string.repeat_time_period)
          slider(
            viewID = R.id.time_period_slider,
            from = Player.MIN_TIME_PERIOD.toFloat(),
            to = Player.MAX_TIME_PERIOD.toFloat(),
            value = player.timePeriod.toFloat(),
            labelFormatter = { "${it.toInt() / 60}m ${it.toInt() % 60}s" },
            changeListener = { player.timePeriod = it.toInt() }
          )
        }

        positiveButton(R.string.okay)
        onDismiss { PlaybackController.requestUpdateEvent(requireContext()) }
      }

      //analyticsProvider.logEvent("sound_controls_open", bundleOf("sound_key" to listItem.data))
    }
  }
}
