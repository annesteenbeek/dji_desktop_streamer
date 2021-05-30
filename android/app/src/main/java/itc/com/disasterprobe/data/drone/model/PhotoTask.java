package itc.com.disasterprobe.data.drone.model;

import android.util.Base64;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by anne on 8-5-18.
 */

public class PhotoTask {
    private File directory;
    private String filename;
    private long downloadProgress = 0;
    private long downloadSize = 0;
    private int uploadProgress = 0;
    private PhotoState state = null;

    public PhotoTask(File directory, String filename, PhotoState state) {
       this.directory = directory;
       this.filename = filename;
       this.state = state;
    }

    public PhotoState getState() {
        return state;
    }

    public void setState(PhotoState state) {
        this.state = state;
        if (state == PhotoState.UPLOADED) {
            if (getFullPath().exists() ){
                // TODO delete file after upload
//                getFullPath().delete();
            }
        }
    }

    public File getDirectory() {
        return directory;
    }

    public File getFullPath() {
        return new File(directory.toString() + "/" +filename);
    }

    public void setDirectory(File directory) {
        this.directory = directory;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getDownloadProgress() {
        return downloadProgress;
    }

    public void setDownloadProgress(long downloadProgress) {
        this.downloadProgress = downloadProgress;
    }

    public long getDownloadSize() {
        return downloadSize;
    }

    public void setDownloadSize(long downloadSize) {
        this.downloadSize = downloadSize;
    }

    public int getDownloadProgressPercent() {
        return (int) (downloadProgress*100/downloadSize);
    }

    public int getUploadProgress() {
        return uploadProgress;
    }

    public void setUploadProgress(int uploadProgress) {
        this.uploadProgress = uploadProgress;
    }

    public String getEncodedImage() {
        File imagefile = getFullPath();
        int size = (int) imagefile.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(imagefile));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String encoded = Base64.encodeToString(bytes, Base64.DEFAULT);
        return encoded;
    }

}
