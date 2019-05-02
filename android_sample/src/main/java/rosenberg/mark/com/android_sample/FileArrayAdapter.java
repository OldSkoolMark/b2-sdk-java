package rosenberg.mark.com.android_sample;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public class FileArrayAdapter extends RecyclerView.Adapter<FileArrayAdapter.ViewHolder> {

    public interface DownloadClickCallback {
        void onDownloadClick( String b2FileID, String fileName);
    }
    public interface OpenDownloadedFileClickCallback{
        void onOpenFileClick( String b2FileID, String localFilePath);
    }

    private final int listItemLayout;
    private final List<FileItem> itemList;
    private final DownloadClickCallback downloadClickCallback;
    private final OpenDownloadedFileClickCallback openClickCallback;
    private String localDownloadPath;
    private String b2FileIDdownloading;
    private int downloadPercentComplete;
    private final List<String> pendingDownloads = Collections.synchronizedList(new ArrayList<>());

    public synchronized String getB2FileIDdownloading() {
        return b2FileIDdownloading;
    }

    public synchronized void setB2FileIDdownloading(String b2FileIDdownloading) {
        this.b2FileIDdownloading = b2FileIDdownloading;
    }
    private int uploadPercentComplete;
    private String uploadFileName;
    private String uploadFileID;
    private String uploadBucketID;

    // Constructor of the class
    public FileArrayAdapter(int layoutId, ArrayList<FileItem> itemList, DownloadClickCallback downloadClickCallback, OpenDownloadedFileClickCallback openClickCallback) {
        this.listItemLayout = layoutId;
        this.itemList = itemList;
        this.downloadClickCallback = downloadClickCallback;
        this.openClickCallback = openClickCallback;
    }

    public void loadFileItems(List<FileItem> fileItemList){
        itemList.clear();
        itemList.addAll(fileItemList);
        downloadPercentComplete = 0;
        setB2FileIDdownloading("");
    }

    public void updateDownloadProgress(String b2FileID, long percentComplete, long contentLength, boolean done, String downloadPath){
        pendingDownloads.remove(b2FileID);
        this.downloadPercentComplete = done ? 100 : (int)percentComplete;
        setB2FileIDdownloading(b2FileID);
        this.localDownloadPath = downloadPath != null ? downloadPath : this.localDownloadPath;
        notifyDataSetChanged();
    }

    public void updateUploadProgress(String fileName, String fileID, String bucketID, long percentComplete, long contentLength) {
        Log.i(TAG,"updateUploadProgress: "+percentComplete+" "+fileName);
        uploadPercentComplete = (int)percentComplete;
        uploadFileName = fileName;
        uploadBucketID = bucketID;
        uploadFileID = fileID;
        notifyDataSetChanged();
    }
    public void setUploadFileName(String filename){
        uploadFileName = filename;
    }

    // get the size of the list
    @Override
    public int getItemCount() {
        return itemList == null ? 0 : itemList.size();
    }

    // specify the row layout file and click for each row
    @Override
    public @NonNull
    ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(listItemLayout, parent, false);
        ViewHolder myViewHolder = new ViewHolder(view);
        return myViewHolder;
    }

        // load data in each row element
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int listPosition) {
        TextView textView = holder.textView;
        Activity activity = (Activity) textView.getContext();
        String filename = itemList.get(listPosition).name;
        textView.setText(filename);

        final String b2FileID = itemList.get(listPosition).id;
        if( b2FileID != null ) {
            // file is already on device or is being downloaded
            if (b2FileID.equals(getB2FileIDdownloading())) {
                // File download has started. We have content-length so we show % complete
                if (downloadPercentComplete == 100) {
                    DownloadedFilesInfo.getInstance(activity).putPath(activity, b2FileID, localDownloadPath);
                    // File has already been downloaded
                    holder.indeterminateProgressbar.setVisibility(View.GONE);
                    holder.determinateProgressbar.setVisibility(View.GONE);
                    holder.downloadButton.setVisibility(View.GONE);
                    holder.openButton.setVisibility(View.VISIBLE);
                } else {
                    holder.indeterminateProgressbar.setVisibility(View.VISIBLE);
                    holder.determinateProgressbar.setVisibility(View.VISIBLE);
                    holder.determinateProgressbar.setProgress(downloadPercentComplete);
                    holder.openButton.setVisibility(View.GONE);
                    holder.downloadButton.setVisibility(View.GONE);
                }
            } else if (!TextUtils.isEmpty(DownloadedFilesInfo.getInstance(activity).getPath(b2FileID))) {
                // File has already been downloaded
                holder.indeterminateProgressbar.setVisibility(View.GONE);
                holder.determinateProgressbar.setVisibility(View.GONE);
                holder.downloadButton.setVisibility(View.GONE);
                holder.openButton.setVisibility(View.VISIBLE);
            } else if (isQueuedForDownload(b2FileID)) {
                // Download is in queue but not started yet
                holder.indeterminateProgressbar.setVisibility(View.VISIBLE);
                holder.determinateProgressbar.setVisibility(View.GONE);
                holder.downloadButton.setVisibility(View.GONE);
                holder.downloadButton.setVisibility(View.GONE);
            } else {
                holder.downloadButton.setVisibility(View.VISIBLE);
                holder.openButton.setVisibility(View.GONE);
                holder.indeterminateProgressbar.setVisibility(View.GONE);
                holder.determinateProgressbar.setVisibility(View.GONE);
            }

            holder.downloadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String fileName = new StringBuilder(textView.getText()).toString();
                    if (!pendingDownloads.contains(b2FileID)) {
                        pendingDownloads.add(b2FileID);
                    }
                    downloadClickCallback.onDownloadClick(b2FileID, fileName);
                    holder.indeterminateProgressbar.setVisibility(View.VISIBLE);
                    holder.downloadButton.setVisibility(View.GONE);
                }
            });
            holder.openButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    localDownloadPath = DownloadedFilesInfo.getInstance(activity).getPath(b2FileID);
                    openClickCallback.onOpenFileClick(b2FileID, localDownloadPath);
                }
            });
        } else {

            // file is being uploaded
            if( uploadFileName.endsWith(filename)){
                if( uploadPercentComplete > -1){
                    holder.openButton.setVisibility(View.GONE);
                    if( uploadPercentComplete < 100 ){
                        holder.downloadButton.setVisibility(View.GONE);
                        holder.indeterminateProgressbar.setVisibility(View.VISIBLE);
                        holder.determinateProgressbar.setProgress(uploadPercentComplete);
                        holder.determinateProgressbar.setVisibility(View.VISIBLE);
                    } else {
                        holder.downloadButton.setVisibility(View.VISIBLE);
                        holder.determinateProgressbar.setVisibility(View.GONE);
                        holder.indeterminateProgressbar.setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    private boolean isQueuedForDownload(String b2FileID) {
        return pendingDownloads.contains(b2FileID);
    }

    // Static inner class to initialize the views of rows
    static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public ProgressBar determinateProgressbar;
        public ProgressBar indeterminateProgressbar;
        public ImageView downloadButton;
        public ImageView openButton;
        public ViewGroup layout;
        public ViewHolder(View itemView) {
            super(itemView);
            determinateProgressbar = itemView.findViewById(R.id.progress_bar);
            indeterminateProgressbar = itemView.findViewById(R.id.starting_progress_bar);
            textView = itemView.findViewById(R.id.file_name);
            downloadButton = itemView.findViewById(R.id.download_button);
            openButton = itemView.findViewById(R.id.open_button);
        }
    }

    private final static String TAG = FileArrayAdapter.class.getSimpleName();
}
