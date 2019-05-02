package rosenberg.mark.com.android_sample;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.backblaze.b2.client.structures.B2FileVersion;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.obsez.android.lib.filechooser.ChooserDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static rosenberg.mark.com.android_sample.B2Service.BROADCAST_FILE_DOWNLOAD_PROGRESS;
import static rosenberg.mark.com.android_sample.B2Service.BROADCAST_FILE_UPLOAD_PROGRESS;
import static rosenberg.mark.com.android_sample.B2Service.DownloadProgressExtraKeys.DOWNLOADED_FILE_PATH;
import static rosenberg.mark.com.android_sample.B2Service.DownloadProgressExtraKeys.FILEID;
import static rosenberg.mark.com.android_sample.B2Service.DownloadProgressExtraKeys.DONE;
import static rosenberg.mark.com.android_sample.B2Service.DownloadProgressExtraKeys.PERCENTCOMPLETE;
import static rosenberg.mark.com.android_sample.B2Service.DownloadProgressExtraKeys.CONTENTLENGTH;

public class FileListFragment extends Fragment
        implements Observer<List<B2FileVersion>> ,
        FileArrayAdapter.DownloadClickCallback,
        FileArrayAdapter.OpenDownloadedFileClickCallback{

    public final static String BUCKET_ID_KEY = "bucketid";
    private FileListViewModel mViewModel;
    private FileArrayAdapter mArrayAdapter;
    private String mBucketID;
    private View mProgressBar;
    private FloatingActionButton mFab;
    private RecyclerView mRecyclerView;
    private CoordinatorLayout mContainer;
    private ViewGroup mEmptyView;
    private BroadcastReceiver mDownloadProgressBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String fileID = intent.getStringExtra(FILEID.name());
            boolean done = intent.getBooleanExtra(DONE.name(), false);
            long percentComplete = intent.getLongExtra(PERCENTCOMPLETE.name(), -1);
            long contentLength = intent.getLongExtra(CONTENTLENGTH.name(), -1);
            String downloadPath = intent.getStringExtra(DOWNLOADED_FILE_PATH.name());
            mArrayAdapter.updateDownloadProgress(fileID, percentComplete, contentLength, done, downloadPath);
        }
    };

    private BroadcastReceiver mUploadProgressBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String fileID = intent.getStringExtra(B2Service.UploadProgressExtraKeys.FILEID.name());
            String fileName = intent.getStringExtra(B2Service.UploadProgressExtraKeys.FILENAME.name());
            String bucketID = intent.getStringExtra(B2Service.UploadProgressExtraKeys.BUCKETID.name());
            long percentComplete = intent.getLongExtra(B2Service.UploadProgressExtraKeys.PERCENTCOMPLETE.name(), -1);
            long contentLength = intent.getLongExtra(B2Service.UploadProgressExtraKeys.CONTENTLENGTH.name(), -1);
            if( percentComplete == 100 ){
                if( mBucketID != null) {
                    showProgress(true);
                    mViewModel.getAllFiles(mBucketID).observe(getActivity(), FileListFragment.this);
                }
            }else if( fileName != null && percentComplete > -1 && contentLength > -1){
                mArrayAdapter.updateUploadProgress(fileName, fileID, bucketID, percentComplete, contentLength);
            }
        }
    };

    public static FileListFragment newInstance(final String bucketID) {
        final FileListFragment me = new FileListFragment();
        final Bundle bundle = new Bundle(1);
        bundle.putString(BUCKET_ID_KEY, bucketID);
        me.setArguments(bundle);
        return me;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = getArguments();
        mBucketID = b != null ? b.getString(BUCKET_ID_KEY) : null;
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mDownloadProgressBroadcastReceiver,
                new IntentFilter(BROADCAST_FILE_DOWNLOAD_PROGRESS));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mUploadProgressBroadcastReceiver,
                new IntentFilter(BROADCAST_FILE_UPLOAD_PROGRESS));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mDownloadProgressBroadcastReceiver,
                new IntentFilter(BROADCAST_FILE_DOWNLOAD_PROGRESS));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mUploadProgressBroadcastReceiver,
                new IntentFilter(BROADCAST_FILE_UPLOAD_PROGRESS));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mContainer = (CoordinatorLayout)inflater.inflate(R.layout.file_list_fragment, container, false);
        mEmptyView = mContainer.findViewById(R.id.no_files_layout);
        mProgressBar = mContainer.findViewById(R.id.progressbar);
        mFab = mContainer.findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new ChooserDialog()
                        .with(getActivity())
                        .withStartFile(null)
                        .withChosenListener(new ChooserDialog.Result() {
                            @Override
                            public void onChoosePath(String path, File pathFile) {
                                mUploadPath = path;
                                if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                    startUpload(mUploadPath, mBucketID);
                                } else {
                                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                                            Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                                                .setCancelable(true)
                                                .setPositiveButton("Request Permission",
                                                        new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                                                        READ_EXTERNAL_STORAGE_REQUEST_CODE);
                                                            }
                                                        })
                                                .create();
                                        dialog.show();
                                    } else {
                                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                                READ_EXTERNAL_STORAGE_REQUEST_CODE);
                                    }
                                }
                            }
                        })
                        .build()
                        .show();
            }
        });
        mArrayAdapter = new FileArrayAdapter(R.layout.file_item, new ArrayList<>(), this, this);
        mRecyclerView = mContainer.findViewById(R.id.file_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mArrayAdapter);
        return mContainer;
    }

    private void startUpload(String path, String bucketID){
        String fileName = Utils.extractFileNameFromPath(path);
        B2FileVersion b2FileVersion = new B2FileVersion(null, fileName, -1, "", "", null, "uploading", System.currentTimeMillis());
        mViewModel.addUploadInProgress(b2FileVersion);
        B2Service.startUpload(getActivity(), bucketID, path);
        mArrayAdapter.setUploadFileName(fileName);
        mArrayAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(FileListViewModel.class);
    }

    @Override
    public void onResume() {
        super.onResume();
        if( mBucketID != null) {
            showProgress(true);
            mViewModel.getAllFiles(mBucketID).observe(getActivity(), this);
        }
    }

    @Override
    public void onChanged(List<B2FileVersion> b2FileVersions) {
        List<FileItem> fileItemList = DisplayModel.fileItems(b2FileVersions);
        Log.i(TAG, "number of files: "+(fileItemList != null ? fileItemList.size() : 0));
        if( fileItemList.size() > 0) {
            showProgress(false);
            mRecyclerView.setVisibility(View.VISIBLE);
            mArrayAdapter.loadFileItems(fileItemList);
            mArrayAdapter.notifyDataSetChanged();
        } else {
            showProgress(false);
            mRecyclerView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }
    private final static int READ_EXTERNAL_STORAGE_REQUEST_CODE = 0;
    private final static int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1;
    private String mUploadPath;
    private String mB2FileID;
    private String mFilename;

    @Override
    public void onDownloadClick( String b2FileID, String fileName) {
        mB2FileID = b2FileID;
        mFilename = fileName;
        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            B2Service.startDownload(getActivity(), mB2FileID, mFilename);
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog dialog = new AlertDialog.Builder(getActivity())
                        .setCancelable(true)
                        .setPositiveButton("Request Permission",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
                                    }
                                })
                        .create();
                dialog.show();
            } else {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onOpenFileClick(String b2FileID, String localFilePath) {
        File target = localFilePath != null ? new File(localFilePath) : null;
        if( target != null && target.exists()) {
            openFile(getActivity(), localFilePath);
        } else {
            Snackbar.make(getActivity().findViewById(android.R.id.content), "Downloaded file has been moved or deleted", Snackbar.LENGTH_LONG).show();
            DownloadedFilesInfo.getInstance(getActivity()).removePath(getActivity(), b2FileID);
            mArrayAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case WRITE_EXTERNAL_STORAGE_REQUEST_CODE:
                if (grantResults.length > 0 ){
                    if( grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        B2Service.startDownload(getActivity(), mB2FileID, mFilename);
                    } else if( grantResults[0] == PackageManager.PERMISSION_DENIED){
                        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            //Show permission explanation dialog...
                        }else{
                            Toast.makeText(getActivity(), "Cannot download without WRITE_EXTERNAL_STORAGE permission", Toast.LENGTH_LONG).show();
                        }
                    }
                }  else {
                    Toast.makeText(getActivity(), "Cannot download without WRITE_EXTERNAL_STORAGE permission", Toast.LENGTH_LONG).show();
                }
                return;
            case READ_EXTERNAL_STORAGE_REQUEST_CODE:
                if (grantResults.length > 0 ){
                    if( grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        startUpload(mUploadPath, mBucketID);
                    } else if( grantResults[0] == PackageManager.PERMISSION_DENIED){
                        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            //Show permission explanation dialog...
                        }else{
                            Toast.makeText(getActivity(), "Cannot upload without READ_EXTERNAL_STORAGE permission", Toast.LENGTH_LONG).show();
                        }
                    }
                }  else {
                    Toast.makeText(getActivity(), "Cannot upload without READ_EXTERNAL_STORAGE permission", Toast.LENGTH_LONG).show();
                }
                return;
            default:

        }
    }

    private void showProgress(boolean show){
        if( show ){
            mProgressBar.setVisibility(View.VISIBLE);
            mFab.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.GONE);
        } else {
            mProgressBar.setVisibility(View.GONE);
            mFab.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void openFile(Activity activity, String path) {
        File file = new File(path);
        MimeTypeMap map = MimeTypeMap.getSingleton();
        String ext = MimeTypeMap.getFileExtensionFromUrl(file.getName());
        String type = map.getMimeTypeFromExtension(ext);

        if (type == null) {
            type = "*/*";
        }

        Uri data = FileProvider.getUriForFile(activity, activity.getPackageName()+".fileprovider", file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(data, type);

        activity.startActivity(intent);
    }
    private final static String TAG = FileListFragment.class.getSimpleName();
}
