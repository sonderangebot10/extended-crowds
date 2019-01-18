package fragment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import app.Config;
import util.CustomListViewAdapter;
import util.CustomRequest;
import util.Item;
import util.SystemUtils;

public class TaskHistoryFragment extends android.app.Fragment {

    private final String TAG = "TaskHistory";
    private final int DELAY = Config.DELAY;

    // UI references
    ListView historyList;
    View mProgressView;
    View mHistoryFormView;


    //Networking
    private RequestQueue queue;
    private String TASK_HISTORY_URL;
    private boolean connected = false;

    private SharedPreferences prefs;

    String[] questions;     // The question (hit) or sensor used (sensing)
    String[] types;         // which kind of task
    String[] data;
    String[] sensor;
    String[] duration;
    List<Item> items;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_history, container,false);

        queue = Volley.newRequestQueue(getActivity());
        TASK_HISTORY_URL = getString(R.string.TASK_HISTORY_URL);
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        mProgressView = view.findViewById(R.id.history_progress);
        mHistoryFormView = view.findViewById(R.id.history_form);
        historyList = (ListView) view.findViewById(R.id.history_list);

        //fetch data from server
        SystemUtils.showProgress(true, getActivity(), mProgressView, mHistoryFormView);
        HashMap<String, String> params = new HashMap<>();
        params.put("email", prefs.getString("email", "shit"));

        CustomRequest jsObjRequest = new CustomRequest(Request.Method.POST, TASK_HISTORY_URL,
                params, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    SystemUtils.showProgress(false, getActivity(), mProgressView, mHistoryFormView);
                    connected = true;

                    String status = response.getString("status");
                    Log.e(TAG, response.toString());

                    if( status.equals("OK") ){ // Everything's ok!
                        JSONArray tasks = response.getJSONArray("tasks");
                        transformJSONArray(tasks);


                        historyList.setAdapter(new CustomListViewAdapter(getActivity(), items));
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
                    SystemUtils.showProgress(false, getActivity(), mProgressView, mHistoryFormView);
                    SystemUtils.displayToast(getActivity(), getString(R.string.error_cant_connect_to_server));

                    Intent i = new Intent(getActivity(), LoginActivity.class);
                    getActivity().startActivity(i);
                    getActivity().finish();
                }
            }
        }, DELAY);

        // Populate the list view
        historyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                SystemUtils.displayToast(getActivity(), String.valueOf(position));

                // Create a new fragment to switch to
                Fragment fragment = new TaskHistoryInformationFragment();

                // Add arguments to the bundle passed to the fragment we are about to change to
                Bundle args = new Bundle();
                args.putString("type", types[position]);
                args.putString("question", questions[position]);
                args.putString("data", data[position]);
                args.putString("sensor", sensor[position]);
                args.putString("duration", duration[position]);

                fragment.setArguments(args);

                // Switch fragments
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.frame_container, fragment).commit();

            }
        });

        return view;
    }

    private void transformJSONArray(JSONArray array){

        questions = new String[array.length()];
        types = new String[array.length()];
        data = new String[array.length()];
        sensor = new String[array.length()];
        duration = new String[array.length()];
        items = new LinkedList<>();

        String s;

        for(int i = 0; i < array.length(); i++){
            try {
                ArrayList<String> a = new ArrayList<>();
                JSONObject object = (JSONObject) array.get(i);
                String type = object.getString("type");

                if(TextUtils.equals(type, "sensing")) {
                    questions[i] = getString(R.string.task_history_label) + " " + object.getString("sensor");
                    types[i] = "Sensor task";
                    sensor[i] = object.getString("sensor");
                    duration[i] = object.getString("duration");
                    items.add(new Item(getString(R.string.task_history_label) + " " +  object.getString("sensor") + " " +
                            getString(R.string.task_history_sensor), "Sensor task"));

                    // 5;5;7;;3;3
                    s =  object.getString("answer") + "," + object.getString("created") + "," + object.getString("completed");
                } else{
                    questions[i] = object.getString("question");
                    types[i] = type;
                    sensor[i] = "NoN";
                    duration[i] = "NoN";
                    items.add(new Item(object.getString("question"), type));
                    String answers = object.getString("answer").replace("[", "").replace("]", "").replace("\"", "").replace(",", ";");

                    // cow;orangutan;burd,10-12-2017
                    s = answers + "," + object.getString("created") + "," + object.getString("completed");
                }

                data[i] = s;

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
}
