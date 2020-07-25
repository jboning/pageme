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

import java.util.ArrayList;

import name.jboning.pageme.config.AlertRuleEvaluator;
import name.jboning.pageme.config.ConfigManager;
import name.jboning.pageme.config.model.AlertRule;

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
        AlertRule rule = findFirstTriggeredRule(context, msg);
        if (rule == null) {
            return;
        }

        Log.d("SmsReceiver", "should alert");

        Intent alertIntent = AlertActivity.getIntent(context, msg, rule);
        alertIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            context.startActivity(alertIntent);
        } else {
            createNotificationChannel(context);

            PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(context, 0,
                    alertIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification.Builder notificationBuilder =
                    new Notification.Builder(context, ALERT_CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_notif)
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

    private AlertRule findFirstTriggeredRule(Context context, CombinedSmsMessage msg) {
        ArrayList<AlertRule> rules = new ConfigManager().getRules(context);
        for (AlertRule rule : rules) {
            if (new AlertRuleEvaluator(rule, msg).matches()) {
                return rule;
            }
        }
        return null;
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
