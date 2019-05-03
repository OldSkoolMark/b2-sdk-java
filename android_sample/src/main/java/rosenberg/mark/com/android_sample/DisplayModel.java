package rosenberg.mark.com.android_sample;

import com.backblaze.b2.client.structures.B2Bucket;
import com.backblaze.b2.client.structures.B2FileVersion;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

public class DisplayModel {
    public static @NonNull List<BucketItem> bucketItems(@NonNull List<B2Bucket> b2Buckets) {
        List<BucketItem> bucketItemList = new ArrayList<>(b2Buckets.size());
        for (B2Bucket b2Bucket : b2Buckets) {
            bucketItemList.add(new BucketItem(b2Bucket));
        }
        return bucketItemList;
    }
    public static @NonNull List<FileItem> fileItems(@NonNull List<B2FileVersion> b2FileVersions){
        List<FileItem> items = new ArrayList<>(b2FileVersions.size());
        for( B2FileVersion fileVersion : b2FileVersions ){
            items.add(new FileItem(fileVersion.getFileName(), fileVersion.getFileId(), FileItem.State.UNKNOWN));
        }
        return items;
    }
}
