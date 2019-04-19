package rosenberg.mark.com.android_sample;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class DownloadedFilesInfo {
    private static final String PREFERENCES_NAME = "DownloadedFilesInfo";
    private static final String MAP_PREF = "map";
    private Map<String, String> map = new HashMap<>();

    private static DownloadedFilesInfo instance = null;
    public static DownloadedFilesInfo getInstance(Activity activity){
        instance = instance == null ? new DownloadedFilesInfo(activity) : instance;
        return instance;
    }

    public DownloadedFilesInfo(Activity activity){
        readFromPrefs(activity);
    }

    public void putPath( Activity activity, String b2FileID, String localPath){
        map.put(b2FileID, localPath);
        writeToPrefs(activity);
    }
    public String getPath( String b2FileID){
        return map.get(b2FileID);
    }

    public void writeToPrefs(Activity activity){
        SharedPreferences sharedPref = activity.getApplicationContext().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(MAP_PREF, toJson());
        editor.commit();

    }
    public void readFromPrefs(Activity activity){
        SharedPreferences sharedPref = activity.getApplicationContext().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        String json = sharedPref.getString(MAP_PREF, null);
        fromJson(json);
    }
    public String toJson(){
        Gson gson = new Gson();
        String json = gson.toJson(this);
        return json;
    }

    public void fromJson(String json){
        Gson gson = new Gson();
        DownloadedFilesInfo tmp = gson.fromJson(json, DownloadedFilesInfo.class);
        map.clear();
        if( tmp != null ) {
            map.putAll(tmp.map);
        }
    }

    public void removePath(Activity activity, String b2FileID) {
        map.remove(b2FileID);
        writeToPrefs(activity);
    }
}
