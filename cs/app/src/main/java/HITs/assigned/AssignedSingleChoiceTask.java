package HITs.assigned;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import fragment.AssignedTaskFragment;
import interfaces.AssignedHITInterface;
import util.CustomRequest;
import util.SystemUtils;

public class AssignedSingleChoiceTask implements AssignedHITInterface {

    private final int DELAY = Config.DELAY;

    // UI references
    private View mProgressView;
    private View mAssignedFormView;

    private String id;
    private String question;
    private String[] options;
    private String answer = "";
    private int position;

    private FragmentManager fragmentManager;

    // Networking
    private RequestQueue queue;
    private String UPDATE_TASK_URL;
    private boolean connected = false;

    private SharedPreferences prefs;

    @Override
    public void createUI(LinearLayout mainLayout, View[] list, activity.MainActivity context){

        mProgressView = list[0];
        mAssignedFormView = list[1];

        UPDATE_TASK_URL = context.getString(R.string.UPDATE_TASK_URL);
        queue = Volley.newRequestQueue(context);

        prefs = PreferenceManager.getDefaultSharedPreferences(context);

        TextView mQuestionView = new TextView(context);
        TextView mOptionsLabel = new TextView(context);
        RadioGroup mOptionsGroup = new RadioGroup(context);
        Button mCreateButton = new Button(context);

        // Question layout
        mQuestionView.setText(context.getString(R.string.question) + " " + question);
        mQuestionView.setTextSize(20);
        mQuestionView.setTextColor(Color.WHITE);

        // Options label
        mOptionsLabel.setText(context.getString(R.string.assigned_task_choices));
        mOptionsLabel.setTextSize(20);
        mOptionsLabel.setTextColor(Color.WHITE);

        // Radio button group
        createRadioButtons(mainLayout, mOptionsGroup, context);

        // Submit button
        createSubmitButton(mCreateButton, context);

        // Add everything to the main layout
        mainLayout.addView(mQuestionView);
        mainLayout.addView(mOptionsLabel);
        mainLayout.addView(mOptionsGroup);
        mainLayout.addView(mCreateButton);
    }

    @Override
    public void setUIRelatedData(Object[] fragmentManager, String[] data) {

        this.fragmentManager = (FragmentManager) fragmentManager[0];

        int j = 0;
        if(data.length > 3){
            id = data[0];
            question = data[1];
            position = Integer.parseInt(data[2]);
            options = new String[data.length - 3];

            for(int i = 3; i < data.length; i++){
                options[j] = data[i];
                j++;
            }

        }
    }

    private void createRadioButtons(LinearLayout mainLayout, RadioGroup rg, Context context) {

        // Initialize a new RadioGroup
        rg.setOrientation(RadioGroup.VERTICAL);

        // Create Radio Buttons for RadioGroup
        for(String s : options){
            RadioButton rb = new RadioButton(context);
            rb.setText(s);
            rb.setTextSize(20);
            rb.setTextColor(Color.WHITE);
            rg.addView(rb);
        }

        // Set the OnChangeListener to do stuff
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                RadioButton rb = (RadioButton) group.findViewById(checkedId);
                answer = rb.getText().toString();
            }
        });
    }


    private void createSubmitButton(Button button, final activity.MainActivity context) {

        button.setText(context.getString(R.string.submit));

        // Set onClickListener to do stuff
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.equals(answer, "")) {
                    SystemUtils.displayToast(context, "Choose a radio button!");
                } else {
                    SystemUtils.displayToast(context, answer);

                    // When we've answered the question, remove this HIT from the string in
                    // shared preferences and send the answer to the server
                    SystemUtils.showProgress(true, context, mProgressView, mAssignedFormView);

                    HashMap<String, String> params = new HashMap<>();
                    params.put("data", answer);
                    params.put("type", "single");
                    params.put("id", id);
                    params.put("file", "single_choice.php");
                    params.put("email", prefs.getString("email", "something fucked up"));

                    // prepare the Request
                    CustomRequest jsObjRequest = new CustomRequest(Request.Method.POST, UPDATE_TASK_URL,
                            params, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            SystemUtils.showProgress(false, context, mProgressView, mAssignedFormView);
                            connected = true;

                            try {
                                String status = response.getString("status");
                                if (status.equals("OK")) { // Everything's ok!
                                    SystemUtils.displayToast(context, context.getString(R.string.assigned_task_success));

                                    String[] ids = prefs.getString("assigned_id", "").split(",");
                                    String[] questions = prefs.getString("assigned_question", "").split(",");
                                    String[] hitTypes = prefs.getString("assigned_type", "").split(",");
                                    String[] hitOptions = prefs.getString("assigned_options", "").split(",");
                                    String ii, q, t, o;

                                    // id
                                    StringBuilder sb = new StringBuilder();
                                    for (int i = 0; i < ids.length; i++) {
                                        if (i != position) {
                                            sb.append(",");
                                            sb.append(ids[i]);
                                        }
                                    }
                                    ii = sb.toString().replaceFirst(",", "");

                                    // questions
                                    sb = new StringBuilder();
                                    for (int i = 0; i < questions.length; i++) {
                                        if (i != position) {
                                            sb.append(",");
                                            sb.append(questions[i]);
                                        }
                                    }
                                    q = sb.toString().replaceFirst(",", "");

                                    // types
                                    sb = new StringBuilder();
                                    for (int i = 0; i < hitTypes.length; i++) {
                                        if (i != position) {
                                            sb.append(",");
                                            sb.append(hitTypes[i]);
                                        }
                                    }
                                    t = sb.toString().replaceFirst(",", "");

                                    //options
                                    sb = new StringBuilder();
                                    for (int i = 0; i < hitOptions.length; i++) {
                                        if (i != position) {
                                            sb.append(",");
                                            sb.append(hitOptions[i]);
                                        }
                                    }
                                    o = sb.toString().replaceFirst(",", "");

                                    // update prefs
                                    prefs.edit().putString("assigned_id", ii).apply();
                                    prefs.edit().putString("assigned_question", q).apply();
                                    prefs.edit().putString("assigned_type", t).apply();
                                    prefs.edit().putString("assigned_options", o).apply();

                                    // change fragment when all done
                                    final android.app.FragmentTransaction ft = fragmentManager.beginTransaction();
                                    ft.replace(R.id.frame_container, new AssignedTaskFragment()).commit();

                                } else { // WRONG!
                                    // Print the reason for why something went wrong
                                    String reason = response.getString("reason");
                                    SystemUtils.showProgress(false, context, mProgressView, mAssignedFormView);
                                    SystemUtils.displayToast(context, reason);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError response) {
                            Log.d("Response: ", response.toString());
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
                                SystemUtils.showProgress(false, context, mProgressView, mAssignedFormView);
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
    }
}
