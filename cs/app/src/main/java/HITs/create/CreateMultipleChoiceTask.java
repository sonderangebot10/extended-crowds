package HITs.create;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Point;
import android.location.Location;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.johan_dp8ahsz.cs.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import activity.LoginActivity;
import activity.MainActivity;
import app.Config;
import fragment.CreateTaskFragment;
import interfaces.CreateHITInterface;
import util.CustomRequest;
import util.SystemUtils;


public class CreateMultipleChoiceTask implements CreateHITInterface, OnMapReadyCallback, GoogleMap.OnMapClickListener,
        GoogleMap.OnMyLocationButtonClickListener {

    private final String TAG = "MULTIPLE CHOICE";
    private final int DELAY = Config.DELAY;

    // UI references
    private View mProgressView;
    private View mCreateHitForm;
    private LinkedList<LinearLayout> optionFields;
    private LinearLayout mOptionsLayout;
    private MapView mMapView;


    // Map references
    GoogleMap map;
    Marker marker;
    Location mLastKnownLocation;
    ArrayList<LatLng> positions;

    // A default location (Stockholm, Sweden) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(59.3262896669483, 18.072013556957245);
    private static final int DEFAULT_ZOOM = 10;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;


    //Networking
    private RequestQueue queue;
    private String CREATE_TASK_URL;
    private String POSITIONS_URL;
    private boolean connected = false;

    private SharedPreferences prefs;
    private Activity context;

    @Override
    public void createUI(LinearLayout mainLayout, View[] list, final activity.MainActivity context){

        // Initialize
        final EditText mQuestionView = new EditText(context);
        TextView mOptionsLabel = new TextView(context);
        mOptionsLayout = new LinearLayout(context);
        ImageButton mAddOptionButton = new ImageButton(context);
        Button mCreateButton = new Button(context);

        TextView mMapLabel = new TextView(context);
        mMapView = new MapView(context);
        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);

        mProgressView = list[0];
        mCreateHitForm = list[1];
        optionFields = new LinkedList<>();

        queue = Volley.newRequestQueue(context);
        CREATE_TASK_URL = context.getString(R.string.CREATE_TASK_URL);
        POSITIONS_URL = context.getString(R.string.POSITIONS_URL);
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.context = context;

        positions = new ArrayList<>();
        if(Config.SHOW_USERS_ON_MAP == true) getPositions();

        // Change the toolbar title to something appropriate.
        MainActivity.setToolbarTitle(context.getString(R.string.hit_multiple_choice_task));

        // Layout parameters to have width stretch the screen and height to wrap content
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        // Question
        mQuestionView.setHint(context.getString(R.string.hit_question));
        mQuestionView.setSingleLine(true);
        mQuestionView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mQuestionView.setTextColor(Color.WHITE);
        mQuestionView.setLayoutParams(lp);

        InputFilter[] filterArray = new InputFilter[1]; // set maximum nbr of characters
        filterArray[0] = new InputFilter.LengthFilter(99);
        mQuestionView.setFilters(filterArray);


        // MapView
        mMapLabel.setTextColor(Color.WHITE);
        mMapLabel.setText("Choose a location:");
        mMapLabel.setTextSize(20);

        // Layout parameters to have width stretch the screen and height to wrap content
        Display display = context.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, size.y/3);

        mMapView.setLayoutParams(lp2);
        mMapView.onCreate(null);
        mMapView.onResume();
        // Gets to GoogleMap from the MapView and does initialization stuff
        mMapView.getMapAsync(this);
        MapsInitializer.initialize(context);

        // Label for option fields
        mOptionsLabel.setTextColor(Color.WHITE);
        mOptionsLabel.setText(context.getString(R.string.hit_add_option));
        mOptionsLabel.setTextSize(20);

        // Options layout
        mOptionsLayout.setOrientation(LinearLayout.VERTICAL);

        // Add options button
        mAddOptionButton.setBackgroundResource(R.mipmap.add_option_button);
        LinearLayout.LayoutParams bl = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mAddOptionButton.setLayoutParams(bl);
        mAddOptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addEditText(context);
            }
        });

        // Create button
        mCreateButton.setText(context.getString(R.string.create_task));
        mCreateButton.setTextSize(20);
        mCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String question = mQuestionView.getText().toString();
                // There should be at least 2 options
                if (optionFields.size() < 2){
                    SystemUtils.displayToast(context, context.getString(R.string.error_insufficient_options));
                }
                else if(TextUtils.equals(question, "")){
                    SystemUtils.displayToast(context, context.getString(R.string.hit_error_question));
                }
                else if (marker == null){
                    SystemUtils.displayToast(context, context.getString(R.string.hit_error_location));
                }
                else{
                    String description, options = "";
                    LatLng point = marker.getPosition();

                    question = mQuestionView.getText().toString();
                    for(int i = 0; i < optionFields.size(); i++){
                        EditText t = (EditText) optionFields.get(i).getChildAt(1);
                        options += ";" + t.getText().toString();
                    }
                    options = options.replaceFirst(";","");

                    SystemUtils.showProgress(true, context, mProgressView, mCreateHitForm);
                    HashMap<String, String> params = new HashMap<>();
                    params.put("email", prefs.getString("email", "shit"));
                    params.put("type", "hit");
                    params.put("hit_type", "multiple");
                    params.put("description", "empty");
                    params.put("question", question);
                    params.put("answer_choices", options);
                    params.put("file", "multiple_choice.php");
                    params.put("lat", String.valueOf(point.latitude));
                    params.put("lng", String.valueOf(point.longitude));

                    CustomRequest jsObjRequest = new CustomRequest(Request.Method.POST, CREATE_TASK_URL,
                            params, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                SystemUtils.showProgress(false, context, mProgressView, mCreateHitForm);
                                connected = true;

                                Log.e(TAG, "GOT A RESPONSE");
                                String status = response.getString("status");
                                if( status.equals("OK") ){ // Everything's ok!
                                    SystemUtils.displayToast(context, context.getString(R.string.task_success));

                                    // Jump to somewhere more appropriate
                                    Fragment fragment = new CreateTaskFragment();
                                    final FragmentTransaction ft = context.getFragmentManager().beginTransaction();
                                    ft.replace(R.id.frame_container, fragment, "Create Task");
                                    ft.commit();
                                }
                                else{ // something went wrong
                                    SystemUtils.displayToast(context, response.getString("reason"));
                                    Log.e(TAG, response.getString("reason"));

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError response) {
                            Log.d(TAG + " ERR", response.toString());
                        }
                    });
                    queue.add(jsObjRequest);

                    // Remove the spinner after DELAY seconds, and show a message if we have not been
                    // able to communicate with the server.
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(!connected){
                                SystemUtils.showProgress(false, context, mProgressView, mCreateHitForm);
                                SystemUtils.displayToast(context, context.getString(R.string.error_cant_connect_to_server));

                                Intent i = new Intent(context, LoginActivity.class);
                                context.startActivity(i);
                                context.finish();
                            }
                        }
                    }, DELAY);

                }
            }
        });


        // Add all views to the main layout
        mainLayout.addView(mQuestionView);
        mainLayout.addView(mMapLabel);
        mainLayout.addView(mMapView);
        mainLayout.addView(mOptionsLabel);
        mainLayout.addView(mOptionsLayout);
        mainLayout.addView(mAddOptionButton);
        mainLayout.addView(mCreateButton);

    }

    /**
     * This function adds a layout with an option field with a delete button to the main
     * layout.
     */
    private void addEditText(Context context){

        LinearLayout textLayout = new LinearLayout(context);
        textLayout.setOrientation(LinearLayout.HORIZONTAL);

        // text field
        EditText text = new EditText(context);
        text.setTextColor(Color.WHITE);
        text.setTextSize(20);
        text.setMaxLines(1);
        text.setHint(context.getString(R.string.hit_task_option));
        text.setSingleLine();
        text.setImeOptions(EditorInfo.IME_ACTION_DONE);

        InputFilter[] filterArray = new InputFilter[1]; // set maximum nbr of characters
        filterArray[0] = new InputFilter.LengthFilter(35);
        text.setFilters(filterArray);


        // remove button
        ImageButton button = new ImageButton(context);
        button.setBackgroundResource(R.mipmap.remove_option_button);

        // action listeners
        button.setOnClickListener(clickListener(textLayout));

        // Add views to their respective layouts
        textLayout.addView(button);
        textLayout.addView(text);

        optionFields.add(textLayout);
        mOptionsLayout.addView(textLayout);
    }

    /**
     * When pressed, the provided layout is deleted from the main layout
     * @param layout layout to delete
     * @return Returns a new OnClickListener
     */
    private ImageButton.OnClickListener clickListener(final LinearLayout layout){
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                optionFields.remove(layout);
                mOptionsLayout.removeView(layout);
            }
        };
    }


    @Override
    public void onMapClick(LatLng point) {
        // We onlu want one marker on the map
        if(marker != null){
            marker.remove();
        }
        marker = map.addMarker(new MarkerOptions().position(new LatLng(point.latitude, point.longitude)));

    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setMapToolbarEnabled(false);
        map.setOnMyLocationButtonClickListener(this);
        map.setOnMapClickListener(this);

        try {
            map.setMyLocationEnabled(true);

            // get the current location of the device
            Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(context, new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        mLastKnownLocation = task.getResult();
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(mLastKnownLocation.getLatitude(),
                                        mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                    }

                    else {
                        Log.d(TAG, "Current location is null. Using defaults.");
                        Log.e(TAG, "Exception: %s", task.getException());

                        map.moveCamera(CameraUpdateFactory
                                .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                        map.getUiSettings().setMyLocationButtonEnabled(false);
                    }
                }
            });


            for(LatLng pos : positions){
                map.addMarker(new MarkerOptions().position(pos).icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            }

        }catch(SecurityException e){
            Log.e("Exception: %s", e.getMessage());
        }
    }


    private void getPositions(){


        SystemUtils.showProgress(true, context, mProgressView, mCreateHitForm);
        HashMap<String, String> params = new HashMap<>();
        params.put("email", prefs.getString("email", "shit"));

        CustomRequest jsObjRequest = new CustomRequest(Request.Method.POST, POSITIONS_URL,
                params, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    SystemUtils.showProgress(false, context, mProgressView, mCreateHitForm);
                    connected = true;

                    String status = response.getString("status");
                    if (status.equals("OK")) { // Everything's ok!
                        JSONArray array = response.getJSONArray("coords");

                        for (int i = 0; i < array.length(); i++){
                            JSONObject obj = (JSONObject) array.get(i);
                            double lat = Double.parseDouble(obj.getString("lat"));
                            double lng = Double.parseDouble(obj.getString("lng"));
                            LatLng latlng = new LatLng(lat, lng);

                            positions.add(latlng);
                        }

                    } else { // something went wrong
                        SystemUtils.displayToast(context, response.getString("reason"));
                        Log.e(TAG, response.getString("reason"));

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.toString());
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError response) {
                Log.d(TAG + " ERR", response.toString());
            }
        });
        queue.add(jsObjRequest);

        // Remove the spinner after DELAY seconds, and show a message if we have not been
        // able to communicate with the server.
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!connected) {
                    SystemUtils.showProgress(false, context, mProgressView, mCreateHitForm);
                    SystemUtils.displayToast(context, context.getString(R.string.error_cant_connect_to_server));

                    Intent i = new Intent(context, LoginActivity.class);
                    context.startActivity(i);
                    context.finish();
                }
            }
        }, DELAY);

    }
}
