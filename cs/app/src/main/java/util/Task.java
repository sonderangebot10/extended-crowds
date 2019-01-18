package util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by johan on 2017-11-27.
 */

public class Task {

    private String id;
    private String question;
    private String type;
    private String options;

    private static SharedPreferences prefs;

    public Task(Context context, String id, String question, String type, String options) {
        this.id = id;
        this.question = question;
        this.type = type;
        this.options = options;

        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void saveTaskInPreferences(Task task){
        Set<String> set= new HashSet<String>();
        set.add(task.convertToJSON().toString());

        prefs.edit().putStringSet("some_name", set).apply();
    }

    private JSONObject convertToJSON(){
        JSONObject obj = new JSONObject();
        try {
            obj.put("id", this.id);
            obj.put("question", this.question);
            obj.put("type", this.type);
            obj.put("options", this.options);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }
}
