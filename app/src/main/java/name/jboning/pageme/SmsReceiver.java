package name.jboning.pageme;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/* partly cribbed from https://github.com/babariviere/flutter_sms/blob/master/android/src/main/java/com/babariviere/sms/SmsReceiver.java */

public class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
            Log.e("SmsReceiver", "Unexpected intent!");
            return;
        }

        SmsMessage[] msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        CombinedSmsMessage msg = CombinedSmsMessage.fromMessageArray(msgs);

        Log.d("SmsReceiver", "got message!");
        if (!shouldAlert(msg)) {
            return;
        }

        Log.d("SmsReceiver", "should alert");

        Intent alertIntent = new Intent(context, AlertActivity.class);
        try {
            alertIntent.putExtra("sms", msg.toJson().toString());
        } catch (JSONException e) {
            Log.e("SmsReceiver", "error serializing sms!", e);
            return;
        }
        context.startActivity(alertIntent);
    }

    private boolean shouldAlert(CombinedSmsMessage msg) {
        // TODO: make logic configurable.

        String sender = msg.getOriginatingAddress();
        String body = msg.getBody();

        if (body.startsWith("[BAMRU]")
                && !(body.endsWith("Have you left home yet?")
                     || body.endsWith("Are you home yet?"))) {
            return true;
        } else if (sender.equals("89361") && body.toLowerCase().contains("callout")) {
            return true;
        } else if (body.toLowerCase().contains("test alert")) {
            return true;
        }
        return false;
    }
}
