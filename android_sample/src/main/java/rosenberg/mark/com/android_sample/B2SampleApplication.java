package rosenberg.mark.com.android_sample;

import android.app.Application;

import com.jakewharton.threetenabp.AndroidThreeTen;

public class B2SampleApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AndroidThreeTen.init(this);
    }
}
