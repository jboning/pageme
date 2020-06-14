package name.jboning.pageme.annoyer

import android.content.Context
import android.media.AudioAttributes
import android.os.Vibrator
import java.util.*

class VibrateAnnoyer(context: Context) : Annoyer {
    override fun isNoisy() = false

    private val lock = Object()
    private var cancelled = false
    private val vibrator: Vibrator = context.getSystemService(Vibrator::class.java)!!
    private val reVibrateTimer = Timer()

    override fun start() {
        vibrate()
        // If something else (e.g. a notification from some other app) uses the vibrator, our
        // repeating vibration will be cancelled. So, periodically kick it off again.
        reVibrateTimer.schedule(ReVibrateTimerTask(), 6000)
    }

    override fun stop() {
        synchronized(lock) {
            cancelled = true
            reVibrateTimer.cancel()
            vibrator.cancel()
        }
    }

    private fun vibrate() {
        val vibrateAttrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()
        vibrator.vibrate(longArrayOf(0, 500, 100), 1, vibrateAttrs)
    }

    private inner class ReVibrateTimerTask : TimerTask() {
        override fun run() {
            synchronized(lock) {
                if (cancelled) {
                    return
                }
                vibrator.cancel()
                vibrate()
            }
        }
    }
}