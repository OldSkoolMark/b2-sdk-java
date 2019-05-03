package rosenberg.mark.com.android_sample;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PendingDownloadList {
    private PendingDownloadList(){}
    public static PendingDownloadList getInstance(){
       instance = instance != null ? instance :  new PendingDownloadList();
       return instance;
    }
    private static PendingDownloadList instance;
    private final List<FileItem> pendingDownloads = Collections.synchronizedList(new ArrayList<>());
    public void addAll(Collection< ? extends FileItem> c){
        pendingDownloads.addAll(c);
    }

    public void addFileItem(FileItem fileItem){
        pendingDownloads.add(fileItem);
    }

    public void removeFileItem(String fileID){
        FileItem fileItem = getFileItem(fileID);
        pendingDownloads.remove(fileItem);
    }

    public boolean containsFileItem(String fileID){
        return !(getFileItem(fileID) == null);
    }
    public FileItem getFileItem(String fileID){
        for( FileItem i : pendingDownloads){
            if( i.id.equals(fileID)){
                return i;
            }
        }
        return null;
    }
}
