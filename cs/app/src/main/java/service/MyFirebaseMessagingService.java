package service;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.johan_dp8ahsz.cs.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import activity.MainActivity;
import app.Config;
import fragment.AssignedTaskFragment;
import util.CustomRequest;
import util.NotificationUtils;
import util.WakefulReceiver;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();

    private NotificationUtils notificationUtils;
    static AlarmManager scheduler;
    SharedPreferences prefs;

    static private HashMap<String, PendingIntent> intentMap = new HashMap<>();

    @Override
    public void onCreate(){
        Log.e("firebase oncre", "oncre");
        Log.e("firebase oncre","scheduler == true: " + String.valueOf(scheduler == null));
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.e(TAG, "From: " + remoteMessage.getFrom());
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.e(TAG, "Notification Body: " + remoteMessage.getNotification().getBody());
            handleNotification(remoteMessage.getNotification().getBody());
        }

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.e(TAG, "Data Payload: " + remoteMessage.getData().toString());

            try {
                JSONObject json = new JSONObject(remoteMessage.getData().toString());
                handleDataMessage(json);
            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
            }
        }
    }

    private void handleNotification(String message) {
        if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
            // app is in foreground, broadcast the push message
            Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
            pushNotification.putExtra("message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

            // play notification sound
            NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
            notificationUtils.playNotificationSound();
        }else{
            // If the app is in background, firebase itself handles the notification
        }
    }

    private void handleDataMessage(JSONObject json) {
        Log.e(TAG, "push json: " + json.toString());

        try {
            JSONObject data = json.getJSONObject("data");

            String title = data.getString("title");
            String message = data.getString("message");
            boolean isBackground = data.getBoolean("is_background");
            String imageUrl = data.getString("image");
            String timestamp = data.getString("timestamp");
            JSONObject payload = data.getJSONObject("payload");

            Log.e(TAG, "title: " + title);
            Log.e(TAG, "message: " + message);
            Log.e(TAG, "isBackground: " + isBackground);
            Log.e(TAG, "payload: " + payload.toString());
            Log.e(TAG, "imageUrl: " + imageUrl);
            Log.e(TAG, "timestamp: " + timestamp);

         //   ShowToastInIntentService(payload.getString("type"));

            // If we have received a sensor task
            if(TextUtils.equals(payload.getString("type"), "sensor")){
                Log.e(TAG, "NEW SENSING TASK");

                int duration = Integer.parseInt(payload.getString("duration"));
                int readings = Integer.parseInt(payload.getString("readings"));
                long interval = (long) ((duration / (double)readings) * 60000); // minutes to milis

                Log.e("interval::", String.valueOf(interval));

                scheduler = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                Intent sensorTask = new Intent(getApplicationContext(), WakefulReceiver.class );

                // put some arguments for the sensor service
                Bundle args = new Bundle();
                args.putString("id", payload.getString("id"));
                args.putInt("max_iter", readings);
                args.putLong("interval", interval);
                args.putString("sensor_type", payload.getString("sensor"));
                prefs.edit().putInt("sensing_current_" + payload.getString("id"), 1).apply();
                sensorTask.putExtras(args);

                // dummy action so that the intent don't drop its extras
                sensorTask.setAction(Long.toString(System.currentTimeMillis()));

                // schedule the sensor task according to provided parameters
                PendingIntent scheduledIntent = PendingIntent.getBroadcast(
                        getApplicationContext(), 0, sensorTask, 0);

                intentMap.put(payload.getString("id"), scheduledIntent);
       //         scheduler.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, scheduledIntent);
       //         scheduler.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, interval, scheduledIntent);

                scheduler.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), scheduledIntent);
            }
            else if(TextUtils.equals(payload.getString("type"), "hit")){
                Log.e(TAG, "NEW HIT");

                //Send data via intent to AssignedTaskFragment and update the list view there
                Intent intent = new Intent(getApplicationContext(), AssignedTaskFragment.Receiver.class);
                intent.setAction("com.example.johan_dp8ahsz.cs.RECEIVED_TASK");
                Bundle args = new Bundle();
                args.putString("id", payload.getString("id"));
                args.putString("hit_type", payload.getString("hit_type"));
                args.putString("question", payload.getString("question"));
                args.putString("options", payload.getString("choices"));
                intent.putExtras(args);

                // Send intent to Assigned tasks fragment
                sendBroadcast(intent);
            }
            else if(TextUtils.equals(payload.getString("type"), "expired")){
                Log.e(TAG, "EXPIRED TASK");

                //Send data via intent to AssignedTaskFragment and update the list view there
                Intent intent = new Intent(getApplicationContext(), AssignedTaskFragment.Receiver.class);
                intent.setAction("com.example.johan_dp8ahsz.cs.EXPIRED_TASK");
                Bundle args = new Bundle();
                args.putString("id", payload.getString("id"));
                args.putString("active", "expired");
                intent.putExtras(args);

                // Send intent to Assigned tasks fragment
                sendBroadcast(intent);
            }
            else if(TextUtils.equals(payload.getString("type"), "heartbeat")){
                Log.e(TAG, "HEARTBEAT");

                // Aquire a wakelock so that we can respond if idle
                PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
                PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                        "HEARTBEAT:MyWakelockTag");
                wakeLock.acquire();

                // Only answer heartbeats if you are online
                if(TextUtils.equals(prefs.getString("is_logged_in", ""), "true")){

                    RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                    String URL = getString(R.string.HEARTBEAT_URL);

     //               GPSTracker gps = new GPSTracker(getApplicationContext());

                    HashMap<String, String> params = new HashMap<>();
                    params.put("email", prefs.getString("email", "shit"));
   //                 params.put("lat", String.valueOf( gps.getLatitude()));
     //               params.put("lng", String.valueOf( gps.getLongitude()));

     //               gps.stopUsingGPS();

                    CustomRequest jsObjRequest = new CustomRequest(Request.Method.POST, URL, params, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {}
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError response) {}
                    });
                    queue.add(jsObjRequest);

                }

                // release the lock
                wakeLock.release();
            }
            else if(TextUtils.equals(payload.getString("type"), "forced_logout")){
                Log.e(TAG, "FORCED LOGOUT");

                //Send data via intent to MainActivity
                Intent intent = new Intent(getApplicationContext(), MainActivity.Receiver.class);
                intent.setAction("com.example.johan_dp8ahsz.cs.FORCED_LOGOUT");
                Bundle args = new Bundle();
                args.putString("active", "force!");
                intent.putExtras(args);

                // Send intent to Assigned tasks fragment
                sendBroadcast(intent);
            }

            if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
                // app is in foreground, broadcast the push message
                Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
                pushNotification.putExtra("message", message);
                LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

                // play notification sound
      //          NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
      //          notificationUtils.playNotificationSound();
            } else {
                // app is in background, show the notification in notification tray
                Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
                resultIntent.putExtra("message", message);

                // check for image attachment
                if (TextUtils.isEmpty(imageUrl)) {
                    showNotificationMessage(getApplicationContext(), title, message, timestamp, resultIntent);
                } else {
                    // image is present, show notification with image
                    showNotificationMessageWithBigImage(getApplicationContext(), title, message, timestamp, resultIntent, imageUrl);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Json Exception: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    /**
     * Showing notification with text only
     */
    private void showNotificationMessage(Context context, String title, String message, String timeStamp, Intent intent) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent);
    }

    /**
     * Showing notification with text and image
     */
    private void showNotificationMessageWithBigImage(Context context, String title, String message,
                                                     String timeStamp, Intent intent, String imageUrl) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent, imageUrl);
    }

    /**
     * This class is used for cancelling of a sensor task. When we receive a broadcast here
     * we know the task is completed, and so we simply cancel the scheduler.
     */
    public static class Receiver extends BroadcastReceiver {

        public Receiver(){}

        @Override
        public void onReceive(Context context, Intent i) {
            //TODO: Check if this actually works for multiple sensor tasks (confirm that all tasks get cancelled eventually)
            // note: not sure if this is still used
            PendingIntent intent = intentMap.get(i.getStringExtra("id"));
            Log.e(TAG, "CANCELLING " + i.getStringExtra("id"));
            scheduler.cancel(intent);
        }
    }

    public void ShowToastInIntentService(final String sText) {
        final Context MyContext = this;

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                MainActivity.setToolbarTitle(sText);
            }
        });
    };
}