package activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import androidx.fragment.app.Fragment;
import android.os.Bundle;

import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.johan_dp8ahsz.cs.R;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import app.Config;
import fragment.AccountManagementFragment;
import fragment.AssignedTaskFragment;
import fragment.ConnectedBluetoothDevicesFragment;
import fragment.CreateTaskFragment;
import fragment.DeviceInfoFragment;
import fragment.OngoingTasksFragment;
import fragment.RewardFragment;
import fragment.TaskHistoryFragment;
import util.CustomRequest;
import util.NavigationDrawerAdapter;
import util.NavigationItems;
import util.NotificationUtils;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private DrawerLayout drawer;
    private static ActionBarDrawerToggle actionbarToggle;

    private static ArrayList<NavigationItems> arrayList;

    private ListView listview;

    private static FragmentManager fragment_manager;
    private static Toolbar toolbar;
    private RelativeLayout left_slider;

    private String lastUsedFragmentTag = "Assigned Tasks";
    private int lastUsedFragmentIndex = 0;

    private static Activity activity = null;
    private static SharedPreferences prefs;
    private String LOGOUT_URL;

    public ImageView mImage;

    private BroadcastReceiver mRegistrationBroadcastReceiver;

    public MainActivity(){}

    //Image request code
    private int PICK_IMAGE_REQUEST = 1;
    public Uri filePath;
    public Bitmap bitmap, bitmap1;

    //handling the image chooser activity result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                mImage.setImageURI(filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            bitmap = imageBitmap;
            mImage.setImageBitmap(imageBitmap);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        LOGOUT_URL = getString(R.string.LOGOUT_URL);

        activity = this;

        init();
        populateListItems();

        // Replace the default/home fragment if savedinstance is null
        if (savedInstanceState == null) {
            selectItem(0);
        }
    }

    void init() {
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        listview = (ListView) findViewById(R.id.list_slidermenu);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        left_slider = (RelativeLayout) findViewById(R.id.slider);

        TextView profileUsername = (TextView) findViewById(R.id.profile_name);
        TextView profileEmail = (TextView) findViewById(R.id.profile_email);
        profileUsername.setText(prefs.getString("username", getString(R.string.default_username)));
        profileEmail.setText(prefs.getString("email", getString(R.string.default_email)));

        // Fragment manager to manage fragment
        fragment_manager = getSupportFragmentManager();
        arrayList = new ArrayList<NavigationItems>();

        // Setting actionbar toggle
        actionbarToggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // close any open keyboards
                InputMethodManager imm=(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                View view = getCurrentFocus();
                if (view != null && imm != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                super.onDrawerOpened(drawerView);
            }
        };

        // Setting drawer listener
        drawer.addDrawerListener(actionbarToggle);


        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                // checking for type intent filter
                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                    // gcm successfully registered
                    // now subscribe to `global` topic to receive app wide notifications
                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);


                } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push notification is received

                    String message = intent.getStringExtra("message");

                    Toast.makeText(getApplicationContext(), "Push notification: " + message, Toast.LENGTH_LONG).show();
                }
            }
        };
    }

    // Populate navigation drawer listitems
    void populateListItems() {
                Integer Icons[] = { R.drawable.assigned_task_icon, R.drawable.create_task_icon,
                        R.drawable.ongoing_icon,R.drawable.task_history_icon, R.drawable.device_info_icon,
                        R.drawable.account_icon, R.drawable.reward_icon, R.drawable.bluetooth_icon, R.drawable.logoff_icon};
        String title[] = getResources().getStringArray(R.array.list_items);
        String subtitle[] = getResources().getStringArray(R.array.list_subitems);

        for (int i = 0; i < Icons.length; i++) {
            arrayList.add(new NavigationItems(title[i], subtitle[i], Icons[i]));
        }

        NavigationDrawerAdapter adapter = new NavigationDrawerAdapter(MainActivity.this, arrayList);

        // Setting adapter
        listview.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    // Select item method for replacing fragments
    public void selectItem(int position) {

        // Setting toolbar title
        toolbar.setTitle(arrayList.get(position).getTitle());

        // Close drawer method
        closeDrawer();

        switch (position) {
            case 0:
                replaceFragment(new AssignedTaskFragment(), "Assigned Tasks", position);
                break;
            case 1:
                replaceFragment(new CreateTaskFragment(), "Create Task", position);
                break;
            case 2:
                replaceFragment(new OngoingTasksFragment(), "Ongoing Tasks", position);
                break;
            case 3:
                replaceFragment(new TaskHistoryFragment(), "Task History", position);
                break;
            case 4:
                replaceFragment(new DeviceInfoFragment(), "Device Information", position);
                break;
            case 5:
                replaceFragment(new AccountManagementFragment(), "Account Management", position);
                break;
            case 6:
                replaceFragment(new RewardFragment(), "Reward Management", position);
                break;
            case 7:
                replaceFragment(new ConnectedBluetoothDevicesFragment(), "Bluetooth Management", position);
                break;
            case 8: // log out
                // Tell the server that you are logging off
                HashMap<String, String> params = new HashMap<>();
                params.put("email", prefs.getString("email", ""));
                // prepare the Request
                CustomRequest jsObjRequest = new CustomRequest(Request.Method.POST, LOGOUT_URL, params, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {}
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError response) {
                        Log.e(TAG + " Fail", response.toString());
                    }
                });

                // add it to the RequestQueue
                RequestQueue queue = Volley.newRequestQueue(this);
                queue.add(jsObjRequest);

                prefs.edit().putString("is_logged_in", "false").apply();

                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(i);
                finish();



                break;
        }
    }

    /**
     * Sets the toolbar title
     * @param str Str will be the new title
     */
    public static void setToolbarTitle(String str){
        if (toolbar != null)
            toolbar.setTitle(str);
    }

    public static void updateUsername(String username) {
        TextView profileUsername = (TextView) activity.findViewById(R.id.profile_name);
        profileUsername.setText(username);
    }

    // Replace fragment method
    void replaceFragment(android.app.Fragment fragment, String tag, int pos) {

        lastUsedFragmentTag = tag;
        lastUsedFragmentIndex = pos;

        // First find the fragment by TAG and if it null then replace the
        // fragment else do nothing
        final android.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.frame_container, fragment, tag).commit();

    }

    // close the open drawer
    void closeDrawer() {
        if (drawer.isDrawerOpen(left_slider)) {
            drawer.closeDrawer(left_slider);
        }
    }

    @Override
    public void onBackPressed() {
        Fragment fr = fragment_manager.findFragmentByTag(lastUsedFragmentTag);
        // First close the drawer if open
        if (drawer.isDrawerOpen(left_slider)) {
            drawer.closeDrawer(left_slider);
        }
        // else replace the home fragment if TAG is null
        else {
            if (fr == null) {
                selectItem(lastUsedFragmentIndex);
            }
            // finally finish activity
            else {
                finish();
            }
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync state for actionbar toggle
        actionbarToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        actionbarToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onResume(){
        super.onResume();


        // register GCM registration complete receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));

        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));

        // clear the notification area when the app is opened
        NotificationUtils.clearNotifications(getApplicationContext());
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }



    /**
     * If we receive a message here, that means we are forced to logout
     */
    public static class Receiver extends BroadcastReceiver {

        public Receiver() {
        }

        @Override
        public void onReceive(Context c, Intent intent) {
            Log.e(MainActivity.TAG, "FORCED LOGOUT");

            try {
                prefs.edit().putString("is_logged_in", "false").apply();
                Intent i = new Intent(activity, LoginActivity.class);
                MainActivity.activity.startActivity(i);
                MainActivity.activity.finish();
            } catch (Exception ignored) {}
        }
    }
}