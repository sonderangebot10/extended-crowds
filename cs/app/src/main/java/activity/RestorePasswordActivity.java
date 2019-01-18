package activity;

import android.content.Context;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import app.Config;
import util.CustomRequest;
import util.SystemUtils;

public class RestorePasswordActivity extends AppCompatActivity {

    private final int DELAY = Config.DELAY;

    // UI references
    private EditText mEmailView;
    private EditText mPasswordView;
    private TextView mInstructionView;
    private Button mRestoreButton;
    private View mProgressView;
    private View mRestoreView;

    private Context context;

    // Networking
    RequestQueue queue;
    private String RESTORE_URL;
    private boolean connected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restore_password);

        RESTORE_URL = getString(R.string.RESTORE_URL);
        queue =  Volley.newRequestQueue(this);
        context = this;


        mEmailView = (EditText) findViewById(R.id.restore_email);
        mPasswordView = (EditText) findViewById(R.id.restore_password);
        mInstructionView = (TextView) findViewById(R.id.restore_instruction);
        mProgressView = findViewById(R.id.restore_progress);
        mRestoreView = findViewById(R.id.restore_form);

        mRestoreButton = (Button) findViewById(R.id.restore_button);
        mRestoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Reset errors.
                mEmailView.setError(null);
                mPasswordView.setError(null);

                String email = mEmailView.getText().toString();
                String password = mPasswordView.getText().toString();

                boolean cancel = false;
                View focusView = null;

                // Check for a valid password
                if (TextUtils.isEmpty(password)) {
                    mPasswordView.setError(getString(R.string.error_field_required));
                    focusView = mPasswordView;
                    cancel = true;
                } else if (!SystemUtils.isPasswordValid(password)) {
                    mPasswordView.setError(getString(R.string.error_invalid_password));
                    focusView = mPasswordView;
                    cancel = true;
                }

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

                if (cancel) {
                    // There was an error; don't initiate password reset and focus the first
                    // form field with an error.
                    focusView.requestFocus();
                } else {
                    // these two lines are here until this feature is implemented server-side.
                    mRestoreButton.setEnabled(false);
                    mInstructionView.setText(R.string.prompt_restore_instruction);


                   // SystemUtils.showProgress(true, context, mProgressView, mRestoreView);
                    HashMap<String, String> params = new HashMap<>();
                    params.put("email", email);
                    params.put("password", password);

                    // prepare the Request
                    CustomRequest deleteRequest = new CustomRequest(Request.Method.POST, RESTORE_URL, params,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {

                                    SystemUtils.showProgress(false, context, mProgressView, mRestoreView);
                                    connected = false;

                                    try {
                                        String status = response.getString("status");
                                        Log.e("Response", status);

                                        if( status.equals("OK") ){ // Everything's ok!
                                            // update SharedPrefs here?
                                            mRestoreButton.setEnabled(false);
                                            mInstructionView.setText(R.string.prompt_restore_instruction);
                                        }
                                        else{ // WRONG!
                                            SystemUtils.displayToast(context, getString(R.string.default_error));
                                        }
                                    }
                                    catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError response) {
                            Log.d("Response: ", response.toString());
                        }
                    });
/*
                    queue.add(deleteRequest);
                    // Remove the spinner after DELAY seconds, and show a message if we have not been
                    // able to communicate with the server.
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            if(!connected){
                                SystemUtils.showProgress(false, context, mProgressView, mRestoreView);
                                SystemUtils.displayToast(context, context.getString(R.string.error_cant_connect_to_server));
                            }
                        }
                    }, DELAY);
*/
                }

            }
        });

    }


}
