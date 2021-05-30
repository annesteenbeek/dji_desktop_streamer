package itc.com.disasterprobe.data.drone.model;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by anne on 8-5-18.
 */

public class PhotoTaskList {

    private ArrayList<PhotoTask> photoTaskList = new ArrayList<PhotoTask>();

    public PhotoTaskList(){}

    public void add(PhotoTask photoTask) {
        photoTaskList.add(photoTask);
    }

    public ArrayList<PhotoTask> filterList(PhotoState state) {
        ArrayList<PhotoTask> result = new ArrayList<>();
        for (PhotoTask photo : photoTaskList) {
            if (photo.getState() == state) {
                result.add(photo);
            }
        }
        return result;
    }

    public ArrayList<PhotoTask> filterList(PhotoState[] state) {
        ArrayList<PhotoTask> result = new ArrayList<>();
        for (PhotoTask photo : photoTaskList) {
            if (Arrays.asList(state).contains(photo.getState())) {
                result.add(photo);
            }
        }
        return result;
    }

    public int getDownloadedCount() {
        PhotoState[] downloadedEnums = new PhotoState[]{
                PhotoState.DOWNLOADED,
                PhotoState.UPLOADING,
                PhotoState.UPLOADED};
        return filterList(downloadedEnums).size();
    }

    public int getLatestDownloadProgress() {
       int progress = 0;
       ArrayList<PhotoTask> dowloads = filterList(PhotoState.DOWNLOADING);
       if (!dowloads.isEmpty()) {
           progress = dowloads.get(0).getDownloadProgressPercent();
       }
       return progress;
    }

    public PhotoTask getTaskByName(String filename) {
        for (PhotoTask task : photoTaskList) {
            if (task.getFilename().equals(filename)){
                return task;
            }
        }
        return null;
    }

    public int size() {
        return photoTaskList.size();
    }

}
