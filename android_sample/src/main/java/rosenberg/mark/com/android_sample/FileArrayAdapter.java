package rosenberg.mark.com.android_sample;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

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

    private void handleDownloadClick(TextView textView, String b2FileID, ViewHolder holder){
        PendingDownloadList pendingDownloads = PendingDownloadList.getInstance();
        String fileName = new StringBuilder(textView.getText()).toString();
        FileItem fileItem = pendingDownloads.getFileItem(b2FileID);
        if( fileItem == null ) {
            pendingDownloads.addFileItem(new FileItem(fileName, b2FileID, FileItem.State.IN_DOWNLOAD_QUEUE));
        } else {
            fileItem.setState(FileItem.State.IN_DOWNLOAD_QUEUE);
        }
        downloadClickCallback.onDownloadClick(b2FileID, fileName);
        holder.indeterminateProgressbar.setVisibility(View.VISIBLE);
        holder.downloadButton.setVisibility(View.GONE);
    }
    // load data in each row element
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int listPosition) {
        TextView textView = holder.textView;
        Context activity =  textView.getContext();
        String filename = itemList.get(listPosition).name;
        textView.setText(filename);
        FileItem fileItem = itemList.get(listPosition);
        final String b2FileID = itemList.get(listPosition).id;
        Log.i(TAG,"item: "+filename+" "+fileItem.getState().name());
        switch( fileItem.getState() ){
            case DOWNLOADABLE:
                holder.downloadButton.setVisibility(View.VISIBLE);
                holder.openButton.setVisibility(View.GONE);
                holder.indeterminateProgressbar.setVisibility(View.GONE);
                holder.determinateProgressbar.setVisibility(View.GONE);
                holder.downloadButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handleDownloadClick(textView, b2FileID, holder);
                    }
                });
                break;
            case DOWNLOAD_SUCCESS:
               DownloadedFilesInfo.getInstance(activity).putPath(activity, b2FileID, fileItem.getLocalFilePath());
                // File has already been downloaded
            case DOWNLOADED:
                holder.indeterminateProgressbar.setVisibility(View.GONE);
                holder.determinateProgressbar.setVisibility(View.GONE);
                holder.downloadButton.setVisibility(View.GONE);
                holder.openButton.setVisibility(View.VISIBLE);
                holder.openButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String localDownloadPath = DownloadedFilesInfo.getInstance(activity).getPath(b2FileID);
                        openClickCallback.onOpenFileClick(b2FileID, localDownloadPath);
                    }
                });
                break;
            case DOWNLOADING:
                holder.indeterminateProgressbar.setVisibility(View.VISIBLE);
                holder.determinateProgressbar.setVisibility(View.VISIBLE);
                holder.determinateProgressbar.setProgress((int)fileItem.getPercentComplete());
                holder.openButton.setVisibility(View.GONE);
                holder.downloadButton.setVisibility(View.GONE);
                break;
            case IN_DOWNLOAD_QUEUE:
                // Download is in queue but not started yet
                holder.indeterminateProgressbar.setVisibility(View.VISIBLE);
                holder.determinateProgressbar.setVisibility(View.GONE);
                holder.downloadButton.setVisibility(View.GONE);
                holder.openButton.setVisibility(View.GONE);
                break;
            case UPLOADING:
                holder.downloadButton.setVisibility(View.GONE);
                holder.indeterminateProgressbar.setVisibility(View.VISIBLE);
                Log.i(TAG,"progess: "+fileItem.getPercentComplete());
                holder.determinateProgressbar.setProgress((int)fileItem.getPercentComplete());
                holder.determinateProgressbar.setVisibility(View.VISIBLE);
                break;
            case UPLOAD_SUCCESS:
                holder.openButton.setVisibility(View.GONE);
                holder.downloadButton.setVisibility(View.VISIBLE);
                holder.downloadButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handleDownloadClick(textView, b2FileID, holder);
                    }
                });
                holder.determinateProgressbar.setVisibility(View.GONE);
                holder.indeterminateProgressbar.setVisibility(View.GONE);
                break;
            default:
                Log.e(TAG,"Unhandled item state");
        }
    }

    private boolean isQueuedForDownload(String b2FileID) {
        return PendingDownloadList.getInstance().containsFileItem(b2FileID);
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
