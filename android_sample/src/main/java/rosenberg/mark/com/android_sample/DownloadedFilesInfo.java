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
    public static DownloadedFilesInfo getInstance(Context context){
        instance = instance == null ? new DownloadedFilesInfo(context) : instance;
        return instance;
    }

    public DownloadedFilesInfo(Context context){
        readFromPrefs(context);
    }

    public synchronized void putPath( Context context, String b2FileID, String localPath){
        map.put(b2FileID, localPath);
        writeToPrefs(context);
    }
    public synchronized String getPath( String b2FileID){
        return map.get(b2FileID);
    }

    public synchronized void writeToPrefs(Context context){
        SharedPreferences sharedPref = context.getApplicationContext().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(MAP_PREF, toJson());
        editor.commit();

    }
    public synchronized void readFromPrefs(Context context){
        SharedPreferences sharedPref = context.getApplicationContext().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        String json = sharedPref.getString(MAP_PREF, null);
        fromJson(json);
    }

    public synchronized void removePath(Context context, String b2FileID) {
        map.remove(b2FileID);
        writeToPrefs(context);
    }

    private String toJson(){
        Gson gson = new Gson();
        String json = gson.toJson(this);
        return json;
    }

    private void fromJson(String json){
        Gson gson = new Gson();
        DownloadedFilesInfo tmp = gson.fromJson(json, DownloadedFilesInfo.class);
        map.clear();
        if( tmp != null ) {
            map.putAll(tmp.map);
        }
    }
}
