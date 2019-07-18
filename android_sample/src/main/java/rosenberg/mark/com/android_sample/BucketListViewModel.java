package rosenberg.mark.com.android_sample;

import android.util.Log;

import com.backblaze.b2.client.B2StorageClient;
import com.backblaze.b2.client.okHttpClient.B2StorageOkHttpClientBuilder;
import com.backblaze.b2.client.structures.B2Bucket;
import com.backblaze.b2.util.B2ExecutorUtils;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BucketListViewModel extends ViewModel {
    MutableLiveData<List<B2Bucket>> bucketList;
    public LiveData<List<B2Bucket>> getAllBuckets(){
        bucketList = bucketList != null ? bucketList : new MutableLiveData<>();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(new Runnable() {
            @Override
            public void run () {
                try (final B2StorageClient client
                             = B2StorageOkHttpClientBuilder.builder(B2Service.B2_ACCOUNT_ID, B2Service.B2_APPLICATION_KEY, B2Service.USER_AGENT)
                        .build()) {
                    bucketList.postValue(client.buckets());
                } catch (Throwable e) {
                    Log.e(TAG, "B2CreateKey failed: " + e.getMessage(), e);
                } finally {
                    B2ExecutorUtils.shutdownAndAwaitTermination(executor, 10, 10);
                }
            }
        });

        return bucketList;
    }
    private static final String TAG = BucketListViewModel.class.getSimpleName();
}
