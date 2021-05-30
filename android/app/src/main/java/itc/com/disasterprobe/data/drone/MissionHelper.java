package itc.com.disasterprobe.data.drone;

import android.content.Context;
import android.content.res.AssetManager;

import androidx.annotation.Nullable;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Singleton;

import dji.common.error.DJIError;
import dji.common.mission.waypoint.WaypointExecutionProgress;
import dji.common.mission.waypoint.WaypointMissionDownloadEvent;
import dji.common.mission.waypoint.WaypointMissionExecutionEvent;
import dji.common.mission.waypoint.WaypointMissionState;
import dji.common.mission.waypoint.WaypointMissionUploadEvent;
import dji.common.mission.waypoint.WaypointUploadProgress;
import dji.common.util.CommonCallbacks;
import dji.sdk.mission.waypoint.WaypointMissionOperator;
import dji.sdk.mission.waypoint.WaypointMissionOperatorListener;
import dji.sdk.sdkmanager.DJISDKManager;
import itc.com.disasterprobe.data.DataManager;
import itc.com.disasterprobe.data.drone.model.MissionState;
import itc.com.disasterprobe.data.drone.model.ProbeMission;
import itc.com.disasterprobe.data.drone.model.ProductState;
import timber.log.Timber;

/**
 * Created by anne on 18-3-18.
 */

@Singleton
public class MissionHelper {

    private WaypointMissionOperator instance;
    private Context mApplicationContext;
    private DataManager mDataManager;
    private boolean isConnected = false;
    private ProbeMission currentMission = null;
    private AtomicBoolean isInitialized = new AtomicBoolean(false);

    private ProbeMission testMission;

    public MissionHelper(Context applicationContext, DataManager dataManager) {
        mApplicationContext = applicationContext;
        mDataManager = dataManager;
        EventBus.getDefault().register(this);

//        // ONLY FOR DEV PURPOSES!!!!!!!!!!
//        String filename = "testCoordinates.shorter.json";
//        JSONArray jsonArray = null;
//        try {
//            JSONObject jsonMission = new JSONObject(readFile(filename));
//            jsonArray = jsonMission.getJSONArray("mission");
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        testMission = new ProbeMission(jsonArray, MissionState.PROTOTYPE);
    }

    @Subscribe
    public void onProbeMission(ProbeMission probeMission) {
        currentMission = probeMission;
        if (isInitialized.get() && currentMission.getState() == MissionState.PROTOTYPE) {
            loadWayPointMission(currentMission);
        }
   }

    @Subscribe
    public void onProductState(ProductState productState) {
        if (productState.getSdkRegistered() && productState.getFlightControllerConnected()) {
            if (isInitialized.compareAndSet(false, true)) {
                addListener();
                if (currentMission != null && currentMission.getState() == MissionState.PROTOTYPE) {
                    loadWayPointMission(currentMission);
                }
            }
        }
    }


    private WaypointMissionOperator getWaypointMissionOperator() {
       return DJISDKManager.getInstance().getMissionControl().getWaypointMissionOperator();
    }

    private void addListener() {
        getWaypointMissionOperator().addListener(eventNotificationListener);
    }

    private void removeListener() {
        if (getWaypointMissionOperator() != null) {
            getWaypointMissionOperator().removeListener(eventNotificationListener);
        }
    }

    private WaypointMissionOperatorListener eventNotificationListener = new WaypointMissionOperatorListener() {
        @Override
        public void onDownloadUpdate(WaypointMissionDownloadEvent downloadEvent) {

        }

        @Override
        public void onUploadUpdate(WaypointMissionUploadEvent uploadEvent) {

            if (uploadEvent.getCurrentState() == WaypointMissionState.READY_TO_EXECUTE) {
                currentMission.setState(MissionState.UPLOADED);
            } else if (uploadEvent.getCurrentState() == WaypointMissionState.UPLOADING) {
                WaypointUploadProgress progress = uploadEvent.getProgress();
                currentMission.setUploadProgress(progress.uploadedWaypointIndex);
            }
            EventBus.getDefault().post(currentMission);
        }

        @Override
        public void onExecutionUpdate(WaypointMissionExecutionEvent executionEvent) {
            WaypointExecutionProgress progress = executionEvent.getProgress();
            currentMission.setTargetWaypoint(progress.targetWaypointIndex);
            if (progress.isWaypointReached) {
                currentMission.setWaypointReached(progress.targetWaypointIndex);
            }
            EventBus.getDefault().post(currentMission);
        }

        @Override
        public void onExecutionStart() {
            currentMission.setState(MissionState.RUNNING);
            EventBus.getDefault().post(currentMission);
        }

        @Override
        public void onExecutionFinish(@Nullable final DJIError error) {
            if (error == null) {
                Timber.i("Execution finished: Success!");
                currentMission.setState(MissionState.FINISHED);
            } else {
                Timber.e("Execution problem: " + error.getDescription());
                currentMission.setState(MissionState.STOPPED);
            }
            EventBus.getDefault().post(currentMission);

        }
    };

    public void loadWayPointMission() {
        // ONLY FOR DEV PURPOSES !!!
        EventBus.getDefault().post(testMission);
//        WaypointMission builtMission = buildMission(testMission);
//        loadWayPointMission(builtMission);
    }

    /**
     * This function is used to upload a waypoint mission using the Waypoint Mission Operator.
     * The waypoints are first shared to the Mission operator, then the Operator uploads it to the
     * device.
     * @param probeMission
     */
    public void loadWayPointMission(ProbeMission probeMission) {

        DJIError loadError = getWaypointMissionOperator().loadMission(probeMission.getWaypointMission());
        if (loadError== null) {
            Timber.i("loadWaypoint succeeded");
            probeMission.setState(MissionState.LOADED);
            currentMission = probeMission;
        } else {
            probeMission.setState(MissionState.LOAD_FAILED);
            probeMission.setErrorDescription(loadError.getDescription());
            Timber.e("loadWaypoint failed " + loadError.getDescription());
        }

        EventBus.getDefault().post(probeMission);
    }

    public void uploadWayPointMission() {
        if (currentMission != null) {
            getWaypointMissionOperator().uploadMission(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError error) {
                    if (error == null) {
                        Timber.i("Mission upload successfully!");
                        currentMission.setState(MissionState.UPLOADING);
                    } else {
                        Timber.e("Mission upload failed, error: " + error.getDescription());
                        currentMission.setState(MissionState.UPLOAD_FAILED);
    //                    getWaypointMissionOperator().retryUploadMission(null);
                    }
                    EventBus.getDefault().post(currentMission);
                }
            });
        } else {
            mDataManager.makeToast("Unable to upload mission, load mission first");
        }
    }

    public void startWaypointMission() {
        getWaypointMissionOperator().startMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                Timber.i("Mission Start: " + (djiError == null ? "Successfully" : djiError.getDescription()));
                if (djiError == null) {
                    mDataManager.makeToast("Start mission successfull");
                } else {
                    mDataManager.makeToast("Unable to start mission: " + djiError.getDescription());
                }
            }
        });
    }

    public void stopWaypointMission() {
        getWaypointMissionOperator().stopMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError == null) {
                    Timber.i("Stop mission successfull");
                    if (currentMission != null) {
                        currentMission.setState(MissionState.STOPPED);
                        EventBus.getDefault().post(currentMission);
                    }
                } else {
                    Timber.e("Unable to stop mission: " + djiError.getDescription());
                }
            }
        });
    }

    // ONLY FOR DEBUG PURPOSES
    private String readFile(String filename) throws IOException, IOException {
        AssetManager assetManager = mApplicationContext.getAssets();
        StringBuilder buf = new StringBuilder();
        InputStream json = assetManager.open(filename);
        BufferedReader in =
                new BufferedReader(new InputStreamReader(json, "UTF-8"));
        String str;

        while ((str=in.readLine()) != null) {
            buf.append(str);
        }

        in.close();
        return buf.toString();
    }

}
