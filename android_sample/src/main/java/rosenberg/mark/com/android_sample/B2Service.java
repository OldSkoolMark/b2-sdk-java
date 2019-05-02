package rosenberg.mark.com.android_sample;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.backblaze.b2.client.B2StorageClient;
import com.backblaze.b2.client.contentHandlers.B2ContentFileWriter;
import com.backblaze.b2.client.contentSources.B2ContentSource;
import com.backblaze.b2.client.contentSources.B2ContentTypes;
import com.backblaze.b2.client.contentSources.B2FileContentSource;
import com.backblaze.b2.client.exceptions.B2Exception;
import com.backblaze.b2.client.okHttpClient.B2OkHttpClientImpl;
import com.backblaze.b2.client.okHttpClient.B2StorageOkHttpClientBuilder;
import com.backblaze.b2.client.structures.B2FileVersion;
import com.backblaze.b2.client.structures.B2UploadFileRequest;
import com.backblaze.b2.client.structures.B2UploadListener;
import com.backblaze.b2.client.structures.B2UploadProgress;
import com.backblaze.b2.client.structures.B2UploadState;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import static rosenberg.mark.com.android_sample.B2Service.DownloadProgressExtraKeys.CONTENTLENGTH;
import static rosenberg.mark.com.android_sample.B2Service.DownloadProgressExtraKeys.DOWNLOADED_FILE_PATH;
import static rosenberg.mark.com.android_sample.B2Service.DownloadProgressExtraKeys.FILEID;
import static rosenberg.mark.com.android_sample.B2Service.DownloadProgressExtraKeys.PERCENTCOMPLETE;

public class B2Service extends IntentService {

    public static final String USER_AGENT = "B2AndroidSample";
    // production
    public static final String B2_ACCOUNT_ID = "5efbe16f705d";
    public static final String B2_APPLICATION_KEY = "002eb586f79285b73bcb82720e2335ed327d2dc198";
    // staging
//    public static final String B2_ACCOUNT_ID = "b20462956ffb";
//    public static final String B2_APPLICATION_KEY = "9006552ce81ca941079aaccec9a4800f90523892d2";

    private static final String ACTION_DOWNLOAD = "rosenberg.mark.com.android_sample.action.DOWNLOAD";
    private static final String ACTION_UPLOAD = "rosenberg.mark.com.android_sample.action.UPLOAD";

    private static final String EXTRA_FILE_ID = "rosenberg.mark.com.android_sample.extra.FILE_ID";
    private static final String EXTRA_BUCKET_ID = "rosenberg.mark.com.android_sample.extra.BUCKET_ID";
    private static final String EXTRA_FILE_NAME = "rosenberg.mark.com.android_sample.extra.FILE_NAME";
    private static final String EXTRA_LOCAL_FILE_PATH = "rosenberg.mark.com.android_sample.extra.FILE_PATH";

    private String targetLocalFilePath;

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

    public static void startUpload(Context context, String bucketID, String localFilePath) {
        Intent intent = new Intent(context, B2Service.class);
        intent.setAction(ACTION_UPLOAD);
        intent.putExtra(EXTRA_BUCKET_ID, bucketID);
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
                final String bucketID = intent.getStringExtra(EXTRA_BUCKET_ID);
                final String localFilePath = intent.getStringExtra(EXTRA_LOCAL_FILE_PATH);
                handleUpload(bucketID, localFilePath);
            }
        }
    }


    private void handleDownload(final String fileID, final String fileName) {
        lastPercentDownloadProgress = 0;
        targetLocalFilePath = null;
        try (final B2StorageClient client = B2StorageOkHttpClientBuilder.builder(B2_ACCOUNT_ID, B2_APPLICATION_KEY, USER_AGENT)
                .progressListener(progressListener)
                .build()) {
            File file = createDownloadDestinationFile(fileName);
            Log.i(TAG,"downloading to:" +file.getAbsolutePath());
            targetLocalFilePath = file.getAbsolutePath();
            B2ContentFileWriter sink = B2ContentFileWriter.builder(file).build();
            client.downloadById(fileID, sink);
        } catch (Exception e) {
            Log.e(TAG, "handleDownload() failed: " + e.getMessage());
        }
    }

    /*
     * Download progress via OKHttp progress listener and local brodcast
     */
    public final static String BROADCAST_FILE_DOWNLOAD_PROGRESS = "downloadprogress";
    public enum DownloadProgressExtraKeys { FILEID, DONE, PERCENTCOMPLETE, CONTENTLENGTH, DOWNLOADED_FILE_PATH};
    private void broadcastDownloadProgress(String fileID, long percentComplete, long contentLength, boolean done, String downloadedFilePath) {
        Intent intent = new Intent(BROADCAST_FILE_DOWNLOAD_PROGRESS);
        intent.putExtra(FILEID.name(), fileID);
        intent.putExtra(DownloadProgressExtraKeys.DONE.name(), done);
        intent.putExtra(PERCENTCOMPLETE.name(), percentComplete);
        intent.putExtra(CONTENTLENGTH.name(), contentLength);
        intent.putExtra(DOWNLOADED_FILE_PATH.name(), downloadedFilePath);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private final B2OkHttpClientImpl.ProgressListener progressListener = new B2OkHttpClientImpl.ProgressListener() {

        @Override public void update(final String b2FileID, long bytesRead, long contentLength, boolean done) {
            if (done) {
                broadcastDownloadProgress(b2FileID, 100, contentLength, true, targetLocalFilePath);
            } else {
                if (contentLength != -1) {
                    maybeBroadcastDownloadProgress(b2FileID, bytesRead, contentLength, done, targetLocalFilePath);
                }
            }
        }
    };

    private long lastPercentDownloadProgress;

    private void maybeBroadcastDownloadProgress(String b2FileID, long bytesRead, long contentLength, boolean done, String targetLocalFilePath) {
        long currentProgress = (100*bytesRead)/contentLength;
        if( done ) {
            broadcastDownloadProgress(b2FileID, 100, contentLength, true, targetLocalFilePath);
        } else if(currentProgress - lastPercentDownloadProgress >= 5){
            broadcastDownloadProgress(b2FileID, currentProgress, contentLength, done, targetLocalFilePath);
            lastPercentDownloadProgress = currentProgress;
        }
    }

    private File createDownloadDestinationFile(String b2FileName){
        String fileName = b2FileName.contains("/") ? b2FileName.substring(b2FileName.lastIndexOf("/")) : b2FileName;
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),USER_AGENT);
        file.mkdirs();
        file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),USER_AGENT+"/"+ fileName);
        return file;
    }

    /*
     * Upload
     */
    public final static String BROADCAST_FILE_UPLOAD_PROGRESS = "uploadprogress";
    public enum UploadProgressExtraKeys { FILENAME, FILEID, PERCENTCOMPLETE, CONTENTLENGTH, BUCKETID };

    private void broadcastUploadProgress(String fileName, String bucketID, long percentComplete, long contentLength) {
        Log.i(TAG, "broadcastUploadProgress: "+percentComplete+"% "+fileName);
        Intent intent = new Intent(BROADCAST_FILE_UPLOAD_PROGRESS);
        intent.putExtra(UploadProgressExtraKeys.FILENAME.name(), fileName);
        intent.putExtra(UploadProgressExtraKeys.BUCKETID.name(), bucketID);
        intent.putExtra(UploadProgressExtraKeys.PERCENTCOMPLETE.name(), percentComplete);
        intent.putExtra(UploadProgressExtraKeys.CONTENTLENGTH.name(), contentLength);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void broadcastUploadProgress(String fileID, String fileName, String bucketID, long percentComplete, long contentLength) {
        Log.i(TAG, "broadcastUploadProgress: "+percentComplete+"% "+fileName);
        Intent intent = new Intent(BROADCAST_FILE_UPLOAD_PROGRESS);
        intent.putExtra(UploadProgressExtraKeys.FILEID.name(), fileID);
        intent.putExtra(UploadProgressExtraKeys.FILENAME.name(), fileName);
        intent.putExtra(UploadProgressExtraKeys.BUCKETID.name(), bucketID);
        intent.putExtra(UploadProgressExtraKeys.PERCENTCOMPLETE.name(), percentComplete);
        intent.putExtra(UploadProgressExtraKeys.CONTENTLENGTH.name(), contentLength);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void handleUpload(String bucketID, String localFilePath) {
        try (final B2StorageClient client = B2StorageOkHttpClientBuilder.builder(B2_ACCOUNT_ID, B2_APPLICATION_KEY, USER_AGENT).build()) {
            final File localFile = new File(localFilePath);
            final String sha1 = getFileSHA1(localFilePath);
            final B2ContentSource source  = B2FileContentSource.builder(localFile).build();
            // remove leading / to make B2 happy
            final String fileName = localFilePath.startsWith("/") ? localFilePath.substring(1) : localFilePath; // todo: don't store path?
            Log.i(TAG,"bucketID: "+bucketID+" filename: "+fileName);
            B2UploadFileRequest request = B2UploadFileRequest
                    .builder(bucketID, fileName, B2ContentTypes.B2_AUTO, source)
                    .setListener(new B2UploadListener() {
                        @Override
                        public void progress(B2UploadProgress progress) {
                            long percentComplete = progress.getState() == B2UploadState.SUCCEEDED ? 100 : (100*progress.getBytesSoFar())/progress.getLength();
                            percentComplete = percentComplete > 99 ? 99 : percentComplete;
                            broadcastUploadProgress(fileName, bucketID, percentComplete, progress.getLength() );
                        }
                    })
                    .build();
            B2FileVersion b2FileVersion = client.uploadSmallFile(request);
            broadcastUploadProgress(fileName, b2FileVersion.getFileId(), bucketID, 100, b2FileVersion.getContentLength());
        } catch (B2Exception e) {
            Log.e(TAG,e.getMessage());
        } /*catch (NoSuchAlgorithmException e) {
            Log.e(TAG,e.getMessage());
        }catch (IOException e) {
            Log.e(TAG,e.getMessage());
        }*/ catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create Sha1 hash for file.
     * @param filepath local file path
     * @return SHA1 hash
     * @throws IOException on read error
     */
    private static String getFileSHA1(String filepath) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        // file hashing with DigestInputStream
        try (DigestInputStream dis = new DigestInputStream(new FileInputStream(filepath), md)) {
            while (dis.read() != -1) ; //empty loop to clear the data
            md = dis.getMessageDigest();
        }

        // bytes to hex
        StringBuilder result = new StringBuilder();
        for (byte b : md.digest()) {
            result.append(String.format("%02x", b));
        }
        Log.i(TAG,"sha1: "+result.toString());
        return result.toString();

    }
    private final static String TAG = B2Service.class.getSimpleName();
}