package rosenberg.mark.com.android_sample;

import com.backblaze.b2.client.structures.B2Bucket;

public class BucketItem {

    public final String name;
    public final String id;

    public BucketItem(B2Bucket bucket) {
        name = bucket.getBucketName();
        id = bucket.getBucketId();
    }
}
