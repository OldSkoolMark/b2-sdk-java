package rosenberg.mark.com.android_sample;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private int listItemLayout;
    private List<FileItem> itemList;
    private DownloadClickCallback callback;
    // Constructor of the class
    public FileArrayAdapter(int layoutId, ArrayList<FileItem> itemList, DownloadClickCallback callback) {
        this.listItemLayout = layoutId;
        this.itemList = itemList;
        this.callback = callback;
    }

    public void loadFileItems(List<FileItem> fileItemList){
        itemList = fileItemList;
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
        TextView item = holder.item;
        item.setText(itemList.get(listPosition).name);
        item.setTag(itemList.get(listPosition).id);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileName = new StringBuilder(item.getText()).toString();
                callback.onDownloadClick( (String)item.getTag(), fileName);
            }
        });
    }

    // Static inner class to initialize the views of rows
    static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView item;
        public ViewGroup layout;
        public ViewHolder(View itemView) {
            super(itemView);
            item = itemView.findViewById(R.id.file_name);
        }
    }
}
