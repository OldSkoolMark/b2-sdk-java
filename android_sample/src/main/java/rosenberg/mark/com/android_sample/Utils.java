package rosenberg.mark.com.android_sample;

import java.io.File;

public class Utils {
    public static String extractFileNameFromPath( String path){
        String fileName = path.contains(File.separator) ? path.substring(path.lastIndexOf(File.separator)) : path;
        return fileName.startsWith(File.separator) ? fileName.substring(1) : fileName;
    }
}
