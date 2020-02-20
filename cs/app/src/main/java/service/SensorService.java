package service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import androidx.annotation.Nullable;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.johan_dp8ahsz.cs.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import util.CustomRequest;
import util.SystemUtils;
import util.WakefulReceiver;

public class SensorService extends IntentService implements SensorEventListener {

    private static final String TAG = "SENSOR_SERVICE";

    private Intent intent;

    private SensorManager sensorManager = null;
    private int sensorType = Sensor.TYPE_LIGHT; // default sensor type
    private String sensorName = "light"; // default

    private int maxIterations;
    private int currentIteration;
    private long interval;

    private static final String SENSOR_TYPE = "sensor_type";
    private static final String MAX_ITERATOINS = "max_iter";
    private static String id;

    //Networking
    private RequestQueue queue;
    private String UPDATE_TASK_URL;

    SharedPreferences prefs;

    // Map with all sensor types
    HashMap<String, Integer> sensorMap;

    public SensorService() {
        super("MyIntentService");
    }
/*
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        Log.e(TAG, "I AM INSIDE sesseessss");

        UPDATE_TASK_URL = getString(R.string.UPDATE_TASK_URL);
        queue = Volley.newRequestQueue(getApplicationContext());
        Bundle args = intent.getExtras();
        sensorMap = SystemUtils.readSensorTypesMap(this);

        if(args != null){
            // Get the desired sensor type, if any
            if(args.containsKey(SENSOR_TYPE)){
                sensorType = sensorMap.get(args.getString(SENSOR_TYPE));
                sensorName = args.getString(SENSOR_TYPE)    ;
            }

            id = args.getString("id");
            maxIterations = args.getInt(MAX_ITERATOINS);
            currentIteration = prefs.getInt("sensing_current_" + id, 0);
            interval = args.getLong("interval");
        }

        Sensor sensor = sensorManager.getDefaultSensor(sensorType);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        return START_STICKY;
    }
*/
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        this.intent = intent;

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

     //   Log.e(TAG, "I AM INSIDE sesseessss");

        UPDATE_TASK_URL = getString(R.string.UPDATE_TASK_URL);
        queue = Volley.newRequestQueue(getApplicationContext());
        Bundle args = intent.getExtras();
        sensorMap = SystemUtils.readSensorTypesMap(this);

        if(args != null){
            // Get the desired sensor type, if any
            if(args.containsKey(SENSOR_TYPE)){
                sensorType = sensorMap.get(args.getString(SENSOR_TYPE));
                sensorName = args.getString(SENSOR_TYPE)    ;
            }

            id = args.getString("id");
            maxIterations = args.getInt(MAX_ITERATOINS);
            currentIteration = prefs.getInt("sensing_current_" + id, 0);
            interval = args.getLong("interval");
        }

        Sensor sensor = sensorManager.getDefaultSensor(sensorType);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // ignore since this service is not linked to an activity
        return null;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do nothing
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // grab the values and timestamp - off the main thread
        new SensorEventLoggerTask().execute(event);

        // stop the service
        prefs.edit().putInt("sensing_current_" + id, currentIteration + 1).apply();

        sensorManager.unregisterListener(this);
        stopSelf();
    }

    private class SensorEventLoggerTask extends AsyncTask<SensorEvent, Void, Void> {
        @Override
        protected Void doInBackground(SensorEvent... events) {
            SensorEvent event = events[0];

            // get the current value
            String sensorValue = prefs.getString(sensorType + "_" + id, "");

            // update the value with new values from appropriate sensor
            /*
            StringBuilder sb = new StringBuilder();
            sb.append(sensorValue);
            for(int i = 0; i < event.values.length - 1; i ++){
                sb.append(String.valueOf(event.values[i]));
                sb.append("_");
            }
            sb.append(String.valueOf(event.values[event.values.length-1]));
            */



            if (currentIteration < maxIterations)sensorValue += event.values[0] + "_" ;
            else sensorValue += event.values[0];


        //    ShowToastInIntentService(String.valueOf(sensorValue));

            // Update shared preferences with the new value
            prefs.edit().putString(sensorType + "_" + id, sensorValue).apply();

            Log.e(TAG + " values::", id + " = "+sensorValue);

            // if currentIteration >= maxIterations we are done
            if(currentIteration < maxIterations){
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

                Intent sensorTask = new Intent(getApplicationContext(), SensorService.class );

                // put some arguments for the sensor service
                Bundle args = new Bundle();
                args.putString("id", id);
                args.putInt("max_iter", maxIterations);
                args.putLong("interval", interval);
                args.putString("sensor_type",sensorName);
                sensorTask.putExtras(args);

                // dummy action so that the intent don't drop its extras
                sensorTask.setAction(Long.toString(System.currentTimeMillis()));

                // schedule the sensor task according to provided parameters
                PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0,
                        sensorTask, PendingIntent.FLAG_UPDATE_CURRENT);

                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval, pendingIntent);
            }
            else{

                // clean up in shared preferences
                prefs.edit().remove("sensing_current_" + id).apply();
                prefs.edit().remove(sensorType + "_" + id).apply();
/*
                // Cancel the repeating alarm
                Intent intent = new Intent(getApplicationContext(), MyFirebaseMessagingService.Receiver.class);
                intent.setAction("com.example.johan_dp8ahsz.cs.CANCEL_ALARM");
                intent.putExtra("id", id);
                sendBroadcast(intent);
*/
                // Send the data to the server
                HashMap<String, String> params = new HashMap<>();
                params.put("data", sensorValue);
                params.put("type", sensorName);
                params.put("id", id);
                params.put("file", "sensor.php");
                params.put("email", prefs.getString("email", "something fucked up"));

                CustomRequest jsObjRequest = new CustomRequest(Request.Method.POST, UPDATE_TASK_URL,
                        params, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.e("Response", response.getString("status"));
                            String status = response.getString("status");
                            if( status.equals("OK") ){ // Everything's ok!
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError response) {
                        Log.d(TAG + "response", response.toString());
                    }
                });

                queue.add(jsObjRequest);

            }


            // Release the wakelock acquired when starting this service
            WakefulReceiver.completeWakefulIntent(intent);

            return null;
        }
    }

    public void ShowToastInIntentService(final String sText) {
        final Context MyContext = this;

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                SystemUtils.displayToast(MyContext, sText);
            }
        });
    };

}