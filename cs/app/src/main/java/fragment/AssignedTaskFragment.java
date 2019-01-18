package fragment;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.johan_dp8ahsz.cs.R;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import util.CustomListViewAdapter;
import util.Item;
import util.SystemUtils;
import util.Task;

public class AssignedTaskFragment extends android.app.Fragment {

    private static final String TAG = "ASSIGNED_TASK";

    private static List<Item> items;
    private static LinkedList<ArrayList<String>> options;
    private HashMap<String, String> types;
    private static CustomListViewAdapter mAdapter;

    private static String[] userId;
    ArrayList<String> HITTypes;
    ArrayList<String> className;
    ArrayList<String> HITPackage;

    static ArrayList<Task> tasks;

    static ListView assignedList;
    static Context context;

    private static SharedPreferences prefs;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_assigned_task, container,false);

        assignedList = (ListView) view.findViewById(R.id.assigned_list);

        context = getActivity();
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        options = new LinkedList<>();
        HITTypes = new ArrayList<>();
        className = new ArrayList<>();
        HITPackage = new ArrayList<>();

        tasks = new ArrayList<>();

        // Get all classes used for assigned tasks
        ArrayList<String> tmp = SystemUtils.getHITtypesFromAssets("assigned_hit", getActivity());
        for (String s : tmp){
            String[] tokens = s.split(":");

            className.add(tokens[0]);
            HITPackage.add(tokens[1]);
            HITTypes.add(tokens[2]);
        }

        // Fetch data from shared prefs, if there is any
        String id = prefs.getString("assigned_id", "");
        String questions = prefs.getString("assigned_question", "");
        String hitTypes = prefs.getString("assigned_type", "");
        String hitOptions = prefs.getString("assigned_options", "");

        userId = id.split(",");
        String[] q = questions.split(",");
        String[] t = hitTypes.split(",");
        String[] o = hitOptions.split(",");
        items = new ArrayList<>();
        options = new LinkedList<>();

        // populate the lists used for
        for(int i = 0; i < q.length; i++){
            ArrayList<String> array = new ArrayList<>();

            if(!TextUtils.isEmpty(q[i]) && !TextUtils.isEmpty(t[i]))
                items.add(new Item(q[i], t[i]));
            if(o.length > 0){
                String[] tokens = o[i].split(";");
                Collections.addAll(array, tokens);
            }

            options.add(array);
        }
        mAdapter = new CustomListViewAdapter(context, items);
        assignedList.setAdapter(mAdapter);
        assignedList.setOnItemClickListener(itemClickListener());

        final SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                 String id = prefs.getString("assigned_id", "");
                 String questions = prefs.getString("assigned_question", "");
                 String hitTypes = prefs.getString("assigned_type", "");
                 String hitOptions = prefs.getString("assigned_options", "");

                updateListView(id, questions, hitTypes, hitOptions);
                swipeLayout.setRefreshing(false);
            }
        });
        return view;
    }

    private ListView.OnItemClickListener itemClickListener(){
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // Create a new fragment to which we switch to
                Fragment fragment = new AssignedTaskTestFragment();

                // Add arguments to the bundle passed to the fragment we are about to change to
                Bundle args = new Bundle();
                args.putString("id", userId[position]);
                args.putString("position", String.valueOf(position));
                args.putString("question", items.get(position).getTitle());
                String[] l = options.get(position).toArray(new String[0]);
                args.putStringArray("options", l);

                args.putString("name", getClassName(items.get(position).getType()));
                args.putString("package", getPackage(items.get(position).getType()));

                fragment.setArguments(args);

                // Switch fragments
                final android.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.frame_container, fragment, "Human Intelligence Task").commit();
            }
        };
    }

    /**
     * Returns the class name to process the specified HIT type
     * @param type HIT type
     * @return name of the class associated with the specified type
     */
    private String getClassName(String type){

        for(int i = 0; i < HITTypes.size(); i++){
            if(TextUtils.equals(HITTypes.get(i), type)){
                return className.get(i);
            }
        }

        return "";
    }

    private String getPackage(String type){

        for(int i = 0; i < HITTypes.size(); i++){
            if(TextUtils.equals(HITTypes.get(i), type)){
                return HITPackage.get(i);
            }
        }

        return "";
    }

    /**
     * This function updates the list view
     * @param id A string with all task IDs, separated with a ','
     * @param q A string with all task questions, separated with a ','
     * @param t A string with all task types, separated with a ','
     * @param o A string with all choices to a task, individual options are separated with a,
     *          and otherwise separated with a';' Eg. "yes;no,5;10;20"
     */
    public static void updateListView(String id, String q, String t, String o){

        userId = id.split(",");
        String[] questions = q.split(",");
        String[] hitTypes = t.split(",");
        String[] hitOptions = o.split(",");

        items = new ArrayList<>();
        options = new LinkedList<>();

        // populate the list
        for(int i = 0; i < questions.length; i++){
            ArrayList<String> array = new ArrayList<>();

            if(!TextUtils.isEmpty(questions[i]) && !TextUtils.isEmpty(hitTypes[i]))
                items.add(new Item(questions[i], hitTypes[i]));
            if(hitOptions.length > 0){
                String[] tokens = hitOptions[i].split(";");
                Collections.addAll(array, tokens);
            }

            options.add(array);
        }

        // update data in our adapter
        mAdapter.getData().clear();
        mAdapter.getData().addAll(items);

        // fire the event
        mAdapter.notifyDataSetChanged();
    }

    public static class Receiver extends BroadcastReceiver {

        public Receiver(){}

        @Override
        public void onReceive(Context context, Intent intent) {


            prefs = PreferenceManager.getDefaultSharedPreferences(context);
            Bundle args = intent.getExtras();

            Log.e(TAG + " no sync", args.getString("id"));
            synchronized (this) {
                // get the current string from shared prefs
                String savedId = prefs.getString("assigned_id", "");
                String questions = prefs.getString("assigned_question", "");
                String hitTypes = prefs.getString("assigned_type", "");
                String hitOptions = prefs.getString("assigned_options", "");

                Log.e(TAG + " new", Arrays.toString(questions.split(",")) + "\n"
                        +  Arrays.toString(hitTypes.split(",")) + "\n"
                        +  Arrays.toString(hitOptions.split(",")));

                // A task has expired
                if (TextUtils.equals(args.getString("active", "active"), "expired")) {


                    // remove the the task in all strings (question, type, option)
                    String[] ids = prefs.getString("assigned_id", "").split(",");
                    String[] qs = questions.split(",");
                    String[] hts = hitTypes.split(",");
                    String[] hops = hitOptions.split(",");


                    // find the index of expired task by its id
                    int index = 0;
                    for(String s : ids){
                        if(ids[index].contains(args.getString("id"))){
                            break;
                        }
                        index++;
                    }

                    Log.e(TAG, "index: " + index);

                    String ii, q, t, o;
                    // id
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < ids.length; i++) {
                        if (i != index) {
                            sb.append(",");
                            sb.append(ids[i]);
                        }
                    }
                    ii = sb.toString().replaceFirst(",", "");

                    // questions
                    sb = new StringBuilder();
                    for (int i = 0; i < qs.length; i++) {
                        if (i != index) {
                            sb.append(",");
                            sb.append(qs[i]);
                        }
                    }
                    q = sb.toString().replaceFirst(",", "");

                    // types
                    sb = new StringBuilder();
                    for (int i = 0; i < hts.length; i++) {
                        if (i != index) {
                            sb.append(",");
                            sb.append(hts[i]);
                        }
                    }
                    t = sb.toString().replaceFirst(",", "");

                    //options
                    sb = new StringBuilder();
                    for (int i = 0; i < hops.length; i++) {
                        if (i != index) {
                            sb.append(",");
                            sb.append(hops[i]);
                        }
                    }
                    o = sb.toString().replaceFirst(",", "");

                    // update the prefs with the new stuff.
                    prefs.edit().putString("assigned_id", ii).apply();
                    prefs.edit().putString("assigned_question", q).apply();
                    prefs.edit().putString("assigned_type", t).apply();
                    prefs.edit().putString("assigned_options", o).apply();
                    updateListView(ii, q, t, o);

                }
                // A new task has been received
                else {
                    String q = args.getString("question");
                    String ht = args.getString("hit_type");
                    String ho = args.getString("options");
                    String id = args.getString("id");
/*
                    Task task = new Task(context, id, q, ht, ho);
                    tasks.add(task);
*/
                    // save an updated string
                    if (!TextUtils.isEmpty(questions)) {
                        prefs.edit().putString("assigned_id", savedId + "," + id).apply();
                        prefs.edit().putString("assigned_question", questions + "," + q).apply();
                        prefs.edit().putString("assigned_type", hitTypes + "," + ht).apply();
                        prefs.edit().putString("assigned_options", hitOptions + "," + ho).apply();
                    } else {
                        prefs.edit().putString("assigned_id", id).apply();
                        prefs.edit().putString("assigned_question", q).apply();
                        prefs.edit().putString("assigned_type", ht).apply();
                        prefs.edit().putString("assigned_options", ho).apply();
                    }
                }
            }
        }

    }

}
