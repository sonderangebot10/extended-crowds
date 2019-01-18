package fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.johan_dp8ahsz.cs.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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


public class OngoingTasksFragment extends Fragment {


    private static final String TAG = "ONGOING_TASKS";
    private static final int DELAY = Config.DELAY;


    ListView mOngoingList;
    private View mProgressView;
    private View mOngoingFormView;
    private View focusView = null;

    private CustomListViewAdapter mAdapter;

    // Networking
    private RequestQueue queue;
    private String ONGOING_URL;
    private boolean connected = false;

    private SharedPreferences prefs;
    List<Item> items;

    public OngoingTasksFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ongoing_tasks, container, false);

        // Change the toolbar title to something appropriate.
        MainActivity.setToolbarTitle(getString(R.string.ongoing_tasks));

        queue = Volley.newRequestQueue(getActivity());
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        ONGOING_URL = getString(R.string.ONGOING_URL);

        mProgressView = view.findViewById(R.id.ongoing_progress);
        mOngoingFormView = view.findViewById(R.id.ongoing_form);
        mOngoingList = (ListView) view.findViewById(R.id.ongoing_list);



        //fetch data from server
        SystemUtils.showProgress(true, getActivity(), mProgressView, mOngoingFormView);
        HashMap<String, String> params = new HashMap<>();
        params.put("email", prefs.getString("email", "shit"));

        CustomRequest jsObjRequest = new CustomRequest(Request.Method.POST, ONGOING_URL,
                params, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    SystemUtils.showProgress(false, getActivity(), mProgressView, mOngoingFormView);
                    connected = true;

                    String status = response.getString("status");
                    Log.e(TAG, response.toString());

                    if( status.equals("OK") ){ // Everything's ok!
                        JSONArray tasks = response.getJSONArray("tasks");
                        transformJSONArray(tasks);

                        mOngoingList.setAdapter(new CustomListViewAdapter(getActivity(), items));
                    }
                    else{ // something went wrong
                        Log.e(TAG + " ERR", response.getString("reason"));
                        SystemUtils.displayToast(getActivity(), response.getString("reason"));

                    }
                } catch (JSONException e) {
                    Log.e(TAG + " ERR", e.toString());
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
                    SystemUtils.showProgress(false, getActivity(), mProgressView, mOngoingFormView);
                    SystemUtils.displayToast(getActivity(), getString(R.string.error_cant_connect_to_server));

                    Intent i = new Intent(getActivity(), LoginActivity.class);
                    getActivity().startActivity(i);
                    getActivity().finish();
                }
            }
        }, DELAY);




        return view;
    }


    private void transformJSONArray(JSONArray array){

        String created;
        String question;
        String types;
        String sensor;
        items = new LinkedList<>();

        String s;

        for(int i = 0; i < array.length(); i++){
            try {
                ArrayList<String> a = new ArrayList<>();
                JSONObject object = (JSONObject) array.get(i);
                String type = object.getString("type");

                if(TextUtils.equals(type, "sensing")) {
                    question = getString(R.string.task_history_label) + " " + object.getString("sensor");
                    types = "Sensor task";
                    sensor = object.getString("sensor");
                    created = object.getString("created");
                } else{
                    question = object.getString("question");
                    types = type;
                    sensor = "NoN";
                    created = object.getString("created");
                }

                items.add(new Item(question, types, created, sensor));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

}
