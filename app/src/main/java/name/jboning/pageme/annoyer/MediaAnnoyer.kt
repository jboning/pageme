package name.jboning.pageme.annoyer

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager

class MediaAnnoyer(context: Context) : Annoyer {
    override fun isNoisy() = true

    private val lock = Object()
    private var cancelled = false
    private val mediaPlayer = MediaPlayer()

    init {
        configureMediaPlayer(context)
    }

    private fun configureMediaPlayer(context: Context) {
        val audioManager: AudioManager = context.getSystemService(AudioManager::class.java)!!
        val volumeLevel = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
        val audioUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        mediaPlayer.setDataSource(context.applicationContext, audioUri)
        mediaPlayer.setVolume(volumeLevel.toFloat(), volumeLevel.toFloat())
        mediaPlayer.setAudioAttributes(
                AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
        )
    }

    override fun start() {
        mediaPlayer.setOnCompletionListener { startMediaPlayer(it) }
        mediaPlayer.setOnPreparedListener { startMediaPlayer(it) }
        mediaPlayer.prepareAsync()
    }

    override fun stop() {
        synchronized(lock) {
            cancelled = true
            mediaPlayer.stop()
            mediaPlayer.release()
        }
    }

    private fun startMediaPlayer(mp: MediaPlayer) {
        synchronized(lock) {
            if (cancelled) {
                return
            }
            mp.start()
        }
    }
}