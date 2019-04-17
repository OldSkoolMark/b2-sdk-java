package rosenberg.mark.com.android_sample;

import android.content.BroadcastReceiver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    //All methods in this adapter are required for a bare minimum recyclerview adapter
    private final int listItemLayout;
    private final List<FileItem> itemList;
    private final DownloadClickCallback callback;

    public synchronized String getB2FileIDdownloading() {
        return b2FileIDdownloading;
    }

    public synchronized void setB2FileIDdownloading(String b2FileIDdownloading) {
        this.b2FileIDdownloading = b2FileIDdownloading;
    }

    private String b2FileIDdownloading;
    private int percentComplete;
    // Constructor of the class
    public FileArrayAdapter(int layoutId, ArrayList<FileItem> itemList, DownloadClickCallback callback) {
        this.listItemLayout = layoutId;
        this.itemList = itemList;
        this.callback = callback;
    }

    public void loadFileItems(List<FileItem> fileItemList){
        itemList.clear();
        itemList.addAll(fileItemList);
    }

    public void updateDownloadProgress(String b2FileID, String fileName, long numBytes, long totalBytes){
        float numerator = (float)numBytes;
        float denominator = (float)totalBytes;
        percentComplete = Math.round(numerator/denominator);
        b2FileIDdownloading = b2FileID;
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
        String b2FileID = itemList.get(listPosition).id;
        TextView textView = holder.textView;
        textView.setText(itemList.get(listPosition).name);
        textView.setTag(b2FileID);
        if( b2FileID.equals(getB2FileIDdownloading())){
            holder.progressBar.setVisibility(View.VISIBLE);
            Log.i(TAG,"binding progress percentComplete: "+percentComplete);
            holder.progressBar.setProgress(percentComplete);
        } else {
            holder.progressBar.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileName = new StringBuilder(textView.getText()).toString();
                callback.onDownloadClick( (String)textView.getTag(), fileName);
            }
        });
    }

    // Static inner class to initialize the views of rows
    static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public ProgressBar progressBar;
        public ViewGroup layout;
        public ViewHolder(View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progress_bar);
            textView = itemView.findViewById(R.id.file_name);
        }
    }

    private final static String TAG = FileArrayAdapter.class.getSimpleName();
}
