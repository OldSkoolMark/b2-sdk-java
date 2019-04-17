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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import static rosenberg.mark.com.android_sample.B2Service.ProgressExtraKeys.FILEID;
import static rosenberg.mark.com.android_sample.B2Service.ProgressExtraKeys.FILENAME;
import static rosenberg.mark.com.android_sample.B2Service.ProgressExtraKeys.NUMBYTES;
import static rosenberg.mark.com.android_sample.B2Service.ProgressExtraKeys.TOTALBYTES;

public class B2Service extends IntentService {

    public static final String USER_AGENT = "B2 Android Sample";
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
        try (final B2StorageClient client = B2StorageOkHttpClientBuilder.builder(B2_ACCOUNT_ID, B2_APPLICATION_KEY, USER_AGENT)
                .progressListener(progressListener)
                .build()) {
            File file = createDestinationFile(fileName);
            B2ContentFileWriter sink = B2ContentFileWriter.builder(file).build();
            client.downloadById(fileID, sink);
        } catch (Exception e) {
            Log.e(TAG, "handleDownload() failed: " + e.getMessage());
        }
    }
    public final static String BROADCAST_FILE_DOWNLOAD_PROGRESS = "downloadprogress";
    public enum ProgressExtraKeys{ FILEID, FILENAME, NUMBYTES, TOTALBYTES};
    private void broadcastProgress(String fileID, String fileName, long numBytes, long totalBytes) {
        Intent intent = new Intent(BROADCAST_FILE_DOWNLOAD_PROGRESS);
        intent.putExtra(FILEID.name(), fileID);
        intent.putExtra(FILENAME.name(), fileName);
        intent.putExtra(NUMBYTES.name(), numBytes);
        intent.putExtra(TOTALBYTES.name(), totalBytes);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private final B2OkHttpClientImpl.ProgressListener progressListener = new B2OkHttpClientImpl.ProgressListener() {
        boolean firstUpdate = true;

        @Override public void update(final String b2FileID, long bytesRead, long contentLength, boolean done) {
            if (done) {
                Log.i(TAG, "progress: done");
            } else {
                if (firstUpdate) {
                    firstUpdate = false;
                    if (contentLength == -1) {
                        Log.i(TAG,"progress: content-length: unknown");
                    } else {
                        Log.i(TAG, "progress: "+String.format("content-length: %d\n", contentLength));
                    }
                }

                Log.i(TAG,"progress: "+bytesRead);
                if (contentLength != -1) {
                    maybeBroadcastProgress(b2FileID, bytesRead, contentLength, done);
                    Log.i(TAG,"progress: "+String.format("%d%% done\n", (100 * bytesRead) / contentLength));
                }
            }
        }
    };
    private final Map<String, Long> progressMap = new ConcurrentHashMap<>();
    private void maybeBroadcastProgress(String b2FileID, long bytesRead, long contentLength, boolean done) {
        Long lastPercentProgress = progressMap.get(b2FileID) ;
        lastPercentProgress = lastPercentProgress == null ? 0 : lastPercentProgress;
        long currentProgress = (100*bytesRead)/contentLength;
        if( done ) {
            progressMap.remove(b2FileID);
        } else if( currentProgress - lastPercentProgress > 5){

        }
        progressMap.put(b2FileID, currentProgress);
    }
    private File createDestinationFile(String b2FileName){
        String fileName = b2FileName.contains("/") ? b2FileName.substring(b2FileName.lastIndexOf("/")) : b2FileName;
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
        return file;
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
                            final double percent = (100. * (progress.getBytesSoFar() / (double) progress.getLength()));
                            Log.i(TAG,String.format("  progress(%3.2f, %s)", percent, progress.toString()));
                        }
                    })
                    .build();
            B2FileVersion b2FileVersion = client.uploadSmallFile(request);
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
