package rosenberg.mark.com.android_sample;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.ActionBar;
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
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(getSupportFragmentManager().getBackStackEntryCount() > 1) {
                   onBackPressed();
               } else {
                   finish();
               }
            }
        });

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.listcontainer, BucketListFragment.newInstance());
        ft.addToBackStack("buckets");
        ft.commit();
    }

    private final String TAG = MainActivity.class.getSimpleName();
}
