package util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SystemUtils {

    public SystemUtils(){}

    public static boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    public static boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4 && password.length() < 33;
    }

    public static boolean isUsernameValid(String username) {
        //TODO: Replace this with your own logic
        return username.length() > 2 && username.length() < 17;
    }

    /**
     * Returns the list of available sensors for this device
     * @param context
     * @return
     */
    public static ArrayList<String> getDeviceSensors(Activity context){
        ArrayList<String> sensorList = new ArrayList<>();
        // Get the SensorManager
        SensorManager mSensorManager= (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        // List of Sensors Available
        List<Sensor> mSensorList = mSensorManager.getSensorList(Sensor.TYPE_ALL);

        Sensor temp;
        String tmp;
        int i;
        for (i=0;i<mSensorList.size();i++){
            temp = mSensorList.get(i);
            tmp = temp.getStringType(); // get the sensor type, i.e android.sensor.light

            if(!tmp.isEmpty()){
                String[] tokens = tmp.split("sensor.");
                sensorList.add(tokens[1].replace("_", " ")); // Add the sensor type to the list of sensors available
            }
        }

        return sensorList;
    }

    /**
     * Returns a map with all possible sensor types with their respective value
     * @param context
     * @return
     * @throws IOException
     */
    public static HashMap<String, Integer> readSensorTypesMap(Context context) {

        HashMap<String, Integer> map = new HashMap<>();
        String line;

        AssetManager assetManager = context.getAssets();

        try {
            InputStream is = assetManager.open("sensorTypes");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            reader.readLine(); // skip first line as it only contains instructions
            while((line = reader.readLine()) != null) {
                String[] tokens = line.split(" ");
                map.put(tokens[0], Integer.parseInt(tokens[1]));
            }

            is.close();
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return map;
    }

    /**
     * Returns a list with all possible sensor types
     * @param context
     * @return
     */
    public static ArrayList<String> readSensorTypesList(Context context) {

        ArrayList<String> list = new ArrayList<>();
        String line;

        AssetManager assetManager = context.getAssets();

        try {
            InputStream is = assetManager.open("sensorTypes");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            reader.readLine(); // skip first line as it only contains instructions
            while((line = reader.readLine()) != null) {
                String[] tokens = line.split(" ");
                list.add(tokens[0]);
            }

            is.close();
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }


    public static ArrayList<String> getHITtypesFromAssets(String file, Context context){

        ArrayList<String> list = new ArrayList<>();
        try{
            // Read the file
            AssetManager assetManager = context.getAssets();
            InputStream is = assetManager.open(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            String line;
            reader.readLine(); // skip first line as it only contains instructions
            while((line = reader.readLine()) != null) {
                list.add(line);
            }

            is.close();
            reader.close();
        } catch (IOException e) {
            Log.e("EXEPTION", e.toString());
        }
        return list;
    }



    /**
     * Shows the progress UI and hides the current form.
     */
    public static void showProgress(final boolean show, Context context, final View progressView, final View formView) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        int shortAnimTime = context.getResources().getInteger(android.R.integer.config_shortAnimTime);

        formView.setVisibility(show ? View.GONE : View.VISIBLE);
        formView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                formView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        progressView.setVisibility(show ? View.VISIBLE : View.GONE);
        progressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });

    }

    /**
     * Function for displaying a toast
     * @param context the context to show the toast in
     * @param message what message to show
     */
    public static void displayToast(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Check if there is a network connection available.
     * @param context the context.
     * @return return true if these is a connection, otherwise false.
     */
    public static boolean isNetworkAvailable(Activity context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
