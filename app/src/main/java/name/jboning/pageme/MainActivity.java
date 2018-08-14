package name.jboning.pageme;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
        implements DurationPickerFragment.OnDurationSetListener {
    SharedPreferences sharedPreferences;

    public static final String SHARED_PREFS = "prefs";
    public static final String PREF_SILENCE_UNTIL = "silenceUntil";

    private Timer timer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button testAlertButton = (Button) findViewById(R.id.testAlert);
        testAlertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent i = new Intent(MainActivity.this, AlertActivity.class);
                CombinedSmsMessage m = new CombinedSmsMessage("example@example.com", 1, "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.");
                try {
                    i.putExtra("sms", m.toJson().toString());
                } catch (JSONException e) {
                    Log.e("SmsReceiver", "error serializing sms!", e);
                    return;
                }
                (new Timer()).schedule(new TimerTask() {
                    @Override
                    public void run() {
                        MainActivity.this.startActivity(i);
                    }
                }, 1000);
            }
        });

        sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        renderAudioStatus();

        Button editAudioButton = (Button) findViewById(R.id.editAudioStatus);
        editAudioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAudioEditDialog();
            }
        });

        permissionsCheck();
    }

    private void renderAudioStatus() {
        if (timer == null) {
            return;
        }
        timer.cancel();
        timer = new Timer();

        TextView audioStatusText = (TextView) findViewById(R.id.audioStatus);
        long silenceUntil = sharedPreferences.getLong(PREF_SILENCE_UNTIL, -1);
        if (silenceUntil > System.currentTimeMillis()) {
            DateFormat formatter = new SimpleDateFormat("HH:mm");
            audioStatusText.setText("silenced until " + formatter.format(new Date(silenceUntil)));
            audioStatusText.setTextColor(getColor(R.color.audioOff));
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            renderAudioStatus();
                        }
                    });
                }
            }, silenceUntil - System.currentTimeMillis());
        } else {
            audioStatusText.setText("enabled");
            audioStatusText.setTextColor(getColor(R.color.audioOn));
        }
    }

    private void showAudioEditDialog() {
        DialogFragment f = new DurationPickerFragment();
        f.show(getSupportFragmentManager(), "duration");
    }

    @Override
    public void onDurationSet(int minutes) {
        updateAudioSilencePref(System.currentTimeMillis() + (1000 * 60 * minutes));
    }

    @Override
    public void onDurationCleared() {
        updateAudioSilencePref(-1);
    }

    private void updateAudioSilencePref(long until) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(PREF_SILENCE_UNTIL, until);
        editor.apply();
        renderAudioStatus();
    }

    private void permissionsCheck() {
        Log.d("MainActivity", "checking permissions");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MainActivity", "requesting permissions");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.VIBRATE},
                    0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsCheck();
    }

    @Override
    protected void onDestroy() {
        timer.cancel();
        timer = null;
        super.onDestroy();
    }
}
