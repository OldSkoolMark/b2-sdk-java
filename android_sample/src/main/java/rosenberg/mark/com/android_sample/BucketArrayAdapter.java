package rosenberg.mark.com.android_sample;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;


public class BucketArrayAdapter extends RecyclerView.Adapter<BucketArrayAdapter.ViewHolder> {

    //All methods in this adapter are required for a bare minimum recyclerview adapter
    private int listItemLayout;
    private List<BucketItem> itemList;
    // Constructor of the class
    public BucketArrayAdapter(int layoutId, ArrayList<BucketItem> itemList) {
        listItemLayout = layoutId;
        this.itemList = itemList;
    }

    public void loadBucketItems(List<BucketItem> bucketItemList){
        itemList = bucketItemList;
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
        return new ViewHolder(view);
    }

    // load data in each row element
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int listPosition) {
        TextView item = holder.item;
        item.setText(itemList.get(listPosition).name);
        item.setTag(itemList.get(listPosition).id);
    }

    // Static inner class to initialize the views of rows
    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView item;
        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            item = itemView.findViewById(R.id.bucket_name);
        }
        @Override
        public void onClick(View view) {
            FileListFragment fragment = FileListFragment.newInstance((String)item.getTag());
            FragmentTransaction ft = ((FragmentActivity)view.getContext()).getSupportFragmentManager().beginTransaction();
            ft.add(R.id.listcontainer, fragment);
            ft.addToBackStack("files");
            ft.commit();
        }
    }
}
