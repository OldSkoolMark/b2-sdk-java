package rosenberg.mark.com.android_sample;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.backblaze.b2.client.structures.B2Bucket;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class BucketListFragment extends Fragment implements Observer<List<B2Bucket>> {

    private BucketListViewModel mViewModel;
    private BucketArrayAdapter mArrayAdapter;

    public static BucketListFragment newInstance() {
        return new BucketListFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bucket_list_fragment, container, false);
        mArrayAdapter = new BucketArrayAdapter(R.layout.bucket_item, new ArrayList<>());
        RecyclerView recyclerView = v.findViewById(R.id.bucket_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mArrayAdapter);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(BucketListViewModel.class);
    }

    @Override
    public void onResume() {
        super.onResume();
        FragmentActivity a = getActivity();
        if( a != null ) {
            mViewModel.getAllBuckets().observe(a, this);
        }
    }

    @Override
    public void onChanged(List<B2Bucket> b2Buckets) {
        List<BucketItem> bucketItemList = DisplayModel.bucketItems(b2Buckets);
        mArrayAdapter.loadBucketItems(bucketItemList);
        mArrayAdapter.notifyDataSetChanged();
    }
    private final static String TAG = BucketListFragment.class.getSimpleName();
}
