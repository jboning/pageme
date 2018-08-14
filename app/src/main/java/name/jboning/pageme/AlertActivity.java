package name.jboning.pageme;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;

public class AlertActivity extends AppCompatActivity {
    private CombinedSmsMessage message;
    private AlertViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            String jsonData = getIntent().getStringExtra("sms");
            message = CombinedSmsMessage.fromJson(new JSONObject(jsonData));
        } catch (JSONException e) {
            Log.e("AlertActivity", "error deserializing sms!", e);
        }
        setContentView(R.layout.activity_alert);

        TextView messageView = (TextView) findViewById(R.id.message);
        messageView.setText(message.getBody());
        TextView senderView = (TextView) findViewById(R.id.sender);
        senderView.setText("from " + message.getOriginatingAddress());

        SwipeConfirm swipe = (SwipeConfirm) findViewById(R.id.swipeConfirm);
        swipe.setOnConfirmedListener(new SwipeConfirm.OnConfirmedListener() {
            @Override
            public void onConfirmed(SwipeConfirm swipeConfirm) {
                AlertActivity.super.onBackPressed();
            }
        });

        viewModel = ViewModelProviders.of(this).get(AlertViewModel.class);
        viewModel.annoy(getSystemService(Vibrator.class), buildMediaPlayer());
    }

    @Override
    public void onBackPressed() {
        // do nothing (ignore back button press)
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
    protected void onDestroy() {
        super.onDestroy();
    }
}
