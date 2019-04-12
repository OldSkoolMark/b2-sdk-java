package rosenberg.mark.com.android_sample;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar)findViewById(R.id.appbarlayout_tool_bar);
        toolbar.setTitle("B2 Quickstart");
        setSupportActionBar(toolbar);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.listcontainer, BucketListFragment.newInstance());
        ft.commit();
    }

    private final String TAG = MainActivity.class.getSimpleName();
}
