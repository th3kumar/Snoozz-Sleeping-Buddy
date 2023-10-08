package com.fridayhouse.snoozz.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
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
import com.github.ashutoshgngwr.noice.model.Preset
import com.google.android.material.snackbar.Snackbar
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class LibraryFragment : Fragment() {

  private lateinit var binding: LibraryFragmentBinding
  private lateinit var presetRepository: PresetRepository
  private lateinit var settingsRepository: SettingsRepository


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
    //analyticsProvider = NoiceApplication.of(requireContext()).getAnalyticsProvider()
    adapter = SoundListAdapter(requireContext())
    binding.soundList.also {
      it.adapter = adapter
      it.setHasFixedSize(true)
      it.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
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

  override fun onDestroyView() {
    eventBus.unregister(this)
    super.onDestroyView()
  }

  private fun showPresetSavedMessage() {
    Snackbar.make(requireView(), R.string.preset_saved, Snackbar.LENGTH_LONG)
      .setAction(R.string.dismiss) { }
      .show()
  }

  private class SoundListItem(@LayoutRes val layoutID: Int, val data: String)

  private inner class SoundListAdapter(private val context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val layoutInflater = LayoutInflater.from(context)

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
        title.text = context.getString(sound.titleResID)
        if (isPlaying) {
          playIndicator.visibility = View.VISIBLE
        } else {
          playIndicator.visibility = View.INVISIBLE
        }

        if (settingsRepository.shouldDisplaySoundIcons()) {
          icon.visibility = View.VISIBLE
          icon.setImageResource(sound.iconID)
        } else {
          icon.visibility = View.GONE
        }

        @SuppressLint("SetTextI18n")
        volumeButton.text = "${(volume * 100) / Player.MAX_VOLUME}%"
        volumeButton.isEnabled = isPlaying

        @SuppressLint("SetTextI18n")
        timePeriodButton.text = "${timePeriod / 60}m ${timePeriod % 60}s"
        timePeriodButton.isEnabled = isPlaying
        if (sound.isLooping) {
          timePeriodButton.visibility = View.GONE
        } else {
          timePeriodButton.visibility = View.VISIBLE
        }
      }
    }
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
