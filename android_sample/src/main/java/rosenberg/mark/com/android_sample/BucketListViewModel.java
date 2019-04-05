package rosenberg.mark.com.android_sample;

import android.util.Log;

import com.backblaze.b2.client.B2StorageClient;
import com.backblaze.b2.client.okHttpClient.B2StorageOkHttpClientBuilder;
import com.backblaze.b2.client.structures.B2Bucket;
import com.backblaze.b2.client.structures.B2Capabilities;
import com.backblaze.b2.util.B2ExecutorUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BucketListViewModel extends ViewModel {
    private static final String USER_AGENT = "B2Sample";
    private static final String B2_ACCOUNT_ID = "5efbe16f705d";
    private static final String B2_APPLICATION_KEY = "002eb586f79285b73bcb82720e2335ed327d2dc198";
    MutableLiveData<List<B2Bucket>> bucketList;
    public LiveData<List<B2Bucket>> getAllBuckets(){
        bucketList = bucketList != null ? bucketList : new MutableLiveData<>();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(new Runnable() {
            @Override
            public void run () {
                try (final B2StorageClient client = B2StorageOkHttpClientBuilder.builder(B2_ACCOUNT_ID, B2_APPLICATION_KEY, USER_AGENT).build()) {
                    bucketList.postValue(client.buckets());
                } catch (Exception e) {
                    Log.e(TAG, "B2CreateKey failed: " + e.getMessage());
                } finally {
                    B2ExecutorUtils.shutdownAndAwaitTermination(executor, 10, 10);
                }
            }
        });

        return bucketList;
    }
    private static final String TAG = BucketListViewModel.class.getSimpleName();
}
