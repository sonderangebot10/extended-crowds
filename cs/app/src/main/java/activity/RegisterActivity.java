package activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.johan_dp8ahsz.cs.R;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import app.Config;
import util.SystemUtils;
import util.CustomRequest;

public class RegisterActivity extends AppCompatActivity {

    private final int DELAY = Config.DELAY;

    // UI references
    private EditText mEmailView;
    private EditText mUsernameView;
    private EditText mPasswordView;
    private EditText mPasswordRepeatView;
    private View mProgressView;
    private View mRegisterFormView;

    private RadioButton mQ1O1;
    private RadioButton mQ1O2;
    private RadioButton mQ1O3;
    private RadioButton mQ2O1;
    private RadioButton mQ2O2;
    private RadioButton mQ2O3;
    private RadioButton mQ3O1;
    private RadioButton mQ3O2;
    private RadioButton mQ3O3;
    private RadioGroup mG1;
    private RadioGroup mG2;
    private RadioGroup mG3;

    private int a1;
    private int a2;
    private int a3;


    private SharedPreferences prefs;
    private Context context;
    private String sensors;

    // Networking
    private RequestQueue queue;
    private String REGISTER_URL;
    private boolean connected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        REGISTER_URL  = getString(R.string.REGISTER_URL);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        queue = Volley.newRequestQueue(this);
        context = this;

        // get all sensors
        ArrayList<String> allSensors = SystemUtils.readSensorTypesList(this);

        // retrieve and sort my available sensors
        ArrayList<String> mySensors = SystemUtils.getDeviceSensors(this);
        Collections.sort(mySensors);

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < allSensors.size(); i++){
            if(mySensors.contains(allSensors.get(i))){
                sb.append(1);
            } else{
                sb.append(0);
            }
        }
        sensors = sb.toString();

        mEmailView          = (EditText) findViewById(R.id.register_email);
        mPasswordRepeatView = (EditText) findViewById(R.id.repeat_password);
        mUsernameView       = (EditText) findViewById(R.id.register_username);
        mPasswordView       = (EditText) findViewById(R.id.register_password);
        final Button mRegisterButton = (Button) findViewById(R.id.register_account_button);

        mQ1O1 = (RadioButton) findViewById(R.id.question_1_option_1_button);
        mQ1O2 = (RadioButton) findViewById(R.id.question_1_option_2_button);
        mQ1O3 = (RadioButton) findViewById(R.id.question_1_option_3_button);
        mQ2O1 = (RadioButton) findViewById(R.id.question_2_option_1_button);
        mQ2O2 = (RadioButton) findViewById(R.id.question_2_option_2_button);
        mQ2O3 = (RadioButton) findViewById(R.id.question_2_option_3_button);
        mQ3O1 = (RadioButton) findViewById(R.id.question_3_option_1_button);
        mQ3O2 = (RadioButton) findViewById(R.id.question_3_option_2_button);
        mQ3O3 = (RadioButton) findViewById(R.id.question_3_option_3_button);
        mG1 = (RadioGroup) findViewById(R.id.question_1_group);
        mG2 = (RadioGroup) findViewById(R.id.question_2_group);
        mG3 = (RadioGroup) findViewById(R.id.question_3_group);

        mQ1O1.setTag(1); // THe score of the button
        mQ1O2.setTag(2);
        mQ1O3.setTag(3);
        mQ2O1.setTag(1);
        mQ2O2.setTag(2);
        mQ2O3.setTag(3);
        mQ3O1.setTag(1);
        mQ3O2.setTag(2);
        mQ3O3.setTag(3);

        mG1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {

                RadioButton rb = (RadioButton) radioGroup.findViewById(i);
                a1 = (int) rb.getTag();
                Log.e("REGISTRATION", String.valueOf(a1));
            }
        });
        mG2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {

                RadioButton rb = (RadioButton) radioGroup.findViewById(i);
                a2 = (int) rb.getTag();
                Log.e("REGISTRATION", String.valueOf(a2));
            }
        });
        mG3.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {

                RadioButton rb = (RadioButton) radioGroup.findViewById(i);
                a3 = (int) rb.getTag();
                Log.e("REGISTRATION", String.valueOf(a3));
            }
        });



        mRegisterFormView = findViewById(R.id.register_form);
        mProgressView = findViewById(R.id.register_progress);

        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Reset errors.
                mEmailView.setError(null);
                mUsernameView.setError(null);
                mPasswordView.setError(null);
                mPasswordRepeatView.setError(null);

                final String email = mEmailView.getText().toString();
                final String password = mPasswordView.getText().toString();
                final String passwordRepeat = mPasswordRepeatView.getText().toString();
                final String username = mUsernameView.getText().toString();
                final String deviceModel = android.os.Build.MODEL;
                final String osVersion = android.os.Build.VERSION.RELEASE;

                boolean cancel = false;
                View focusView = null;

                // Check for a valid email address.
                if (TextUtils.isEmpty(email)) {
                    mEmailView.setError(getString(R.string.error_field_required));
                    focusView = mEmailView;
                    cancel = true;
                } else if (!SystemUtils.isEmailValid(email)) {
                    mEmailView.setError(getString(R.string.error_invalid_email));
                    focusView = mEmailView;
                    cancel = true;
                }

                // Check for a valid username.
                if (TextUtils.isEmpty(username)) {
                    mUsernameView.setError(getString(R.string.error_field_required));
                    focusView = mUsernameView;
                    cancel = true;
                } else if (!SystemUtils.isUsernameValid(username)) {
                    mUsernameView.setError(getString(R.string.error_incorrect_username));
                    focusView = mUsernameView;
                    cancel = true;
                }

                // Check for a valid password
                if (TextUtils.isEmpty(password)) {
                    mPasswordView.setError(getString(R.string.error_field_required));
                    focusView = mPasswordView;
                    cancel = true;
                } else if (!SystemUtils.isPasswordValid(password)) {
                    mPasswordView.setError(getString(R.string.error_invalid_password));
                    focusView = mPasswordView;
                    cancel = true;
                } else if (!password.equals(passwordRepeat)) {
                    mPasswordView.setError(getString(R.string.error_password_mismatch));
                    focusView = mPasswordView;
                    cancel = true;
                }

                if(mG1.getCheckedRadioButtonId() == -1 || mG2.getCheckedRadioButtonId() == -1
                        || mG3.getCheckedRadioButtonId() == -1){
                    cancel = true;
                    focusView = mG1;
                    SystemUtils.displayToast(getApplicationContext(), "Please answer all the questions.");
                }

                if (cancel) {
                    // There was an error; don't initiate password reset and focus the first
                    // form field with an error.
                    focusView.requestFocus();
                } else {
                    // Show a progress spinner, and kick off a background task to
                    // perform the user register attempt.
                    SystemUtils.showProgress(true, context, mProgressView, mRegisterFormView);
                    int bid = a1+a2+a3;

                    HashMap<String, String> params = new HashMap<>();
                    params.put("email", email);
                    params.put("username", username);
                    params.put("password", password);
                    params.put("device_model", deviceModel);
                    params.put("device_os", osVersion );
                    params.put("device_sensors", sensors);
                    params.put("firebase", prefs.getString("token", FirebaseInstanceId.getInstance().getToken()));
                    params.put("bid", String.valueOf(bid));

                    // prepare the Request
                    CustomRequest jsObjRequest = new CustomRequest(Request.Method.POST, REGISTER_URL,
                            params, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            SystemUtils.showProgress(false, context, mProgressView, mRegisterFormView);
                            connected = true;

                            try {
                                Log.e("Response", response.getString("status"));
                                String status = response.getString("status");
                                if( status.equals("OK") ){ // Everything's ok!
                                    // Update the user information in shared preferences
                                    prefs.edit().putString("username", username).apply();
                                    prefs.edit().putString("password", password).apply();
                                    prefs.edit().putString("email", email).apply();
                                    prefs.edit().putString("device_os", osVersion).apply();
                                    prefs.edit().putString("device_model", deviceModel).apply();
                                    SystemUtils.displayToast(context, getString(R.string.register_success));

                                    // jump to MainActivity
                                    changeActivity(LoginActivity.class);
                                    finish();
                                }
                                else{ // WRONG!
                                    mEmailView.setError(getString(R.string.error_user_exists));
                                    mEmailView.requestFocus();
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

                    // add it to the RequestQueue
                    queue.add(jsObjRequest);

                    // Remove the spinner after DELAY seconds, and show a message if we have not been
                    // able to communicate with the server.
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(!connected){
                                SystemUtils.showProgress(false, context, mProgressView, mRegisterFormView);
                                SystemUtils.displayToast(context, getString(R.string.error_cant_connect_to_server));
                            }
                        }
                    }, DELAY);
                }
            }
        });
    }

    private void changeActivity(java.lang.Class a){
        Intent intent = new Intent(RegisterActivity.this, a);
        startActivity(intent);
    }
}
