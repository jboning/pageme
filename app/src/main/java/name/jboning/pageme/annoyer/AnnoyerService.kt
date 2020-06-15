package name.jboning.pageme.annoyer

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import name.jboning.pageme.MainViewModel
import name.jboning.pageme.config.ConfigManager
import name.jboning.pageme.config.model.AnnoyerPolicy
import java.util.*

class AnnoyerService : Service() {
    private val lock = Object()
    private var initialized = false
    private var cancelled = false
    private val annoyers = mutableListOf<Annoyer>()
    private val timer = Timer()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        annoy()
        return START_NOT_STICKY
    }

    private fun annoy() {
        if (initialized) {
            return
        }
        initialized = true
        val policy = ConfigManager().getAnnoyerPolicy(this)
        if (policy == null || policy.actions.isEmpty()) {
            Log.d("AnnoyerService", "No notification actions! Not annoying.")
            return
        }
        Log.d("AnnoyerService", "Annoying")
        schedule(policy, 0)
    }

    private fun schedule(policy: AnnoyerPolicy, step: Int) {
        timer.schedule(AnnoyerTimerTask(policy, step), policy.actions[step].delay_ms)
    }

    private inner class AnnoyerTimerTask(val policy: AnnoyerPolicy, val step: Int): TimerTask() {
        override fun run() {
            synchronized(lock) {
                if (cancelled) {
                    return
                }
                runStep(policy, step)
                if (step + 1 < policy.actions.size) {
                    schedule(policy, step + 1)
                }
            }
        }
    }

    private fun runStep(policy: AnnoyerPolicy, step: Int) {
        Log.d("AnnoyerService", "Annoyer performing step $step")
        val action = policy.actions[step]
        val annoyer = when (action.action) {
            AnnoyerPolicy.NotificationAction.VIBRATE -> VibrateAnnoyer(this@AnnoyerService)
            AnnoyerPolicy.NotificationAction.FLASH_TORCH -> TorchAnnoyer(this@AnnoyerService)
            AnnoyerPolicy.NotificationAction.PLAY_SOUND -> MediaAnnoyer(this@AnnoyerService)
        }
        annoyers.add(annoyer)
        if (!(annoyer.isNoisy() && isSilenced())) {
            annoyer.start()
        }
    }

    private fun isSilenced(): Boolean {
        val prefs = getSharedPreferences(MainViewModel.SHARED_PREFS, Context.MODE_PRIVATE)
        val silenceUntil = prefs.getLong(MainViewModel.PREF_SILENCE_UNTIL, -1)
        return silenceUntil > System.currentTimeMillis()
    }

    override fun onDestroy() {
        Log.d("AnnoyerService", "Destroying")
        synchronized(lock) {
            cancelled = true
            annoyers.forEach {
                it.stop()
            }
        }
        super.onDestroy()
    }
}