package name.jboning.pageme.annoyer

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import java.util.*

class TorchAnnoyer(context: Context) : Annoyer {
    override fun isNoisy() = false

    private val TORCH_PERIOD_MILLIS = 500L

    private val lock = Object()
    private var cancelled = false
    private val cameraManager = context.getSystemService(CameraManager::class.java)!!
    private val timer = Timer()
    private var torchesOn = false;

    override fun start() {
        timer.schedule(TorchTimerTask(), 0L, TORCH_PERIOD_MILLIS)
    }

    override fun stop() {
        synchronized(lock) {
            cancelled = true
            timer.cancel()
            setTorches(false)
        }
    }

    private inner class TorchTimerTask : TimerTask() {
        override fun run() {
            synchronized(lock) {
                if (cancelled) {
                    return
                }
                toggleTorches()
            }
        }
    }

    private fun toggleTorches() {
        torchesOn = !torchesOn
        setTorches(torchesOn);
    }

    private fun setTorches(on: Boolean) {
        val cameras = try {
            cameraManager.cameraIdList
        } catch (e: CameraAccessException) {
            return
        }
        for (c in cameras) {
            try {
                if (cameraManager.getCameraCharacteristics(c).get(CameraCharacteristics.FLASH_INFO_AVAILABLE)!!) {
                    cameraManager.setTorchMode(c, on)
                }
            } catch (e: CameraAccessException) {
                // ignore
            }
        }
    }
}