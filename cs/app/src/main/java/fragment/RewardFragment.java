package fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.johan_dp8ahsz.cs.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import activity.LoginActivity;
import app.Config;
import util.CustomRequest;
import util.SystemUtils;


public class RewardFragment extends Fragment {

    private final String TAG = "REWARD_FRAG";
    private final int DELAY = Config.DELAY;

    // UI References
    TextView rewardHITText;
    TextView rewardSensorText;
    View mProgressView;
    View mRewardFormView;

    // Networking
    private RequestQueue queue;
    private String REWARD_URL;
    private boolean connected = false;

    private SharedPreferences prefs;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reward, container, false);

        rewardHITText = (TextView) view.findViewById(R.id.reward_hit_text);
        rewardSensorText = (TextView) view.findViewById(R.id.reward_sensor_text);
        mProgressView = view.findViewById(R.id.reward_progress);
        mRewardFormView = view.findViewById(R.id.reward_form);

        queue = Volley.newRequestQueue(getActivity());
        REWARD_URL = getString(R.string.REWARD_URL);
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        //fetch points from server
        SystemUtils.showProgress(true, getActivity(), mProgressView, mRewardFormView);
        HashMap<String, String> params = new HashMap<>();
        params.put("email", prefs.getString("email", "shit"));

        CustomRequest jsObjRequest = new CustomRequest(Request.Method.POST, REWARD_URL,
                params, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    SystemUtils.showProgress(false, getActivity(), mProgressView, mRewardFormView);
                    connected = true;

                    String status = response.getString("status");
                    Log.e(TAG, response.toString());

                    if( status.equals("OK") ){ // Everything's ok!
                        String p = response.getString("points");
                        String[] points = p.split(";");
                        rewardSensorText.setText(points[0]);
                        rewardHITText.setText(points[1]);
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
                    SystemUtils.showProgress(false, getActivity(), mProgressView, mRewardFormView);
                    SystemUtils.displayToast(getActivity(), getString(R.string.error_cant_connect_to_server));

                    Intent i = new Intent(getActivity(), LoginActivity.class);
                    getActivity().startActivity(i);
                    getActivity().finish();
                }
            }
        }, DELAY);

        return view;
    }

}
