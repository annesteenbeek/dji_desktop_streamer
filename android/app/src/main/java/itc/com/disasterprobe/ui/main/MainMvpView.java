package itc.com.disasterprobe.ui.main;

import com.google.android.gms.maps.GoogleMap;

import itc.com.disasterprobe.data.drone.model.PhotoTaskList;
import itc.com.disasterprobe.data.drone.model.ProbeMission;
import itc.com.disasterprobe.ui.base.MvpView;

/**
 * Created by anne on 18-3-18.
 */

public interface MainMvpView extends MvpView {

    void displayMissionMap(ProbeMission probeMission);

    void displayMissionState(ProbeMission probeMission);

    void displayPhotoInfo(PhotoTaskList photoQue);

    void displayConnectionStatus(boolean connected);
}
