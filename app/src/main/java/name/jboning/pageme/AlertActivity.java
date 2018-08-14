package name.jboning.pageme;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class AlertActivity extends AppCompatActivity {
    private CombinedSmsMessage message;

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

        startService(new Intent(this, AnnoyerService.class));
    }

    @Override
    public void onBackPressed() {
        // do nothing (ignore back button press)
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, AnnoyerService.class));
        super.onDestroy();
    }
}
