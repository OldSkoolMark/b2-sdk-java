package rosenberg.mark.com.android_sample;

import android.util.Log;

import com.backblaze.b2.client.B2StorageClient;
import com.backblaze.b2.client.okHttpClient.B2StorageOkHttpClientBuilder;
import com.backblaze.b2.client.structures.B2FileVersion;
import com.backblaze.b2.util.B2ExecutorUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import static rosenberg.mark.com.android_sample.B2Service.B2_ACCOUNT_ID;
import static rosenberg.mark.com.android_sample.B2Service.B2_APPLICATION_KEY;
import static rosenberg.mark.com.android_sample.B2Service.USER_AGENT;

public class FileListViewModel extends ViewModel {
    MutableLiveData<List<B2FileVersion>> fileList;
    List<B2FileVersion> fileVersionList = new ArrayList<>();
    public LiveData<List<B2FileVersion>> getAllFiles(String bucketID){
        fileList = fileList != null ? fileList : new MutableLiveData<>();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(new Runnable() {
            @Override
            public void run () {
                fileVersionList.clear();
                try (final B2StorageClient client = B2StorageOkHttpClientBuilder.builder(B2_ACCOUNT_ID, B2_APPLICATION_KEY, USER_AGENT).build()) {
                    for( B2FileVersion fileVersion : client.fileNames(bucketID)){
                        fileVersionList.add(fileVersion);
                    }
                    fileList.postValue(fileVersionList);
                } catch (Exception e) {
                    Log.e(TAG, "B2CreateKey failed: " + e.getMessage());
                } finally {
                    B2ExecutorUtils.shutdownAndAwaitTermination(executor, 10, 10);
                }
            }
        });
        return fileList;
    }

    public LiveData<List<B2FileVersion>> addUploadInProgress(B2FileVersion b2FileVersion) {
        fileVersionList.add(b2FileVersion);
        return fileList;
    }
    private static final String TAG = FileListViewModel.class.getSimpleName();
}
