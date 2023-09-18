package com.fridayhouse.snoozz.activities

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import com.fridayhouse.snoozz.R
import com.fridayhouse.snoozz.databinding.ActivityCustomBinding
import com.fridayhouse.snoozz.exoplayer.PlayerService
import kotlinx.android.synthetic.main.activity_custom.actionButton_customActivity_setTime
import kotlinx.android.synthetic.main.activity_custom.bird_volume
import kotlinx.android.synthetic.main.activity_custom.bowl_volume
import kotlinx.android.synthetic.main.activity_custom.cat_volume
import kotlinx.android.synthetic.main.activity_custom.fire_volume
import kotlinx.android.synthetic.main.activity_custom.flute_volume
import kotlinx.android.synthetic.main.activity_custom.grass_volume
import kotlinx.android.synthetic.main.activity_custom.harp_volume
import kotlinx.android.synthetic.main.activity_custom.keyboard_volume
import kotlinx.android.synthetic.main.activity_custom.music_volume
import kotlinx.android.synthetic.main.activity_custom.ocean_volume
import kotlinx.android.synthetic.main.activity_custom.om_volume
import kotlinx.android.synthetic.main.activity_custom.piano_volume
import kotlinx.android.synthetic.main.activity_custom.rail_volume
import kotlinx.android.synthetic.main.activity_custom.rain_volume
import kotlinx.android.synthetic.main.activity_custom.tabla_volume
import kotlinx.android.synthetic.main.activity_custom.thunder_volume
import kotlinx.android.synthetic.main.activity_custom.wind_volume
import soup.neumorphism.NeumorphImageView
import java.util.Locale
import java.util.concurrent.TimeUnit

class CustomActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCustomBinding
    private var selectedTimerDuration: Long = 0
    private val interval: Long = 1000
    private var countdownTimer: CountDownTimer? = null
    private var startTime: Long = 0
    private val sharedPreferences: SharedPreferences by lazy {
        getSharedPreferences("TimerPrefs", Context.MODE_PRIVATE)
    }

    // timer duration options
    private var timerTimesHumanReadable: Array<String> =
        arrayOf("5 minutes", "15 minutes", "30 minutes", "1 hour", "2 hours", "4 hours", "6 hours")

    // and their corresponding durations in ms
    // and their corresponding durations in ms
    private var timerTimesMilliseconds: Array<Long> = arrayOf(
        5 * 60 * 1000,
        15 * 60 * 1000,
        30 * 60 * 1000,
        60 * 60 * 1000,
        120 * 60 * 1000,
        240 * 60 * 1000,
        360 * 60 * 1000
    )
    private var playerService: PlayerService? = null

    companion object {
        private var isKeyboardVisible = false
        private var isRainVisible = false
        private var isThunderVisible = false
        private var isSeaVisible = false
        private var isWindVisible = false
        private var isMusicVisible = false
        private var isPianoVisible = false
        private var isFluteVisible = false
        private var isGrassVisible = false
        private var isBowlVisible = false
        private var isBirdVisible = false
        private var isHerpVisible = false
        private var isOhmVisible = false
        private var isTrainVisible = false
        private var isCatVisible = false
        private var isFireVisible = false
        private var isDrumVisible = false
        // Add more static variables for other SeekBars if needed

        private var keyboardProgress = 100
        private var thunderProgress = 100
        private var seaProgress = 100
        private var windProgress = 100
        private var musicProgress = 100
        private var pianoProgress = 100
        private var fluteProgress = 100
        private var grassProgress = 100
        private var bowlProgress = 100
        private var birdProgress = 100
        private var herpProgress = 100
        private var ohmProgress = 100
        private var trainProgress = 100
        private var catProgress = 100
        private var fireProgress = 100
        private var drumProgress = 100
        private var rainProgress = 100
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {}

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            playerService = (service as PlayerService.PlayerBinder).getService()
            // update the FAB
            if (playerService?.isPlaying() == true) {
                togglePlayPauseButton(true)
                //binding.actionButtonCustomActivityStopPlay.visibility = View.VISIBLE
                binding.icAtomAnim.resumeAnimation()
            } else togglePlayPauseButton(false) //binding.actionButtonCustomActivityStopPlay.visibility = View.INVISIBLE
            playerService?.playerChangeListener = playerChangeListener

            // Call updateTimerButtonState once service in connected to update UI
            updateTimerButtonState()

            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }

    private val playerChangeListener = {
        if (playerService?.isPlaying() == true) {
            // To enable the "pause" view
            togglePlayPauseButton(true)
           // binding.actionButtonCustomActivityStopPlay.visibility = View.VISIBLE
            binding.icAtomAnim.resumeAnimation()
        } else {
            togglePlayPauseButton(false)
            //binding.actionButtonCustomActivityStopPlay.visibility = View.INVISIBLE
            binding.icAtomAnim.pauseAnimation()
        }
    }

    fun togglePlayPauseButton(enablePause: Boolean) {
        val pauseImageView = findViewById<NeumorphImageView>(R.id.actionButton_customActivity_stopPlay)
        val playImageView = findViewById<NeumorphImageView>(R.id.actionButton_customActivity_play)

        if (enablePause) {
            pauseImageView.visibility = View.VISIBLE
            playImageView.visibility = View.GONE
        } else {
            pauseImageView.visibility = View.GONE
            playImageView.visibility = View.VISIBLE
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Initialize icAtomAnim and set its initial visibility
        binding.icAtomAnim.pauseAnimation()

        // Restore the timer state and time duration from SharedPreferences
        val timerRunning = sharedPreferences.getBoolean("isTimerRunning", false)
        selectedTimerDuration = sharedPreferences.getLong("selectedTimerDuration", 0)
        startTime = sharedPreferences.getLong("startTime", 0)

        // Verify if 'which' is a valid index for the timerTimesMilliseconds array
        val which =
            if (selectedTimerDuration.toInt() in timerTimesMilliseconds.map { it.toInt() }) {
                timerTimesMilliseconds.indexOf(selectedTimerDuration)
            } else {
                // Use the default index of 0 if the selected duration is not found in the array
                0
            }

        if (timerRunning && selectedTimerDuration > 0) {
            // If a timer was running before, restart the timer
            val timeMs: Long = selectedTimerDuration - (System.currentTimeMillis() - startTime)

            startTimer(timeMs)
        } else {
            // Update the countdown text with the selected timer duration
            val hours = TimeUnit.MILLISECONDS.toHours(selectedTimerDuration)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(selectedTimerDuration) % 60
            val seconds = TimeUnit.MILLISECONDS.toSeconds(selectedTimerDuration) % 60
            val formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds)
            updateCountdownText(formattedTime)

            // Update the button state and visibility
            updateTimerButtonState()
            binding.actionButtonCustomActivitySetTime.visibility = if (countdownTimer != null) View.VISIBLE else View.GONE
            binding.timerCountdownText.visibility =
                if (countdownTimer != null) View.VISIBLE else View.GONE
            /*binding.startTimerIcon.visibility =
                if (countdownTimer == null) View.VISIBLE else View.GONE*/
        }

        binding.apply {
            // Restore visibility state from static variables
            keyboardVolume.visibility = if (isKeyboardVisible) View.VISIBLE else View.INVISIBLE
            rainVolume.visibility = if (isRainVisible) View.VISIBLE else View.INVISIBLE
            thunderVolume.visibility = if (isThunderVisible) View.VISIBLE else View.INVISIBLE
            oceanVolume.visibility = if (isSeaVisible) View.VISIBLE else View.INVISIBLE
            windVolume.visibility = if (isWindVisible) View.VISIBLE else View.INVISIBLE
            musicVolume.visibility = if (isMusicVisible) View.VISIBLE else View.INVISIBLE
            pianoVolume.visibility = if (isPianoVisible) View.VISIBLE else View.INVISIBLE
            fluteVolume.visibility = if (isFluteVisible) View.VISIBLE else View.INVISIBLE
            grassVolume.visibility = if (isGrassVisible) View.VISIBLE else View.INVISIBLE
            bowlVolume.visibility = if (isBowlVisible) View.VISIBLE else View.INVISIBLE
            birdVolume.visibility = if (isBirdVisible) View.VISIBLE else View.INVISIBLE
            harpVolume.visibility = if (isHerpVisible) View.VISIBLE else View.INVISIBLE
            omVolume.visibility = if (isOhmVisible) View.VISIBLE else View.INVISIBLE
            railVolume.visibility = if (isTrainVisible) View.VISIBLE else View.INVISIBLE
            catVolume.visibility = if (isCatVisible) View.VISIBLE else View.INVISIBLE
            fireVolume.visibility = if (isFireVisible) View.VISIBLE else View.INVISIBLE
            tablaVolume.visibility = if (isDrumVisible) View.VISIBLE else View.INVISIBLE
            // Set visibility for more SeekBars if needed

            // Restore progress state from static variables
            keyboardVolume.progress = keyboardProgress
            thunderVolume.progress = thunderProgress
            oceanVolume.progress = seaProgress
            windVolume.progress = windProgress
            musicVolume.progress = musicProgress
            pianoVolume.progress = pianoProgress
            fluteVolume.progress = fluteProgress
            grassVolume.progress = grassProgress
            bowlVolume.progress = bowlProgress
            birdVolume.progress = birdProgress
            harpVolume.progress = herpProgress
            omVolume.progress = ohmProgress
            railVolume.progress = trainProgress
            catVolume.progress = catProgress
            fireVolume.progress = fireProgress
            tablaVolume.progress = drumProgress
            rainVolume.progress = rainProgress

            createNotificationChannel()

            iconKeyboard.setOnClickListener {
                playerService?.toggleSound(PlayerService.Sound.KEYBOARD)
                toggleProgressBar(keyboard_volume)
                this@CustomActivity.updateTimerState()
            }

            iconRain.setOnClickListener {
                playerService?.toggleSound(PlayerService.Sound.RAIN)
                toggleProgressBar(rain_volume)
                this@CustomActivity.updateTimerState()
            }

            iconThunder.setOnClickListener {
                playerService?.toggleSound(PlayerService.Sound.THUNDER)
                toggleProgressBar(thunder_volume)
                this@CustomActivity.updateTimerState()
            }

            iconOcean.setOnClickListener {
                playerService?.toggleSound(PlayerService.Sound.OCEAN)
                toggleProgressBar(ocean_volume)
                this@CustomActivity.updateTimerState()
            }

            iconWind.setOnClickListener {
                playerService?.toggleSound(PlayerService.Sound.WIND)
                toggleProgressBar(wind_volume)
                this@CustomActivity.updateTimerState()
            }

            iconMusical.setOnClickListener {
                playerService?.toggleSound(PlayerService.Sound.MUSIC)
                toggleProgressBar(music_volume)
                this@CustomActivity.updateTimerState()
            }

            iconPiano.setOnClickListener {
                playerService?.toggleSound(PlayerService.Sound.PIANO)
                toggleProgressBar(piano_volume)
                this@CustomActivity.updateTimerState()
            }

            iconFlute.setOnClickListener {
                playerService?.toggleSound(PlayerService.Sound.FLUTE)
                toggleProgressBar(flute_volume)
                this@CustomActivity.updateTimerState()
            }

            iconBowl.setOnClickListener {
                playerService?.toggleSound(PlayerService.Sound.BOWL)
                toggleProgressBar(bowl_volume)
                this@CustomActivity.updateTimerState()
            }

            iconGrass.setOnClickListener {
                playerService?.toggleSound(PlayerService.Sound.GRASS)
                toggleProgressBar(grass_volume)
                this@CustomActivity.updateTimerState()
            }

            iconBirds.setOnClickListener {
                playerService?.toggleSound(PlayerService.Sound.BIRD)
                toggleProgressBar(bird_volume)
                this@CustomActivity.updateTimerState()
            }

            iconHarp.setOnClickListener {
                playerService?.toggleSound(PlayerService.Sound.HARP)
                toggleProgressBar(harp_volume)
                this@CustomActivity.updateTimerState()
            }

            iconOm.setOnClickListener {
                playerService?.toggleSound(PlayerService.Sound.OM)
                toggleProgressBar(om_volume)
                this@CustomActivity.updateTimerState()
            }

            iconRailway.setOnClickListener {
                playerService?.toggleSound(PlayerService.Sound.RAIL)
                toggleProgressBar(rail_volume)
                this@CustomActivity.updateTimerState()
            }

            iconCat.setOnClickListener {
                playerService?.toggleSound(PlayerService.Sound.CAT)
                toggleProgressBar(cat_volume)
                this@CustomActivity.updateTimerState()
            }

            iconFire.setOnClickListener {
                playerService?.toggleSound(PlayerService.Sound.FIRE)
                toggleProgressBar(fire_volume)
                this@CustomActivity.updateTimerState()
            }

            iconTabla.setOnClickListener {
                playerService?.toggleSound(PlayerService.Sound.TABLA)
                toggleProgressBar(tabla_volume)
                this@CustomActivity.updateTimerState()
            }

            keyboardVolume.setOnSeekBarChangeListener(VolumeChangeListener(PlayerService.Sound.KEYBOARD))
            rainVolume.setOnSeekBarChangeListener(VolumeChangeListener(PlayerService.Sound.RAIN))
            thunderVolume.setOnSeekBarChangeListener(VolumeChangeListener(PlayerService.Sound.THUNDER))
            oceanVolume.setOnSeekBarChangeListener(VolumeChangeListener(PlayerService.Sound.OCEAN))
            windVolume.setOnSeekBarChangeListener(VolumeChangeListener(PlayerService.Sound.WIND))
            musicVolume.setOnSeekBarChangeListener(VolumeChangeListener(PlayerService.Sound.MUSIC))
            pianoVolume.setOnSeekBarChangeListener(VolumeChangeListener(PlayerService.Sound.PIANO))
            fluteVolume.setOnSeekBarChangeListener(VolumeChangeListener(PlayerService.Sound.FLUTE))
            grassVolume.setOnSeekBarChangeListener(VolumeChangeListener(PlayerService.Sound.GRASS))
            bowlVolume.setOnSeekBarChangeListener(VolumeChangeListener(PlayerService.Sound.BOWL))
            birdVolume.setOnSeekBarChangeListener(VolumeChangeListener(PlayerService.Sound.BIRD))
            harpVolume.setOnSeekBarChangeListener(VolumeChangeListener(PlayerService.Sound.HARP))
            omVolume.setOnSeekBarChangeListener(VolumeChangeListener(PlayerService.Sound.OM))
            railVolume.setOnSeekBarChangeListener(VolumeChangeListener(PlayerService.Sound.RAIL))
            catVolume.setOnSeekBarChangeListener(VolumeChangeListener(PlayerService.Sound.CAT))
            fireVolume.setOnSeekBarChangeListener(VolumeChangeListener(PlayerService.Sound.FIRE))
            tablaVolume.setOnSeekBarChangeListener(VolumeChangeListener(PlayerService.Sound.TABLA))

            actionButtonCustomActivityStopPlay.setOnClickListener {
                playerService?.stopPlaying()
                togglePlayPauseButton(false)
                //actionButtonCustomActivityStopPlay.visibility = View.INVISIBLE
                icAtomAnim.pauseAnimation()
                // hide all volume bars
                hideAllVolumeBars()
                this@CustomActivity.stopPlaying()
                this@CustomActivity.cancelTimer()
            }

            actionButtonCustomActivityPlay.setOnClickListener {
                //will change it later, toast for user feedback for now only.
                Toast.makeText(this@CustomActivity, "Pick a sound to play 🎵👆", Toast.LENGTH_SHORT).show()
            }


            actionButtonCustomActivitySetTime.setOnClickListener {
                this@CustomActivity.startTimerClickHandler()
            }

            actionButtonCustomActivityRemoveTime.setOnClickListener {
                this@CustomActivity.cancelTimer()
            }
        }
    }

    private fun hideAllVolumeBars() {
        arrayOf(
            keyboard_volume,
            rain_volume,
            thunder_volume,
            ocean_volume,
            wind_volume,
            music_volume,
            piano_volume,
            flute_volume,
            grass_volume,
            bowl_volume,
            bird_volume,
            harp_volume,
            om_volume,
            rail_volume,
            cat_volume,
            fire_volume,
            tabla_volume,
        ).forEach { bar ->
            bar?.visibility = View.INVISIBLE
        }
    }

    private fun toggleProgressBar(progressBar: ProgressBar) {
        progressBar.visibility =
            if (progressBar.visibility == View.VISIBLE) View.INVISIBLE else View.VISIBLE
    }

    private fun stopPlaying() {
        playerService?.stopPlaying()
        binding.icAtomAnim.pauseAnimation()
        togglePlayPauseButton(false)
        //binding.actionButtonCustomActivityStopPlay.visibility = View.INVISIBLE
        // hide all volume bars
        hideAllVolumeBars()
    }

    private fun updateTimerState() {
        // update the timer state in response to the user enabling/disabling a specific soundtrack
        // if no sound is playing, cancel the timer and update the button state
        // if sound is playing, only update the button state
        if (playerService == null || !(playerService!!.isPlaying()))
            this.cancelTimer()
        this.updateTimerButtonState()
    }

    private fun updateTimerButtonState() {
        // if no sound is playing, both buttons should be invisible
        binding.apply {
            if (playerService == null || !(playerService!!.isPlaying())) {
                //startTimer.visibility = View.GONE
                actionButtonCustomActivitySetTime.visibility = View.GONE
                actionButtonCustomActivityRemoveTime.visibility = View.GONE
                timerCountdownText.visibility = View.GONE
            } else {
                // sound is playing
                if (this@CustomActivity.countdownTimer == null) {
                    // No timer is running, show the start button and hide the cancel button
                    //startTimer.visibility = View.VISIBLE
                    actionButtonCustomActivitySetTime.visibility = View.VISIBLE
                    actionButtonCustomActivityRemoveTime.visibility = View.GONE
                    //startTimerIcon.visibility = View.VISIBLE
                    timerCountdownText.visibility = View.GONE
                } else {
                    // Timer is running, hide the start button and show the timer countdown
                    //startTimer.visibility = View.VISIBLE
                    actionButtonCustomActivitySetTime.visibility = View.INVISIBLE
                    actionButtonCustomActivityRemoveTime.visibility = View.VISIBLE
                    timerCountdownText.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun startTimerClickHandler() {
        // Create a list of items to show in the AlertDialog
        val items = timerTimesHumanReadable

        // Set the custom style for the AlertDialog
        val alertDialog = AlertDialog.Builder(this, R.style.RoundedAlertDialog)

        // Set the title for the AlertDialog
        alertDialog.setTitle("Set timer duration")
        // Use the custom layout for the list items
        val customAdapter = ArrayAdapter<String>(this, R.layout.custom_list_item, items)
        alertDialog.setAdapter(customAdapter) { _, which ->
            if (which in timerTimesMilliseconds.indices) {
                val timeMs = timerTimesMilliseconds[which]
                startTimer(timeMs)
            } else {
                //no-op
            }
        }
        alertDialog.show()
    }

    private fun startTimer(timeMs: Long) {
        // Ensure the 'timeMs' parameter is a valid positive value
        if (timeMs <= 0) {
            return
        }

        // Calculate the new start time
        startTime = System.currentTimeMillis()

        // Calculate the remaining time in milliseconds
        val remainingTimeMs = timeMs - (System.currentTimeMillis() - startTime)

        // Cancel the previous timer if it's running
        countdownTimer?.cancel()

        // Save the timer state and time duration in SharedPreferences
        val editor = sharedPreferences.edit()
        editor.putLong("selectedTimerDuration", timeMs)
        editor.putLong("startTime", System.currentTimeMillis())
        editor.putBoolean("isTimerRunning", true)
        editor.apply()

        // Create a new countdown timer
        countdownTimer = object : CountDownTimer(remainingTimeMs, interval) {
            override fun onTick(millisUntilFinished: Long) {
                val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60
                val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60
                updateCountdownText(
                    String.format(
                        Locale.getDefault(),
                        "%02d:%02d:%02d",
                        hours,
                        minutes,
                        seconds
                    )
                )
            }

            override fun onFinish() {
                updateCountdownText("00:00:00")
                stopPlaying()
                cancelTimer()
            }
        }
        countdownTimer?.start()

        // Update the button state
        updateTimerButtonState()

        // Show the countdown text and action button to set time and remove time
        binding.actionButtonCustomActivitySetTime.visibility = View.INVISIBLE
        binding.actionButtonCustomActivityRemoveTime.visibility = View.VISIBLE
        binding.timerCountdownText.visibility = View.VISIBLE
    }

    private fun updateCountdownText(countdownText: String) {
        binding.timerCountdownText.text = countdownText
    }

    private fun cancelTimer() {
        // Cancel the countdown timer if it's running
        countdownTimer?.cancel()

        // Clear the selected timer duration
        selectedTimerDuration = 0L

        // Hide the countdown text
        binding.timerCountdownText.visibility = View.GONE

        // Set the countdown timer to null
        countdownTimer = null

        // Update the button state
        updateTimerButtonState()
        playerService?.stopForeground()

        // Save the timer state and time duration in SharedPreferences
        val editor = sharedPreferences.edit()
        editor.putLong("selectedTimerDuration", selectedTimerDuration)
        editor.putBoolean("isTimerRunning", false)
        editor.apply()
    }

    inner class VolumeChangeListener(private val sound: PlayerService.Sound) :
        SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            playerService?.setVolume(sound, (progress + 1) / 20f)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}

        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    }

    override fun onRestart() {
        super.onRestart()
        if (playerService?.isPlaying() == true) {
            binding.icAtomAnim.resumeAnimation()
        }

    }

    override fun onStart() {
        super.onStart()
        val playerIntent = Intent(this, PlayerService::class.java)
        startService(playerIntent)
        bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE)

    }

    override fun onStop() {
        unbindService(serviceConnection)
        super.onStop()
        binding.icAtomAnim.pauseAnimation()
        binding.apply {
            // Save visibility state to static variables
            isKeyboardVisible = keyboardVolume.visibility == View.VISIBLE
            isRainVisible = rainVolume.visibility == View.VISIBLE
            isThunderVisible = thunderVolume.visibility == View.VISIBLE
            isSeaVisible = oceanVolume.visibility == View.VISIBLE
            isWindVisible = windVolume.visibility == View.VISIBLE
            isMusicVisible = musicVolume.visibility == View.VISIBLE
            isPianoVisible = pianoVolume.visibility == View.VISIBLE
            isFluteVisible = fluteVolume.visibility == View.VISIBLE
            isGrassVisible = grassVolume.visibility == View.VISIBLE
            isBowlVisible = bowlVolume.visibility == View.VISIBLE
            isBirdVisible = birdVolume.visibility == View.VISIBLE
            isHerpVisible = harpVolume.visibility == View.VISIBLE
            isOhmVisible = omVolume.visibility == View.VISIBLE
            isTrainVisible = rainVolume.visibility == View.VISIBLE
            isCatVisible = catVolume.visibility == View.VISIBLE
            isFireVisible = fireVolume.visibility == View.VISIBLE
            isDrumVisible = tablaVolume.visibility == View.VISIBLE
            // Save visibility state for more SeekBars if needed

            // Save progress state to static variables
            keyboardProgress = keyboardVolume.progress
            thunderProgress = thunderVolume.progress
            seaProgress = oceanVolume.progress
            windProgress = windVolume.progress
            musicProgress = musicVolume.progress
            pianoProgress = pianoVolume.progress
            fluteProgress = fluteVolume.progress
            grassProgress = grassVolume.progress
            bowlProgress = bowlVolume.progress
            birdProgress = birdVolume.progress
            herpProgress = harpVolume.progress
            ohmProgress = omVolume.progress
            trainProgress = railVolume.progress
            catProgress = catVolume.progress
            fireProgress = fireVolume.progress
            drumProgress = tablaVolume.progress
            rainProgress = rainVolume.progress
            Log.d("CustomActivity", "onStop() called")
        }
    }

    override fun onResume() {
        super.onResume()
        playerService?.stopForeground()
        if (playerService?.isPlaying() == true) {
            binding.icAtomAnim.resumeAnimation()
        }
    }

    override fun onPause() {
        playerService?.startForeground()
        super.onPause()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.app_name)
            val importance = NotificationManager.IMPORTANCE_MIN
            val channel = NotificationChannel("softsound", name, importance)

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }
}