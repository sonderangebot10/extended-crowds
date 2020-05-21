package fragment;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.johan_dp8ahsz.cs.R;

import java.util.HashMap;

import app.Config;


public class ConnectedBluetoothDevicesFragment extends Fragment {

    private final String TAG = "BLUETOOTH_DEVICES_FRAG";
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
        View view = inflater.inflate(R.layout.fragment_bluetooth_devices, container, false);

        queue = Volley.newRequestQueue(getActivity());
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        HashMap<String, String> params = new HashMap<>();
        params.put("email", prefs.getString("email", "shit"));

        return view;
    }

}
