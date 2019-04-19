package rosenberg.mark.com.android_sample;

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

    //All methods in this adapter are required for a bare minimum recyclerview adapter
    private final int listItemLayout;
    private final List<FileItem> itemList;
    private final DownloadClickCallback downloadClickCallback;
    private final OpenDownloadedFileClickCallback openClickCallback;
    private String localDownloadPath;

    public synchronized String getB2FileIDdownloading() {
        return b2FileIDdownloading;
    }

    public synchronized void setB2FileIDdownloading(String b2FileIDdownloading) {
        this.b2FileIDdownloading = b2FileIDdownloading;
    }

    private String b2FileIDdownloading;
    private int percentComplete;
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
        percentComplete = 0;
        b2FileIDdownloading = "";
    }

    public void updateDownloadProgress(String b2FileID, long percentComplete, long contentLength, boolean done, String downloadPath){
        this.percentComplete = done ? 100 : (int)percentComplete;
        this.b2FileIDdownloading = b2FileID;
        this.localDownloadPath = downloadPath != null ? downloadPath : this.localDownloadPath;
        notifyDataSetChanged();
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
        final String b2FileID = itemList.get(listPosition).id;
        boolean alreadyDownloaded = false;
        TextView textView = holder.textView;
        textView.setText(itemList.get(listPosition).name);
        if( b2FileID.equals(getB2FileIDdownloading())){
            holder.indeterminateProgressbar.setVisibility(View.GONE);
            holder.determinateProgressbar.setVisibility(View.VISIBLE);
            holder.determinateProgressbar.setProgress(percentComplete);
            if( percentComplete == 100){
                alreadyDownloaded = true;
            }
        } else {
            holder.determinateProgressbar.setVisibility(View.GONE);
            holder.indeterminateProgressbar.setVisibility(View.GONE);
        }
        if( alreadyDownloaded ) {
            holder.downloadButton.setVisibility(View.GONE);
            holder.openButton.setVisibility(View.VISIBLE);
        } else {
            holder.downloadButton.setVisibility(View.VISIBLE);
            holder.openButton.setVisibility(View.GONE);

        }

        holder.downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileName = new StringBuilder(textView.getText()).toString();
                downloadClickCallback.onDownloadClick( b2FileID, fileName);
                holder.indeterminateProgressbar.setVisibility(View.VISIBLE);
                holder.downloadButton.setVisibility(View.GONE);
            }
        });
        holder.openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openClickCallback.onOpenFileClick(b2FileID, localDownloadPath);
            }
        });
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
