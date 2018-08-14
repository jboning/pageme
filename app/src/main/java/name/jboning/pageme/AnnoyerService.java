package name.jboning.pageme;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class AnnoyerService extends Service
        implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    private static final long VIBRATE_DELAY_MILLIS = 3000;
    private static final long AUDIO_DELAY_MILLIS = VIBRATE_DELAY_MILLIS + 3000;

    private boolean initialized = false;
    private boolean cancelled = false;
    private final Object cancelledLock = new Object();
    private Vibrator vibrator;
    private MediaPlayer mediaPlayer;
    private long initTime;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("AnnoyerService", "Launched");
        annoy();
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void annoy() {
        if (initialized) {
            return;
        }

        Log.d("AnnoyerService", "Annoying");

        vibrator = getSystemService(Vibrator.class);
        mediaPlayer = buildMediaPlayer();
        initialized = true;
        initTime = System.currentTimeMillis();

        AudioAttributes vibrateAttrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build();
        vibrator.vibrate(new long[]{VIBRATE_DELAY_MILLIS, 500, 100}, 1, vibrateAttrs);

        SharedPreferences prefs = getSharedPreferences(MainActivity.SHARED_PREFS, Context.MODE_PRIVATE);
        long silenceUntil = prefs.getLong(MainActivity.PREF_SILENCE_UNTIL, -1);
        if (silenceUntil > System.currentTimeMillis()) {
            return;
        }

        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.prepareAsync();
    }

    private MediaPlayer buildMediaPlayer() {
        MediaPlayer p = new MediaPlayer();
        AudioManager audioManager= (AudioManager) getSystemService(AUDIO_SERVICE);
        try {
            int volumeLevel=audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
            Uri audioUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            p.setDataSource(getApplicationContext(), audioUri);
            p.setVolume(volumeLevel,volumeLevel);
            p.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .build()
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return p;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        synchronized (cancelledLock) {
            if (cancelled) {
                return;
            }

            long remainingDelay = AUDIO_DELAY_MILLIS - (System.currentTimeMillis() - initTime);
            if (remainingDelay < 0) {
                Log.d("AnnoyerService", "Starting audio (prepared callback)");
                mp.start();
            } else {
                new Timer().schedule(new AnnoyerService.StartAudioTimerTask(), remainingDelay);
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

                Log.d("AnnoyerService", "Starting audio (timer callback)");
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
    public void onDestroy() {
        Log.d("AnnoyerService", "Destroying");
        if (initialized) {
            synchronized (cancelledLock) {
                cancelled = true;
                vibrator.cancel();
                mediaPlayer.stop();
                mediaPlayer.release();
            }
        }
        super.onDestroy();
    }
}
