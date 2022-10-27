package com.fridayhouse.snoozz

import PlayerService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_custom.*
import kotlinx.android.synthetic.main.activity_custom.view.*


class CustomActivity : AppCompatActivity() {
//    var keyboardplaybool: Boolean = false
//    var rainplaybool: Boolean = false
//    var thunderplaybool: Boolean = false
//    var oceanplaybool: Boolean = false
//    var windplaybool: Boolean = false
//    var musicplaybool: Boolean = false
//    var pianoplaybool: Boolean = false
//    var fluteplaybool: Boolean = false
//    var grassplaybool: Boolean = false
//    var bowlplaybool: Boolean = false
//    var birdplaybool: Boolean = false
//    var harpplaybool: Boolean = false
//    var omplaybool: Boolean = false
//    var railplaybool: Boolean = false
//    var catplaybool: Boolean = false
//    var fireplaybool: Boolean = false
//    var tablaplaybool: Boolean = false

    private var playerService: PlayerService? = null




    private val serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {}

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            playerService = (service as PlayerService.PlayerBinder).getService()
            // update the FAB
            if (playerService?.isPlaying() == true) fab.show() else fab.hide()
            playerService?.playerChangeListener = playerChangeListener
        }

    }

    private val playerChangeListener = {
        if (playerService?.isPlaying() == true) fab.show() else fab.hide()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom)

       createNotificationChannel()
       // val keyboardplay: ImageView = findViewById(R.id.icon_keyboard)
        icon_keyboard.setOnClickListener{
            playerService?.toggleSound(PlayerService.Sound.KEYBOARD)
            toggleProgressBar(keyboard_volume)
            toggleImageView(icon_keyboard)
        }

        //val rainplay: ImageView = findViewById(R.id.icon_rain)
        icon_rain.setOnClickListener{
            playerService?.toggleSound(PlayerService.Sound.RAIN)
            toggleProgressBar(rain_volume)
        }

        //val thunderplay: ImageView = findViewById(R.id.icon_thunder)
        icon_thunder.setOnClickListener{
            playerService?.toggleSound(PlayerService.Sound.THUNDER)
            toggleProgressBar(thunder_volume)
        }

        //val oceanplay: ImageView = findViewById(R.id.icon_ocean)
        icon_ocean.setOnClickListener{
            playerService?.toggleSound(PlayerService.Sound.OCEAN)
            toggleProgressBar(ocean_volume)
        }

       // val windplay: ImageView = findViewById(R.id.icon_wind)
        icon_wind.setOnClickListener{
            playerService?.toggleSound(PlayerService.Sound.WIND)
            toggleProgressBar(wind_volume)
        }

       // val musicplay: ImageView = findViewById(R.id.icon_musical)
        icon_musical.setOnClickListener{
            playerService?.toggleSound(PlayerService.Sound.MUSIC)
            toggleProgressBar(music_volume)
        }

        //val pianoplay: ImageView = findViewById(R.id.icon_piano)
        icon_piano.setOnClickListener{
            playerService?.toggleSound(PlayerService.Sound.PIANO)
            toggleProgressBar(piano_volume)
        }

        //val fluteplay: ImageView = findViewById(R.id.icon_flute)
        icon_flute.setOnClickListener{
            playerService?.toggleSound(PlayerService.Sound.FLUTE)
            toggleProgressBar(flute_volume)
        }

        //val bowlplay: ImageView = findViewById(R.id.icon_bowl)
        icon_bowl.setOnClickListener{
            playerService?.toggleSound(PlayerService.Sound.BOWL)
            toggleProgressBar(bowl_volume)
        }

        //val grassplay: ImageView = findViewById(R.id.icon_grass)
        icon_grass.setOnClickListener{
            playerService?.toggleSound(PlayerService.Sound.GRASS)
            toggleProgressBar(grass_volume)
        }

        //val birdplay: ImageView = findViewById(R.id.icon_birds)
        icon_birds.setOnClickListener{
            playerService?.toggleSound(PlayerService.Sound.BIRD)
            toggleProgressBar(bird_volume)
        }

        //val harpplay: ImageView = findViewById(R.id.icon_harp)
        icon_harp.setOnClickListener{
            playerService?.toggleSound(PlayerService.Sound.HARP)
            toggleProgressBar(harp_volume)
        }

        //val omplay: ImageView = findViewById(R.id.icon_om)
        icon_om.setOnClickListener{
            playerService?.toggleSound(PlayerService.Sound.OM)
            toggleProgressBar(om_volume)
        }

        //val railplay: ImageView = findViewById(R.id.icon_railway)
        icon_railway.setOnClickListener{
            playerService?.toggleSound(PlayerService.Sound.RAIL)
            toggleProgressBar(rail_volume)
        }

        //val catplay: ImageView = findViewById(R.id.icon_cat)
        icon_cat.setOnClickListener{
            playerService?.toggleSound(PlayerService.Sound.CAT)
            toggleProgressBar(cat_volume)
        }

        //val fireplay: ImageView = findViewById(R.id.icon_fire)
        icon_fire.setOnClickListener{
            playerService?.toggleSound(PlayerService.Sound.FIRE)
            toggleProgressBar(fire_volume)
        }

        //val tablaplay: ImageView = findViewById(R.id.icon_tabla)
        icon_tabla.setOnClickListener{
            playerService?.toggleSound(PlayerService.Sound.TABLA)
            toggleProgressBar(tabla_volume)
        }

        keyboard_volume.setOnSeekBarChangeListener(VolumeChangeListener(PlayerService.Sound.KEYBOARD))
        rain_volume.setOnSeekBarChangeListener(VolumeChangeListener(PlayerService.Sound.RAIN))
        thunder_volume.setOnSeekBarChangeListener(VolumeChangeListener(PlayerService.Sound.THUNDER))
        ocean_volume.setOnSeekBarChangeListener(VolumeChangeListener(PlayerService.Sound.OCEAN))
        wind_volume.setOnSeekBarChangeListener(VolumeChangeListener(PlayerService.Sound.WIND))
        music_volume.setOnSeekBarChangeListener(VolumeChangeListener(PlayerService.Sound.MUSIC))
        piano_volume.setOnSeekBarChangeListener(VolumeChangeListener(PlayerService.Sound.PIANO))
        flute_volume.setOnSeekBarChangeListener(VolumeChangeListener(PlayerService.Sound.FLUTE))
        grass_volume.setOnSeekBarChangeListener(VolumeChangeListener(PlayerService.Sound.GRASS))
        bowl_volume.setOnSeekBarChangeListener(VolumeChangeListener(PlayerService.Sound.BOWL))
        bird_volume.setOnSeekBarChangeListener(VolumeChangeListener(PlayerService.Sound.BIRD))
        harp_volume.setOnSeekBarChangeListener(VolumeChangeListener(PlayerService.Sound.HARP))
        om_volume.setOnSeekBarChangeListener(VolumeChangeListener(PlayerService.Sound.OM))
        rail_volume.setOnSeekBarChangeListener(VolumeChangeListener(PlayerService.Sound.RAIL))
        cat_volume.setOnSeekBarChangeListener(VolumeChangeListener(PlayerService.Sound.CAT))
        fire_volume.setOnSeekBarChangeListener(VolumeChangeListener(PlayerService.Sound.FIRE))
        tabla_volume.setOnSeekBarChangeListener(VolumeChangeListener(PlayerService.Sound.TABLA))


        fab.setOnClickListener {
            playerService?.stopPlaying()
            fab.hide()
            //fab.visibility = View.INVISIBLE
            // hide all volume bars
            arrayOf(keyboard_volume, rain_volume,thunder_volume,ocean_volume,wind_volume,music_volume,piano_volume,flute_volume,
                grass_volume,bowl_volume,bird_volume,harp_volume,om_volume,rail_volume,cat_volume,fire_volume.tabla_volume,).forEach { bar ->
                bar.visibility = View.INVISIBLE
            }
        }
    }

    private fun toggleProgressBar(progressBar: ProgressBar) {
        progressBar.visibility = if (progressBar.visibility == View.VISIBLE) View.INVISIBLE else View.VISIBLE
    }

    private fun toggleImageView(imageView: ImageView) {
        imageView.isActivated = if(imageView.isActivated == true) false else true
    }


    inner class VolumeChangeListener(private val sound: PlayerService.Sound) : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            playerService?.setVolume(sound, (progress + 1) / 20f)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}

        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
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
    }

    override fun onResume() {
        super.onResume()
        playerService?.stopForeground()
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