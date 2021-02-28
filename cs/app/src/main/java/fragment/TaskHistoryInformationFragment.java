package fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.johan_dp8ahsz.cs.R;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import activity.LoginActivity;
import activity.MainActivity;
import interfaces.AssignedHITInterface;
import util.CustomRequest;
import util.SensorValueInterpreter;
import util.SystemUtils;

public class TaskHistoryInformationFragment extends android.app.Fragment implements AssignedHITInterface {


    //TODO: Change this class to be modular like Create Task and Assigned Task.
    //Haven't got time right now!


    private final String TAG = "T_HIST";
    private FragmentManager fragmentManager;
    private String id;
    private SharedPreferences prefs;
    private RequestQueue queue;

    // UI references

    String question = "", type = "", id_ = "";
    String[] data;
    String[][] formatedData;

    boolean voted;

    String sensor, duration;

    public TaskHistoryInformationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_task_history_information, container, false);
        LinearLayout mainLayout = (LinearLayout) view.findViewById(R.id.history_info_main);
        queue = Volley.newRequestQueue(getActivity());
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        // Fetch arguments passed to this fragment
        Bundle args = this.getArguments();
        if(args != null){
            id_ = args.getString("id");
            type = args.getString("type");
            question = args.getString("question");
            data = args.getString("data").split(",");
            sensor = args.getString("sensor");
            duration = args.getString("duration");
        }

        // mutual views
        TextView mTaskLabel = new TextView(getActivity());
        TextView mTaskView = new TextView(getActivity());
        TextView mCreatedLabel =  new TextView(getActivity());
        TextView mCreatedView = new TextView(getActivity());
        TextView mCompletedLabel =  new TextView(getActivity());
        TextView mCompletedView = new TextView(getActivity());

        mTaskLabel.setText(getString(R.string.task_history_task));
        setTextColorAndSize(mTaskLabel, 20);
        mTaskView.setText(question);
        setTextColorAndSize(mTaskView, 25);
        mainLayout.addView(mTaskLabel);
        mainLayout.addView(mTaskView);


        // Sensing task
        if(TextUtils.equals(type, "Sensor task")){


            String value = "";
            // data2 = 5;7;3;3
            if(TextUtils.equals(data[0], "no answer")){
                value = data[0];
            } else{
                switch (sensor){
                    case "light":
                        value = SensorValueInterpreter.makeSenseOfLightReadings(data[0]);
                        break;
                    case "pressure":
                        value = SensorValueInterpreter.makeSenseOfPressureReadings(data[0], duration);
                        break;
                    case "ambient_temperature":
                        value = SensorValueInterpreter.makeSenseOfTemperatureReadings(data[0]);
                        break;
                    default:
                        break;
                }
            }

            TextView resultLabel = new TextView(getActivity());
            resultLabel.setText("Result:");
            setTextColorAndSize(resultLabel, 20);

            TextView result = new TextView(getActivity());
            result.setText(value);
            setTextColorAndSize(result, 25);

            mCreatedLabel.setText(getString(R.string.task_history_created));
            mCreatedView.setText(data[1]);
            setTextColorAndSize(mCreatedLabel, 20);
            setTextColorAndSize(mCreatedView, 25);

            mCompletedLabel.setText(getString(R.string.task_history_completed));
            mCompletedView.setText(data[2]);
            setTextColorAndSize(mCompletedLabel, 20);
            setTextColorAndSize(mCompletedView, 25);

            mainLayout.addView(result);
            mainLayout.addView(mCreatedLabel);
            mainLayout.addView(mCreatedView);
            mainLayout.addView(mCompletedLabel);
            mainLayout.addView(mCompletedView);

        }
        // Human intelligence task
        else{

            // yes;no;maybe,10-07-2017

            TextView mAnswerLabel = new TextView(getActivity());
            TextView mAnswerView  = new TextView(getActivity());
            ImageView mImage = new ImageView(getActivity());

            mAnswerLabel.setText("Answer");

            Boolean image = false;
            LinearLayout layout = new LinearLayout(getActivity());

            Button mMinusOne = new Button(getActivity());
            mMinusOne.setText("-1");
            mMinusOne.setTextSize(20);

            if(data[0].startsWith("img="))
            {
                image = true;

                String encodedImage = data[0].substring(4).replace("\\n", "");
                byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                mImage.setImageBitmap(bitmap);
                layout.setGravity(Gravity.CENTER);
                layout.addView(mImage);
            }

            mAnswerView.setText(data[0].replace(";", ", "));
            mCreatedLabel.setText(getString(R.string.task_history_created));
            mCreatedView.setText(data[1]);

            mCompletedLabel.setText(getString(R.string.task_history_completed));
            mCompletedView.setText(data[2]);
            if(TextUtils.equals(data[0].replace(";", ", "), "no answer")){
                mCompletedLabel.setText("Expired:");
            }

            setTextColorAndSize(mAnswerLabel, 20);
            setTextColorAndSize(mAnswerView, 25);
            setTextColorAndSize(mCreatedLabel, 20);
            setTextColorAndSize(mCreatedView, 25);
            setTextColorAndSize(mCompletedLabel, 20);
            setTextColorAndSize(mCompletedView, 25);

            mainLayout.addView(mAnswerLabel);
            if(image)
            {
                mainLayout.addView(layout);
                mainLayout.addView(mMinusOne);

                mMinusOne.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        HashMap<String, String> params = new HashMap<>();
                        params.put("id", id_);

                        mMinusOne.setEnabled(false);

                        // prepare the Request
                        CustomRequest jsObjRequest = new CustomRequest(Request.Method.POST, getString(R.string.REJECT_URL),
                                params, new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {

                                try {
                                    String status = response.getString("status");
                                    if (status.equals("OK")) {
                                        // Jump to somewhere more appropriate
                                        Fragment fragment = new OngoingTasksFragment();
                                        final FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
                                        ft.replace(R.id.frame_container, fragment, "Ongoing Tasks");
                                        ft.commit();
                                    } else { // WRONG!
                                        // Print the reason for why something went wrong
                                        SystemUtils.displayToast(getActivity(), "No other users in the area of this task");

                                        mMinusOne.setEnabled(true);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();

                                    mMinusOne.setEnabled(true);
                                }
                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError response) {
                                Log.d("Response: ", response.toString());

                                mMinusOne.setEnabled(true);
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

                            }
                        }, 10000);

                    }
                });
            }
            else
            {
                mainLayout.addView(mAnswerView);
            }

            mainLayout.addView(mCreatedLabel);
            mainLayout.addView(mCreatedView);
            mainLayout.addView(mCompletedLabel);
            mainLayout.addView(mCompletedView);

        }


        return view;
    }

    @Override
    public void createUI(LinearLayout mainLayout, View[] list, MainActivity context) {

    }

    @Override
    public void setUIRelatedData(Object[] fragmentManager, String[] data) {

        this.fragmentManager = (FragmentManager) fragmentManager[0];

        int j = 0;
        if(data.length > 3){
            id = data[0];
        }
    }

    private void setTextColorAndSize(TextView text, int size){
        text.setTextSize(size);
        text.setTextColor(Color.WHITE);
    }
}
