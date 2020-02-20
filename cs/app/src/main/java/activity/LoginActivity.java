package activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import app.Config;
import util.GPSTracker;
import util.SystemUtils;
import util.CustomRequest;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.johan_dp8ahsz.cs.R;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_PERMISSIONS = 0;

    private final int DELAY = Config.DELAY;

    private final static String TAG = "LOGIN";

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private static View focusView = null;

    private Context context;
    SharedPreferences prefs;
    GPSTracker gps;

    // Networking
    RequestQueue queue;
    private String LOGIN_URL;
    private boolean connected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        context = this;
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        queue = Volley.newRequestQueue(this);
        LOGIN_URL = getString(R.string.LOGIN_URL);
        gps  = new GPSTracker(this);


        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

/*
        prefs.edit().putString("assigned_id", "").apply();
        prefs.edit().putString("assigned_question", "").apply();
        prefs.edit().putString("assigned_type", "").apply();
        prefs.edit().putString("assigned_options", "").apply();
*/



        //Check if already logged in
        if(TextUtils.equals(prefs.getString("is_logged_in", ""), "true") && SystemUtils.isNetworkAvailable(LoginActivity.this)){

            //SystemUtils.displayToast(this, "I am logged in already!");
            SystemUtils.showProgress(true, context, mProgressView, mLoginFormView);

            HashMap<String, String> params = new HashMap<>();
            params.put("email", prefs.getString("email", ""));
            params.put("password", prefs.getString("password", ""));
            params.put("firebase", FirebaseInstanceId.getInstance().getToken());
            params.put("lat", String.valueOf( gps.getLatitude()));
            params.put("lng", String.valueOf( gps.getLongitude()));
            params.put("os", "android");
            params.put("version", Config.VERSION);

            // Store the coordinates in shared preferenses if needed elsewhere.
            prefs.edit().putString("lat", String.valueOf( gps.getLatitude())).apply();
            prefs.edit().putString("lng", String.valueOf( gps.getLongitude())).apply();

            // prepare the Request
            CustomRequest jsObjRequest = new CustomRequest(Request.Method.POST, LOGIN_URL, params, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {

                    SystemUtils.showProgress(false, context, mProgressView, mLoginFormView);
                    connected = true;

                    try {
                        Log.e("Response", response.getString("status"));
                        String status = response.getString("status");
                        if( status.equals("OK") ){ // Everything's ok!
                            prefs.edit().putString("username", response.getString("username")).apply();
                            prefs.edit().putString("is_logged_in", "true").apply();

                            changeActivity(context, MainActivity.class);
                            finish();
                        }
                        else{ // WRONG!
                            if(TextUtils.equals(response.getString("reason"), "outdated")){
                                // There is a new version of the app
                                final SpannableString s = new SpannableString(getString(R.string.new_version_message));
                                Linkify.addLinks(s, Linkify.ALL);

                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setTitle(getString(R.string.new_version_title));
                                builder.setMessage(s)
                                        .setCancelable(false)
                                        .setPositiveButton(getString(R.string.ok), null);

                                // create alert dialog
                                AlertDialog alertDialog = builder.create();

                                // show it
                                alertDialog.show();

                                // Make the textview clickable. Must be called after show()
                                ((TextView)alertDialog.findViewById(android.R.id.message))
                                        .setMovementMethod(LinkMovementMethod.getInstance());
                            }else if(TextUtils.equals(response.getString("reason"), "maintenance")){
                                // There is maintenance currently going on

                                // min = 62
                                // h = 62 % 60 = 1
                                // min = min - (h*60) = 2

                                int minutes = Integer.parseInt(response.getString("time"));
                                int hours = minutes / 60;
                                minutes = minutes - (hours*60);
                                int days = hours / 24;
                                hours = hours - (days*24);

                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setTitle(getString(R.string.maintenance_title));
                                builder.setMessage(getString(R.string.maintenance_message) +
                                        + days + " days " + hours + " hours " + minutes + " minute(s)")
                                        .setCancelable(false)
                                        .setPositiveButton(getString(R.string.ok), null);

                                // create alert dialog
                                AlertDialog alertDialog = builder.create();

                                // show it
                                alertDialog.show();
                            }
                            else {
                                mPasswordView.setError(getString(R.string.error_incorrect_login));
                                LoginActivity.focusView = mPasswordView;
                            }
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
                        SystemUtils.showProgress(false, context, mProgressView, mLoginFormView);
                        SystemUtils.displayToast(context, getString(R.string.error_cant_connect_to_server));
                    }
                }
            }, DELAY);
        }

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        checkPermissions();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mSignInButton = (Button) findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                gps.getLocation();

                // Check for internet connectivity, and show a dialog box if not enabled.
                if (!SystemUtils.isNetworkAvailable(LoginActivity.this)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(getString(R.string.internet_title));
                    builder.setMessage(getString(R.string.internet_message)).setCancelable(false)
                            .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {}
                            });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
                if (!gps.canGetLocation()){
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(getString(R.string.gps_title));
                    builder.setMessage(getString(R.string.gps_message)).setCancelable(false)
                            .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {}
                            });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
                if(SystemUtils.isNetworkAvailable(LoginActivity.this) && gps.canGetLocation()){
                    attemptLogin();
                }
            }
        });

        Button mRegisterButton = (Button) findViewById(R.id.register_button);
        mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                changeActivity(context, RegisterActivity.class);
            }
        });

        Button mResetPasswordButton = (Button) findViewById(R.id.reset_password_button);
        mResetPasswordButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                changeActivity(context, RestorePasswordActivity.class);
            }
        });

    }

    private void changeActivity(Context c, java.lang.Class a) {
        Intent intent = new Intent(c, a);
        startActivity(intent);
    }

    private void checkPermissions() {
        if (!mayCheckPermissions()) {
            return;
        }
    }

    private boolean mayCheckPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        requestPermissions(new String[]{ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS);

        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkPermissions();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        final String email = mEmailView.getText().toString();
        final String password = mPasswordView.getText().toString();

        boolean cancel = false;

        // Check for a valid password, if the user entered one.
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
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            SystemUtils.showProgress(true, context, mProgressView, mLoginFormView);

            HashMap<String, String> params = new HashMap<>();
            params.put("email", email);
            params.put("password", password);
            params.put("firebase", FirebaseInstanceId.getInstance().getToken());
            params.put("lat", String.valueOf( gps.getLatitude()));
            params.put("lng", String.valueOf( gps.getLongitude()));
            params.put("os", "android");
            params.put("version", Config.VERSION);

            // Store the coordinates in shared preferenses if needed elsewhere.
            prefs.edit().putString("lat", String.valueOf( gps.getLatitude())).apply();
            prefs.edit().putString("lng", String.valueOf( gps.getLongitude())).apply();

            // prepare the Request
            CustomRequest jsObjRequest = new CustomRequest(Request.Method.POST, LOGIN_URL, params, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {

                    SystemUtils.showProgress(false, context, mProgressView, mLoginFormView);
                    connected = true;

                    try {
                        Log.e("Response", response.getString("status"));
                        String status = response.getString("status");
                        if( status.equals("OK") ){ // Everything's ok!
                            prefs.edit().putString("email",    email).apply();
                            prefs.edit().putString("password", password).apply();
                            prefs.edit().putString("username", response.getString("username")).apply();
                            prefs.edit().putString("is_logged_in", "true").apply();

                            changeActivity(context, MainActivity.class);
                            finish();
                        }
                        else{ // WRONG!
                            if(TextUtils.equals(response.getString("reason"), "outdated")){
                                // There is a new version of the app
                                final SpannableString s = new SpannableString(getString(R.string.new_version_message));
                                Linkify.addLinks(s, Linkify.ALL);

                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setTitle(getString(R.string.new_version_title));
                                builder.setMessage(s)
                                        .setCancelable(false)
                                        .setPositiveButton(getString(R.string.ok), null);

                                // create alert dialog
                                AlertDialog alertDialog = builder.create();

                                // show it
                                alertDialog.show();

                                // Make the textview clickable. Must be called after show()
                                ((TextView)alertDialog.findViewById(android.R.id.message))
                                        .setMovementMethod(LinkMovementMethod.getInstance());
                            }else if(TextUtils.equals(response.getString("reason"), "maintenance")){
                                // There is maintenance currently going on

                                // min = 62
                                // h = 62 / 60 = 1
                                // min = min - (h*60) = 2


                                int minutes = Integer.parseInt(response.getString("time"));
                                int hours = minutes / 60;
                                minutes = minutes - (hours*60);
                                int days = hours / 24;
                                hours = hours - (days*24);

                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setTitle(getString(R.string.maintenance_title));
                                builder.setMessage(getString(R.string.maintenance_message) +
                                        + days + " days " + hours + " hours " + minutes + " minute(s)")
                                        .setCancelable(false)
                                        .setPositiveButton(getString(R.string.ok), null);

                                // create alert dialog
                                AlertDialog alertDialog = builder.create();

                                // show it
                                alertDialog.show();
                            }
                            else {
                                mPasswordView.setError(getString(R.string.error_incorrect_login));
                                LoginActivity.focusView = mPasswordView;
                            }
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
                        SystemUtils.showProgress(false, context, mProgressView, mLoginFormView);
                        SystemUtils.displayToast(context, getString(R.string.error_cant_connect_to_server));
                    }
                }
            }, DELAY);

        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    @Override
    public void onStop() {
        super.onStop();
        gps.stopUsingGPS();
    }

    @Override
    public void onResume(){
        super.onResume();
        gps.getLocation();
    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        if(TextUtils.equals(prefs.getString("logged_in", ""), "true")){
            SystemUtils.displayToast(this, "Login: true");
            changeActivity(context, MainActivity.class);
            finish();
        }

    }
}

