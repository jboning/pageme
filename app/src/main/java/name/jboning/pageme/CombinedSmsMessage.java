package name.jboning.pageme;

import android.telephony.SmsMessage;

import org.json.JSONException;
import org.json.JSONObject;

public class CombinedSmsMessage {
    private static final String ORIGINATING_ADDRESS = "originatingAddress";
    private final String originatingAddress;

    private static final String TIMESTAMP_MILLIS = "timestampMillis";
    private final long timestampMillis;

    private static final String BODY = "body";
    private final String body;

    public CombinedSmsMessage(String originatingAddress, long timestampMillis, String body) {
        this.originatingAddress = originatingAddress;
        this.timestampMillis = timestampMillis;
        this.body = body;
    }

    public static CombinedSmsMessage fromMessageArray(SmsMessage[] messages) {
        assert messages.length > 0;
        StringBuilder sb = new StringBuilder();
        for (SmsMessage msg : messages) {
            sb.append(msg.getDisplayMessageBody());
        }
        String body = sb.toString();
        return new CombinedSmsMessage(
                messages[0].getDisplayOriginatingAddress(),
                messages[0].getTimestampMillis(),
                body
        );
    }

    public String getOriginatingAddress() {
        return originatingAddress;
    }

    public String getBody() {
        return body;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put(ORIGINATING_ADDRESS, originatingAddress);
        obj.put(TIMESTAMP_MILLIS, timestampMillis);
        obj.put(BODY, body);
        return obj;
    }

    public static CombinedSmsMessage fromJson(JSONObject obj) throws JSONException {
        return new CombinedSmsMessage(
                obj.getString(ORIGINATING_ADDRESS),
                obj.getLong(TIMESTAMP_MILLIS),
                obj.getString(BODY)
        );
    }
}
