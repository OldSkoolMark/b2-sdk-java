package rosenberg.mark.com.android_sample;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.backblaze.b2.client.B2StorageClient;
import com.backblaze.b2.client.contentHandlers.B2ContentFileWriter;
import com.backblaze.b2.client.okHttpClient.B2StorageOkHttpClientBuilder;

import java.io.File;

public class B2Service extends IntentService {

    private static final String ACTION_DOWNLOAD = "rosenberg.mark.com.android_sample.action.DOWNLOAD";
    private static final String ACTION_UPLOAD = "rosenberg.mark.com.android_sample.action.UPLOAD";

    private static final String EXTRA_FILE_ID = "rosenberg.mark.com.android_sample.extra.FILE_ID";
    private static final String EXTRA_FILE_NAME = "rosenberg.mark.com.android_sample.extra.FILE_NAME";
    private static final String EXTRA_LOCAL_FILE_PATH = "rosenberg.mark.com.android_sample.extra.FILE_PATH";

    public B2Service() {
        super("B2Service");
    }

    public static void startDownload(Context context, String fileID, String fileName) {
        Intent intent = new Intent(context, B2Service.class);
        intent.setAction(ACTION_DOWNLOAD);
        intent.putExtra(EXTRA_FILE_ID, fileID);
        intent.putExtra(EXTRA_FILE_NAME, fileName);
        context.startService(intent);
    }

    public static void startUpload(Context context, String fileID, String localFilePath) {
        Intent intent = new Intent(context, B2Service.class);
        intent.setAction(ACTION_UPLOAD);
        intent.putExtra(EXTRA_FILE_ID, fileID);
        intent.putExtra(EXTRA_LOCAL_FILE_PATH, localFilePath);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_DOWNLOAD.equals(action)) {
                final String fileID = intent.getStringExtra(EXTRA_FILE_ID);
                final String fileName = intent.getStringExtra(EXTRA_FILE_NAME);
                handleDownload(fileID, fileName);
            } else if (ACTION_UPLOAD.equals(action)) {
                final String fileID = intent.getStringExtra(EXTRA_FILE_ID);
                final String localFilePath = intent.getStringExtra(EXTRA_LOCAL_FILE_PATH);
                handleUpload(fileID, localFilePath);
            }
        }
    }
    private static final String USER_AGENT = "B2Sample";
    private static final String B2_ACCOUNT_ID = "5efbe16f705d";
    private static final String B2_APPLICATION_KEY = "002eb586f79285b73bcb82720e2335ed327d2dc198";

    private void handleDownload(String fileID, String fileName) {
        try (final B2StorageClient client = B2StorageOkHttpClientBuilder.builder(B2_ACCOUNT_ID, B2_APPLICATION_KEY, USER_AGENT).build()) {
            File file = createDestinationFile(fileName);
            B2ContentFileWriter sink = B2ContentFileWriter.builder(file).build();
            client.downloadById(fileID, sink);
        } catch (Exception e) {
            Log.e(TAG, "handleDownload() failed: " + e.getMessage());
        }
    }

    private File createDestinationFile(String fileName){
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
        return file;
    }

    private void handleUpload(String fileID, String localFilePath) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private final static String TAG = B2Service.class.getSimpleName();
}
