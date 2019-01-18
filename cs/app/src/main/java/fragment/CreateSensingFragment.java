package fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import activity.LoginActivity;
import activity.MainActivity;
import app.Config;
import util.CustomListViewAdapter;
import util.CustomRequest;
import util.Item;
import util.SystemUtils;

public class CreateSensingFragment extends android.app.Fragment
        implements OnMapReadyCallback, GoogleMap.OnMapClickListener,
        GoogleMap.OnMyLocationButtonClickListener {

    private static final String TAG = "SENSING_FRAG";
    private static final int DELAY = Config.DELAY;

    // A default location (Stockholm, Sweden) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(59.3262896669483, 18.072013556957245);
    private static final int DEFAULT_ZOOM = 10;

    // UI references
   // EditText sensorDescription;
    EditText sensorReadings;
    ListView sensorList;
    MapView mapView;

    private View mProgressView;
    private View mSensingFormView;
    private View focusView = null;

    private CustomListViewAdapter mAdapter;
    static final String[] sensors = {"ambient temperature", "light", "pressure"};

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;

    // Map references
    GoogleMap map;
    Marker marker;
    Location mLastKnownLocation;
    ArrayList<LatLng> positions;


    // Networking
    private RequestQueue queue;
    private String SENSING_URL;
    private String POSITIONS_URL;
    private boolean connected = false;

    private String lastChecked = "ambient_temperature";
    private SharedPreferences prefs;

    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_sensing, container, false);

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Log.e("HAHAHHA", String.valueOf(savedInstanceState == null));

        // Change the toolbar title to something appropriate.
        MainActivity.setToolbarTitle(getString(R.string.sensing_task));

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        mapView = (MapView) view.findViewById(R.id.mapView);
     //   sensorDescription = (EditText) view.findViewById(R.id.sensing_description);
        sensorReadings = (EditText) view.findViewById(R.id.sensing_readings);
        sensorList = (ListView) view.findViewById(R.id.sensor_list);
        Button sensingButton = (Button) view.findViewById(R.id.create_sensing_task_button);

        mSensingFormView = view.findViewById(R.id.sensing_form);
        mProgressView = view.findViewById(R.id.sensing_progress);

        SENSING_URL = getString(R.string.CREATE_TASK_URL);
        POSITIONS_URL = getString(R.string.POSITIONS_URL);
        queue = Volley.newRequestQueue(getActivity());

        positions = new ArrayList<>();
   //     getPositions();

        // get available sensors and populate the list view
        //ArrayList<String> mySensors = SystemUtils.readSensorTypesList(getActivity());
        //Collections.sort(mySensors);
        //final String[] sensors = mySensors.toArray(new String[0]);

        final List<Item> items = new LinkedList<>();
        for (String sensor : sensors) {
            items.add(new Item(sensor));
        }

        mAdapter = new CustomListViewAdapter(getActivity(), R.layout.fragment_list_row_checkable, items);
        sensorList.setAdapter(mAdapter);
        sensorList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mAdapter.setChecked(position);
            }
        });

        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        // Gets to GoogleMap from the MapView and does initialization stuff
        mapView.getMapAsync(this);
        MapsInitializer.initialize(this.getActivity());

        // OnClickListener
        sensingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Reset errors
        //        sensorDescription.setError(null);
                sensorReadings.setError(null);

                boolean cancel = false;
                String lat = "", lng = "";

                String description = "empty"; //sensorDescription.getText().toString();

                int readings;
                try{
                    readings = Integer.parseInt(sensorReadings.getText().toString());
                } catch (Exception e){
                    cancel = true;
                    readings = 0;
                }

                // Check if all fields are correct
                /*
                if (TextUtils.isEmpty(description)) {
                    sensorDescription.setError(getString(R.string.error_field_required));
                    focusView = sensorDescription;
                    cancel = true;
                }
                */
                if (TextUtils.isEmpty(sensorReadings.getText().toString())) {
                    sensorReadings.setError(getString(R.string.error_field_required));
                    focusView = sensorReadings;
                    cancel = true;
                }else if (readings == 0) {
                    sensorReadings.setError(getString(R.string.sensing_error_greater_than_zero));
                    focusView = sensorReadings;
                    cancel = true;
                }else if(marker == null) {
                    cancel = true;
                    SystemUtils.displayToast(getActivity(), getString(R.string.sensing_error_no_location));
                    focusView = mapView;
                }

                if(cancel){ // something was wrong with the given data
                    if(focusView != null)
                        focusView.requestFocus();
                } else{
                    // Send task information to the server

                    SystemUtils.showProgress(true, getActivity(), mProgressView, mSensingFormView);
                    connected = true;

                    LatLng point = marker.getPosition();
                    lat = String.valueOf(point.latitude);
                    lng = String.valueOf(point.longitude);

                    HashMap<String, String> params = new HashMap<>();
                    params.put("email", prefs.getString("email", ""));
                    params.put("description", description);
                    params.put("duration", String.valueOf((readings - 1) * Config.TASK_INTERVAL));
                    params.put("readings", String.valueOf(readings));
                    params.put("lat", lat);
                    params.put("lng", lng);
                    params.put("type", "sensor");
                    params.put("file", "sensor.php");
                    params.put("sensor", sensors[mAdapter.getChecked()].replace(" ", "_"));

                    // prepare the Request
                    CustomRequest jsObjRequest = new CustomRequest(Request.Method.POST, SENSING_URL,
                            params, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            SystemUtils.showProgress(false, getActivity(), mProgressView, mSensingFormView);
                            connected = true;

                            try {
                                String status = response.getString("status");
                                if( status.equals("OK") ){ // Everything's ok!
                                    SystemUtils.displayToast(getActivity(), getString(R.string.task_success));

                                    android.app.Fragment fragment = new CreateTaskFragment();
                                    final android.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
                                    ft.replace(R.id.frame_container, fragment).commit();

                                }
                                else{ // WRONG!
                                    // Print the reason for why something went wrong
                                    String reason = response.getString("reason");
                                    SystemUtils.displayToast(getActivity(), reason);
                                    Log.e(TAG + " Response: ", response.toString());
                                    Log.e(TAG , "lat: " + prefs.getString("lat", "") +"lng: " + prefs.getString("lng", ""));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.e(TAG, e.toString());
                            }
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError response) {
                            Log.e(TAG + " Response: ", response.toString());
                        }
                    });

                    // add the request to the RequestQueue
                    queue.add(jsObjRequest);

                    // Remove the spinner after DELAY seconds, and show a message if we have not been
                    // able to communicate with the server.
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(!connected){
                                SystemUtils.showProgress(false, getActivity(), mProgressView, mSensingFormView);
                                SystemUtils.displayToast(getActivity(), getString(R.string.error_cant_connect_to_server));

                                Intent i = new Intent(getActivity(), LoginActivity.class);
                                getActivity().startActivity(i);
                                getActivity().finish();
                            }
                        }
                    }, DELAY);
                }
            }
        });

        return  view;
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
            locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
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

    @Override
    public boolean onMyLocationButtonClick() {
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onMapClick(LatLng point) {
        // We onlu want one marker on the map
        if(marker != null){
            marker.remove();
        }
        marker = map.addMarker(new MarkerOptions().position(new LatLng(point.latitude, point.longitude)));
    }


    private void getPositions(){


        SystemUtils.showProgress(true, getActivity(), mProgressView, mSensingFormView);
        HashMap<String, String> params = new HashMap<>();
        params.put("email", prefs.getString("email", "shit"));

        CustomRequest jsObjRequest = new CustomRequest(Request.Method.POST, POSITIONS_URL,
                params, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    SystemUtils.showProgress(false, getActivity(), mProgressView, mSensingFormView);
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
                        SystemUtils.displayToast(getActivity(), response.getString("reason"));
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
                    SystemUtils.showProgress(false, getActivity(), mProgressView, mSensingFormView);
                    SystemUtils.displayToast(getActivity(), getActivity().getString(R.string.error_cant_connect_to_server));

                    Intent i = new Intent(getActivity(), LoginActivity.class);
                    getActivity().startActivity(i);
                    getActivity().finish();
                }
            }
        }, DELAY);

    }
}