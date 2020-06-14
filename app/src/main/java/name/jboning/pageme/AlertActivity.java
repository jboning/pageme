package name.jboning.pageme;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.appcompat.app.AppCompatActivity;
import name.jboning.pageme.annoyer.AnnoyerService;

public class AlertActivity extends AppCompatActivity {
    private CombinedSmsMessage message;

    private static final String HAS_ACKNOWLEDGED = "hasAcknowledged";
    private boolean hasAcknowledged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            hasAcknowledged = savedInstanceState.getBoolean(HAS_ACKNOWLEDGED);
        }

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
                hasAcknowledged = true;
                stopService(new Intent(AlertActivity.this, AnnoyerService.class));
            }
        });
        swipe.setConfirmed(hasAcknowledged);

        if (!hasAcknowledged) {
            startService(new Intent(this, AnnoyerService.class));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(HAS_ACKNOWLEDGED, hasAcknowledged);
    }

    @Override
    public void onBackPressed() {
        // Only react to back button once the user has acknowledged the alert.
        if (hasAcknowledged) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
