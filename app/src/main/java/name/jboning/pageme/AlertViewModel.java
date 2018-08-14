package name.jboning.pageme;

import android.arch.lifecycle.ViewModel;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Parcel;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

// flagrant abuse of the ViewModel framework to get a class tied to the lifetime of the activity.
public class AlertViewModel extends ViewModel implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {
    private boolean initialized = false;
    private boolean cancelled = false;
    private final Object cancelledLock = new Object();
    private Vibrator vibrator;
    private MediaPlayer mediaPlayer;
    private long initTime;

    private static final long VIBRATE_DELAY_MILLIS = 3000;
    private static final long AUDIO_DELAY_MILLIS = VIBRATE_DELAY_MILLIS + 3000;

    public void annoy(Vibrator v, MediaPlayer mp) {
        if (initialized) {
            return;
        }

        vibrator = v;
        mediaPlayer = mp;
        initialized = true;
        initTime = System.currentTimeMillis();

        AudioAttributes vibrateAttrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build();
        vibrator.vibrate(new long[]{VIBRATE_DELAY_MILLIS, 500, 100}, 1, vibrateAttrs);

        mp.setOnCompletionListener(this);
        mp.setOnPreparedListener(this);
        mp.prepareAsync();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        synchronized (cancelledLock) {
            if (cancelled) {
                return;
            }

            long remainingDelay = AUDIO_DELAY_MILLIS - (System.currentTimeMillis() - initTime);
            if (remainingDelay < 0) {
                Log.d("AlertViewModel", "Starting audio (prepared callback)");
                mp.start();
            } else {
                new Timer().schedule(new StartAudioTimerTask(), remainingDelay);
            }
        }
    }

    private class StartAudioTimerTask extends TimerTask {
        @Override
        public void run() {
            synchronized (cancelledLock) {
                if (cancelled) {
                    return;
                }

                Log.d("AlertViewModel", "Starting audio (timer callback)");
                mediaPlayer.start();
            }
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        synchronized (cancelledLock) {
            if (cancelled) {
                return;
            }

            mp.start();
        }
    }

    @Override
    protected void onCleared() {
        if (initialized) {
            synchronized (cancelledLock) {
                cancelled = true;
                vibrator.cancel();
                mediaPlayer.stop();
                mediaPlayer.release();
            }
        }
        super.onCleared();
    }
}
