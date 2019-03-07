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
        alertIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        try {
            alertIntent.putExtra("sms", msg.toJson().toString());
        } catch (JSONException e) {
            Log.e("SmsReceiver", "error serializing sms!", e);
            return;
        }
        context.startActivity(alertIntent);
        context.startService(new Intent(context, AnnoyerService.class));
    }

    private boolean isBamruPage(CombinedSmsMessage msg) {
        String sender = msg.getOriginatingAddress();
        return (sender.equals("4157122678")
                || sender.equals("8312267814")
                || sender.equals("8312267820")
                || sender.equals("8312267823")
                || sender.equals("8312267824"));
    }

    private boolean isTransitPage(CombinedSmsMessage msg) {
        String body = msg.getBody();
        return (body.endsWith("Have you left home yet?")
                || body.endsWith("Are you home yet?"));
    }

    private boolean isResponseConfirmation(CombinedSmsMessage msg) {
        String body = msg.getBody();
        return (body.startsWith("Departure time recorded")
                || body.startsWith("Return time recorded")
                || (body.startsWith("RSVP") && body.endsWith("recorded.")));
    }

    private boolean isSmcAlertPage(CombinedSmsMessage msg) {
        String sender = msg.getOriginatingAddress();
        return sender.equals("89361");
    }

    private boolean isTestPage(CombinedSmsMessage msg) {
        String body = msg.getBody();
        return body.toLowerCase().contains("test alert")
                || body.toLowerCase().contains("test page");
    }

    private boolean shouldAlert(CombinedSmsMessage msg) {
        // TODO: make logic configurable.

        if (isBamruPage(msg)
                && !isTransitPage(msg)
                && !isResponseConfirmation(msg)) {
            return true;
        } else if (isSmcAlertPage(msg)) {
            return true;
        } else if (isTestPage(msg)) {
            return true;
        }
        return false;
    }
}
