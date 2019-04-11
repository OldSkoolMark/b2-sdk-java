package rosenberg.mark.com.android_sample;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.backblaze.b2.client.structures.B2FileVersion;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.obsez.android.lib.filechooser.ChooserDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class FileListFragment extends Fragment
        implements Observer<List<B2FileVersion>> ,
        FileArrayAdapter.DownloadClickCallback{

    public final static String BUCKET_ID_KEY = "bucketid";
    private FileListViewModel mViewModel;
    private FileArrayAdapter mArrayAdapter;
    private String mBucketID;
    private View mProgressBar;
    private FloatingActionButton mFab;
    private RecyclerView mRecyclerView;

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
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.file_list_fragment, container, false);
        mProgressBar = v.findViewById(R.id.progressbar);
        mFab = v.findViewById(R.id.fab);
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
                                mUploadPathFile = pathFile;
                                if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                    startUpload(mUploadPath, mUploadPathFile, mBucketID);
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
        mArrayAdapter = new FileArrayAdapter(R.layout.file_item, new ArrayList<>(), this);
        mRecyclerView = v.findViewById(R.id.file_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mArrayAdapter);
        return v;
    }

    private void startUpload(String path, File pathFile, String bucketID){
        B2Service.startUpload(getActivity(), bucketID, path);
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
        showProgress(false);
        mArrayAdapter.loadFileItems(fileItemList);
        mArrayAdapter.notifyDataSetChanged();
    }
    private final static int READ_EXTERNAL_STORAGE_REQUEST_CODE = 0;
    private String mUploadPath;
    private File mUploadPathFile;
    private final static int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1;
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
                        startUpload(mUploadPath, mUploadPathFile, mBucketID);
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

    private final static String TAG = FileListFragment.class.getSimpleName();
}
