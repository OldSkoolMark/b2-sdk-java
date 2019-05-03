package rosenberg.mark.com.android_sample;

public class FileItem {
    private FileItem(Builder builder) {
        name = builder.name;
        id = builder.id;
        downloadedFilePath = builder.downloadedFilePath;
        percentComplete = builder.percentComplete;
        contentLength = builder.contentLength;
        done = builder.done;
        state = builder.state;
    }

    public enum State {
        UNKNOWN, //
        DOWNLOADED, // downloaded to device, but user may have deleted or removed the local file
        DOWNLOADABLE, // downloadable file in Bucket
        IN_DOWNLOAD_QUEUE, // download is waiting its turn
        DOWNLOADING, // download from server is in progress
        DOWNLOAD_SUCCESS, // file is now on device
        DOWNLOAD_FAILURE,
        UPLOADING, // on device file is being uploaded to the Bucket
        UPLOAD_SUCCESS, // file is now in the Bucket
        UPLOAD_FAILURE,
    }
    public final String name;
    public final String id;
    private String downloadedFilePath;
    private long percentComplete;
    private long contentLength;
    private boolean done;
    private State state;
    private String bucketID;

    public String getBucketID() {
        return bucketID;
    }

    public FileItem setBucketID(String bucketID) {
        this.bucketID = bucketID;
        return this;
    }


    public String getDownloadedFilePath() {
        return downloadedFilePath;
    }

    public FileItem setDownloadedFilePath(String downloadedFilePath) {
        this.downloadedFilePath = downloadedFilePath;
        return this;
    }

    public long getPercentComplete() {
        return percentComplete;
    }

    public FileItem setPercentComplete(long percentComplete) {
        this.percentComplete = percentComplete;
        return this;
    }

    public long getContentLength() {
        return contentLength;
    }

    public FileItem setContentLength(long contentLength) {
        this.contentLength = contentLength;
        return this;
    }

    public boolean isDone() {
        return done;
    }

    public FileItem setDone(boolean done) {
        this.done = done;
        return this;
    }

    public State getState() {
        return state;
    }

    public FileItem setState(State state) {
        this.state = state;
        return this;
    }

    public FileItem(String name, String id, State state) {
        this.name = name;
        this.id = id;
        this.state = state;
    }


    public static final class Builder {
        private final String name;
        private final String id;
        private String bucketID;
        private String downloadedFilePath;
        private long percentComplete;
        private long contentLength;
        private boolean done;
        private State state;

        public Builder(String name, String id, State state) {
            this.name = name;
            this.id = id;
            this.state = state;
        }

        public Builder downloadedFilePath(String val) {
            downloadedFilePath = val;
            return this;
        }

        public Builder bucketID(String val) {
            downloadedFilePath = val;
            return this;
        }

        public Builder percentComplete(long val) {
            percentComplete = val;
            return this;
        }

        public Builder contentLength(long val) {
            contentLength = val;
            return this;
        }

        public Builder done(boolean val) {
            done = val;
            return this;
        }

        public Builder state(State val) {
            state = val;
            return this;
        }

        public FileItem build() {
            return new FileItem(this);
        }
    }
}
