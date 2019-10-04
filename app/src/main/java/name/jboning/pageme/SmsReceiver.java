package name.jboning.pageme;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

import org.json.JSONException;

/* partly cribbed from https://github.com/babariviere/flutter_sms/blob/master/android/src/main/java/com/babariviere/sms/SmsReceiver.java */

public class SmsReceiver extends BroadcastReceiver {

    private static final String ALERT_CHANNEL_ID = "PAGE_ALERT_CHANNEL";
    private static final int ALERT_NOTIFICATION_ID = 1;

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

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            context.startActivity(alertIntent);
        } else {
            createNotificationChannel(context);

            PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(context, 0,
                    alertIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification.Builder notificationBuilder =
                    new Notification.Builder(context, ALERT_CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_launcher_foreground)
                            .setContentTitle("Incoming alert!")
                            .setContentText(msg.getBody())
                            .setCategory(Notification.CATEGORY_ALARM)

                            // Use a full-screen intent only for the highest-priority alerts where you
                            // have an associated activity that you would like to launch after the user11
                            // interacts with the notification. Also, if your app targets Android 10
                            // or higher, you need to request the USE_FULL_SCREEN_INTENT permission in
                            // order for the platform to invoke this notification.
                            .setFullScreenIntent(fullScreenPendingIntent, true);

            Notification alertNotification = notificationBuilder.build();

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.notify(ALERT_NOTIFICATION_ID, alertNotification);
        }
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
                || body.startsWith("Departure time cleared")
                || body.startsWith("Return time recorded")
                || body.startsWith("Return time cleared")
                || ((body.startsWith("RSVP") || body.startsWith("Response"))
                    && (body.endsWith("recorded.") || body.endsWith("successful."))));
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

    private void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.alert_channel_name);
            String description = context.getString(R.string.alert_channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(ALERT_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
