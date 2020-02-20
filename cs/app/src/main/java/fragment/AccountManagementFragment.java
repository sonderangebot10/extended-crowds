package fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import activity.LoginActivity;
import activity.MainActivity;
import app.Config;
import util.CustomRequest;
import util.SystemUtils;

public class AccountManagementFragment extends android.app.Fragment {

    private final int DELAY = Config.DELAY;

    //UI references
    private TextView mAccountEmail;
    private EditText mNewUsername;
    private EditText mNewPassword;
    private EditText mRepeatPassword;
    private View mProgressView;
    private View mAccountFormView;

    private static View focusView = null;

    private SharedPreferences prefs;
    private Context context;

    // Networking
    private String UPDATE_URL;
    private String DELETE_URL;
    RequestQueue queue;
    private boolean connected = false;

    public AccountManagementFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account_management, container, false);
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
        queue =  Volley.newRequestQueue(getActivity());
        context = getActivity();
        UPDATE_URL = getString(R.string.UPDATE_URL);
        DELETE_URL = getString(R.string.DELETE_URL);

        mAccountEmail = (TextView) view.findViewById(R.id.account_mail);
        mNewUsername = (EditText) view.findViewById(R.id.account_new_username);
        mNewPassword = (EditText) view.findViewById(R.id.account_new_password);
        mRepeatPassword = (EditText) view.findViewById(R.id.account_repeat_password);
        mProgressView = view.findViewById(R.id.delete_progress);
        mAccountFormView = view.findViewById(R.id.account_form);
        Button mUpdateButton = (Button) view.findViewById(R.id.account_update);
        Button mDeleteButton = (Button) view.findViewById(R.id.account_delete);

        mAccountEmail.setText((prefs.getString("email", getString(R.string.default_email))));
        mNewUsername.setText(prefs.getString("username", getString(R.string.default_username)));

        mUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean cancel = false;

                final String newUsername = mNewUsername.getText().toString();
                String newPassword = mNewPassword.getText().toString();
                String repeatPassword = mRepeatPassword.getText().toString();

                // Check if the new uscername is valid
                if (!SystemUtils.isUsernameValid(newUsername)) {
                    mNewUsername.setError(getString(R.string.error_incorrect_username));
                    focusView = mNewUsername;
                    cancel = true;
                }
                // Check if the new password are correct
                else if(!TextUtils.isEmpty(newPassword) && SystemUtils.isPasswordValid(repeatPassword)){
                    if(!TextUtils.equals(newPassword, repeatPassword)){
                        mNewPassword.setError(getString(R.string.error_incorrect_password));
                        cancel = true;
                    }
                }
                else if(TextUtils.isEmpty(newPassword)){
                    newPassword = prefs.getString("password", "lol123");
                }

                if (cancel) {
                    // There was an error; don't attempt update and focus the first
                    // form field with an error.
                    focusView.requestFocus();
                } else {

                    SystemUtils.showProgress(true, context, mProgressView, mAccountFormView);
                    HashMap<String, String> params = new HashMap<>();
                    params.put("email", mAccountEmail.getText().toString());
                    params.put("username", newUsername);
                    params.put("password", newPassword);
                    final String password = newPassword;

                    // prepare the Request
                    CustomRequest updateRequest = new CustomRequest(Request.Method.POST, UPDATE_URL, params,
                            new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            SystemUtils.showProgress(false, context, mProgressView, mAccountFormView);
                            connected = true;
                            try {
                                String status = response.getString("status");
                                if (status.equals("OK")) { // Everything's ok!
                                    prefs.edit().putString("username", newUsername).apply();
                                    prefs.edit().putString("password", password).apply();
                                    MainActivity.updateUsername(newUsername);
                                    SystemUtils.displayToast(getActivity(), getString(R.string.update_successful));
                                } else { // WRONG!
                                    SystemUtils.displayToast(getActivity(), getString(R.string.update_failed));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError response) {
                            Log.e("Response: ", response.toString());
                        }
                    });
                    queue.add(updateRequest);

                    // Remove the spinner after DELAY seconds, and show a message if we have not been
                    // able to communicate with the server.
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(!connected){
                                SystemUtils.showProgress(false, context, mProgressView, mAccountFormView);
                                SystemUtils.displayToast(context, context.getString(R.string.error_cant_connect_to_server));

                                Intent i = new Intent(getActivity(), LoginActivity.class);
                                getActivity().startActivity(i);
                                getActivity().finish();
                            }
                        }
                    }, DELAY);
                }
            }
        });


        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(getString(R.string.account_delete));
                builder.setMessage(getString(R.string.account_delete_confirm))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.yes),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // If here, you want to delete account
                        SystemUtils.showProgress(true, context, mProgressView, mAccountFormView);

                        HashMap<String, String> params = new HashMap<>();
                        params.put("email", mAccountEmail.getText().toString());

                        // prepare the Request
                        CustomRequest deleteRequest = new CustomRequest(Request.Method.POST, DELETE_URL, params,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        SystemUtils.showProgress(false, context, mProgressView, mAccountFormView);
                                        connected = true;

                                        try {
                                            String status = response.getString("status");
                                            Log.e("Response", status);
                                            if( status.equals("OK") ){ // Everything's ok!
                                                prefs.edit().clear().apply();
                                                prefs.edit().putString("is_logged_in", "false").apply();
                                                SystemUtils.displayToast(context, getString(R.string.delete_successful));

                                                Intent i = new Intent(getActivity(), LoginActivity.class);
                                                startActivity(i);
                                                getActivity().finish();
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

                        queue.add(deleteRequest);

                        // Remove the spinner after DELAY seconds, and show a message if we have not been
                        // able to communicate with the server.
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(!connected){
                                    SystemUtils.showProgress(false, getActivity(), mProgressView, mAccountFormView);
                                    SystemUtils.displayToast(getActivity(), getString(R.string.error_cant_connect_to_server));

                                    Intent i = new Intent(getActivity(), LoginActivity.class);
                                    getActivity().startActivity(i);
                                    getActivity().finish();
                                }
                            }
                        }, DELAY);

                }
            }).setNegativeButton(getString(R.string.no),new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                    // if this button is clicked, just close the dialog box and do nothing
                    dialog.cancel();
                }
            });

            // create alert dialog
            AlertDialog alertDialog = builder.create();

            // show it
            alertDialog.show();

            }
        });
        return view;
    }
}
