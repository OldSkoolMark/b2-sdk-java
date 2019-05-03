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
    MutableLiveData<List<FileItem>> fileList;
    List<FileItem> fileItemList = new ArrayList<>();
    public LiveData<List<FileItem>> getAllFiles(String bucketID){
        fileList = fileList != null ? fileList : new MutableLiveData<>();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(new Runnable() {
            @Override
            public void run () {
                fileItemList.clear();
                try (final B2StorageClient client = B2StorageOkHttpClientBuilder.builder(B2_ACCOUNT_ID, B2_APPLICATION_KEY, USER_AGENT).build()) {
                    for( B2FileVersion b2File : client.fileNames(bucketID)){
                        FileItem newItem = new FileItem.Builder(b2File.getFileName(), b2File.getFileId(), FileItem.State.DOWNLOADABLE)
                                .bucketID(bucketID)
                                .contentLength(b2File.getContentLength())
                                .build();
                        fileItemList.add(newItem);
                    }
                    fileList.postValue(fileItemList);
                } catch (Exception e) {
                    Log.e(TAG, "B2CreateKey failed: " + e.getMessage());
                } finally {
                    B2ExecutorUtils.shutdownAndAwaitTermination(executor, 10, 10);
                }
            }
        });
        return fileList;
    }

    public LiveData<List<FileItem>> addUploadInProgress(FileItem fileItem) {
        fileItemList.add(fileItem);
        fileList.postValue(fileItemList);
        return fileList;
    }
    private static final String TAG = FileListViewModel.class.getSimpleName();

    public LiveData<List<FileItem>> updateDownloadProgress(String fileID, String downloadPath, long contentLength, long percentComplete, boolean done) {
        Log.i(TAG,"updateDownloadProgress() "+percentComplete+" "+done);
        FileItem fileItem = getFileItemByID(fileID);
        if( fileItem != null ) {
            fileItem.setDownloadedFilePath(downloadPath);
            fileItem.setContentLength(contentLength);
            fileItem.setPercentComplete(percentComplete);
            fileItem.setDone(done);
            if( !done ){
                fileItem.setState(FileItem.State.DOWNLOADING);
            } else {
                fileItem.setState(FileItem.State.DOWNLOAD_SUCCESS);
            }
        }
        fileList.postValue(fileItemList);
        return fileList;
    }

    public LiveData<List<FileItem>> changeItemState(String b2FileID, FileItem.State newState) {
        FileItem fileItem = getFileItemByID(b2FileID);
        if(fileItem != null){
            fileItem.setState(newState);
        }
        fileList.postValue(fileItemList);
        return fileList;
    }

    private FileItem getFileItemByID(String fileID){
        for( FileItem fi : fileItemList){
            if (fi.id.equals(fileID)) {
                return fi;
            }
        }
        return null;
    }
}
