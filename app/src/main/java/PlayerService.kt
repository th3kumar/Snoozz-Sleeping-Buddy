import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.fridayhouse.snoozz.CustomActivity
import com.fridayhouse.snoozz.R
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

class PlayerService : Service() {



    private val notificationID = 132
    private val tag = "Player"
    //lateinit var trackSelector: DefaultTrackSelector

    // called when sound is started or stopped
    var playerChangeListener: (() -> Unit)? = null

    inner class PlayerBinder : Binder() {
        fun getService(): PlayerService {
            return this@PlayerService
        }
    }

    private val playerBinder = PlayerBinder()

    override fun onCreate() {
        // load each player into the map
        Sound.values().forEach {
            exoPlayers[it] = initializeExoPlayer(it.file)
        }
    }

    enum class Sound(val file: String) {
        KEYBOARD("keyboard_sound.ogg"),
        RAIN("rain_sound.ogg"),
        THUNDER("thunder_sound.ogg"),
        OCEAN("ocean_sound.ogg"),
        WIND("wind_sound.ogg"),
        MUSIC("music_sound.ogg"),
        PIANO("piano_sound.ogg"),
        FLUTE("flute_sound.ogg"),
        GRASS("grass_sound.ogg"),
        BOWL("singing_bowl.ogg"),
        BIRD("birds_sound.ogg"),
        HARP("harp_sound.ogg"),
        OM("om_sound.ogg"),
        RAIL("rail_sound.ogg"),
        CAT("cat_purr_sound.ogg"),
        FIRE("fire_sound.ogg"),
        TABLA("tabla_sound.ogg")
    }

    private val exoPlayers = mutableMapOf<Sound, ExoPlayer>()

    private fun initializeExoPlayer(soundFile: String): ExoPlayer {

        // create the player
        val trackSelector = DefaultTrackSelector(this)
        val exoPlayer = ExoPlayer.Builder(this).setTrackSelector(trackSelector).build()

        // load the media source
        val dataSource = DefaultDataSource.Factory(this,
            Util.getUserAgent(this, this.getString(R.string.app_name)))

        val mediaSource = ProgressiveMediaSource.Factory(dataSource)
            .createMediaSource(MediaItem.fromUri(Uri.parse("asset:///$soundFile")))

        // load the media
        Log.d("MAIN", "loading $soundFile")
        exoPlayer.setMediaSource(mediaSource)
        exoPlayer.prepare()
        exoPlayer.play()
        // loop indefinitely
        exoPlayer.repeatMode = Player.REPEAT_MODE_ALL

        return exoPlayer
    }

    override fun onUnbind(intent: Intent?): Boolean {
        // don't continue if we're not playing any sound and the main activity exits
        playerChangeListener = null
        if (!isPlaying()) {
            stopSelf()
            Log.d(tag, "stopping service")
        }
        return super.onUnbind(intent)
    }

    override fun onBind(intent: Intent?): IBinder {
        // return the binding interface
        return playerBinder
    }

    fun startForeground() {
        // move to the foreground if we are playing sound
        if (isPlaying()) {
            val notificationIntent = Intent(this, CustomActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,0 )

            val notification = NotificationCompat.Builder(this, "softsound")
                .setContentTitle(getText(R.string.app_name))
                .setContentText(getText(R.string.notification_message))
                .setSmallIcon(R.drawable.ic_volume)
                .setContentIntent(pendingIntent)
                .build()

            Log.d(tag, "starting foreground service...")
            startForeground(notificationID, notification)
        }
    }

    fun stopForeground() {
        // we don't need to be foreground anymore
        Log.d(tag, "stopping foreground service...")
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    fun stopPlaying() {
        exoPlayers.values.forEach { it.playWhenReady = false }
    }

    fun isPlaying(): Boolean {
        var playing = false
        exoPlayers.values.forEach { if (it.playWhenReady) playing = true }
        return playing
    }

    fun setVolume(sound: Sound, volume: Float) {
        exoPlayers[sound]?.volume = volume
    }

    fun toggleSound(sound: Sound) {
        exoPlayers[sound]?.playWhenReady = !(exoPlayers[sound]?.playWhenReady ?: false)
        // call the change listener
        playerChangeListener?.invoke()
    }

}