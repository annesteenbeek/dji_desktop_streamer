package itc.com.disasterprobe.data.drone.model;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import dji.common.mission.waypoint.Waypoint;
import dji.common.mission.waypoint.WaypointAction;
import dji.common.mission.waypoint.WaypointActionType;
import dji.common.mission.waypoint.WaypointMission;
import dji.common.mission.waypoint.WaypointMissionFinishedAction;
import dji.common.mission.waypoint.WaypointMissionFlightPathMode;
import dji.common.mission.waypoint.WaypointMissionGotoWaypointMode;
import dji.common.mission.waypoint.WaypointMissionHeadingMode;
import timber.log.Timber;

/**
 * Created by anne on 23-3-18.
 */

/**
 * This class is used as a container for waypoints
 */
public class ProbeMission {

    private List<ProbeWaypoint> waypointList = new ArrayList<>();
    private float autoFlightSpeed = 7.0f;
    private WaypointMission djiMission;
    private MissionState state;
    private MissionType missionType;
    private String missionName = "";
    private JSONObject missionJson;
    private String errorDescription = null;
    private boolean isDisplayed = false;
    private int totalWaypoints = 0;
    private int uploadProgress = -1;
    private int targetWaypoint = 0;


    public ProbeMission(JSONObject newMissionJson) {
        missionJson = newMissionJson;
        if(validate()){
            try {
                decodeJsonMission();
                setState(MissionState.PROTOTYPE);
            } catch (JSONException e) {
                setState(MissionState.INVALID);
                e.printStackTrace();
            }
        } else {
            setState(MissionState.INVALID);
        }
    }

    public void decodeJsonMission() throws JSONException {
        switch(missionJson.getString("mission_type")) {
            case "NADIR":
                missionType = MissionType.NADIR;
                break;
            case "PHOTOPOINTS":
                missionType = MissionType.PHOTOPOINTS;
                break;
            case "ALPR":
                missionType = MissionType.ALPR;
                break;
            default:
                Timber.e("Unknown mission type");
                state = MissionState.INVALID;
                return;
        }

        missionName = missionJson.getString("mission_name");

        if (missionType == MissionType.NADIR) {
            autoFlightSpeed = (float) missionJson.getDouble("autoFlightSpeed");
        }
        JSONArray waypointArrayJson = missionJson.getJSONArray("waypoints");

        for (int i=0; i<waypointArrayJson.length(); i++) {
            try {
                JSONObject waypointJson = waypointArrayJson.getJSONObject(i);

                double latitude = waypointJson.getDouble("latitude");
                double longitude = waypointJson.getDouble("longitude");
                float altitude = (float) waypointJson.getDouble("altitude");

                ProbeWaypoint waypoint = new ProbeWaypoint(latitude, longitude, altitude);

                int gimbalPitch, yaw;
                switch(missionType) {
                    case NADIR:
                        float intervalDistance = (float) waypointJson.getDouble("shootPhotoDistanceInterval");
                        waypoint.setShootPhotoDistanceInterval(intervalDistance);
                        waypoint.setGimbalPitch(-90);
                        break;
                    case PHOTOPOINTS:
                        gimbalPitch = waypointJson.getInt("gimbalPitch");
                        yaw = waypointJson.getInt("yaw");
                        waypoint.setGimbalPitch(gimbalPitch);
                        waypoint.setHeading(yaw);
                        break;
                    case ALPR:
                        gimbalPitch = waypointJson.getInt("gimbalPitch");
                        yaw = waypointJson.getInt("yaw");
                        waypoint.setGimbalPitch(gimbalPitch);
                        waypoint.setHeading(yaw);
                        break;
                }

                waypointList.add(waypoint);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        totalWaypoints = waypointArrayJson.length();
        djiMission = buildMission();
    }

    public JSONObject toJson() {
        JSONObject result = new JSONObject();
        try {
            // TODO is sending mission on each waypoint necesary?
//            result.put("waypoints", missionJson);
//            result.put("targetWaypoint", targetWaypoint);
            result.put("state", state.toString());
            result.put("uploadProgress", uploadProgress);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Validates the current mission, and sets error if any.
     * @return
     */
    public boolean validate() {
        // TODO: crash, fixed in 4.11 https://github.com/dji-sdk/Mobile-SDK-Android/issues/465
//        DJIError missionError = djiMission.checkParameters();
//
//        if (missionError!=null) {
//            setErrorDescription(missionError.getDescription());
//            return false;
//        } else {
//            return true;
//        }
        return true;
    }

    @Nullable
    public WaypointMission buildMission() {
        WaypointMission.Builder builder = new WaypointMission.Builder();

        // TODO configure these parameters
        builder.autoFlightSpeed(autoFlightSpeed);
        builder.maxFlightSpeed(10.0f);
//        builder.setExitMissionOnRCSignalLostEnabled(false);
        builder.finishedAction(WaypointMissionFinishedAction.GO_HOME);
        builder.flightPathMode(WaypointMissionFlightPathMode.NORMAL);
        builder.gotoFirstWaypointMode(WaypointMissionGotoWaypointMode.SAFELY); // default
        builder.headingMode(WaypointMissionHeadingMode.AUTO);
        builder.setGimbalPitchRotationEnabled(true);

        for (ProbeWaypoint point : waypointList){
            Waypoint djiWaypoint = new Waypoint(point.getLatitude(), point.getLongitude(), point.getAltitude());

            switch (missionType) {
                case NADIR:
                    djiWaypoint.gimbalPitch = point.getGimbalPitch();
                    djiWaypoint.shootPhotoDistanceInterval = point.getShootPhotoDistanceInterval();
                    break;
                case PHOTOPOINTS:
                    djiWaypoint.addAction(new WaypointAction(WaypointActionType.GIMBAL_PITCH, point.getGimbalPitch()));
                    djiWaypoint.addAction(new WaypointAction(WaypointActionType.ROTATE_AIRCRAFT, point.getHeading()));
                    djiWaypoint.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                    break;
                case ALPR:
                    djiWaypoint.addAction(new WaypointAction(WaypointActionType.GIMBAL_PITCH, point.getGimbalPitch()));
                    djiWaypoint.addAction(new WaypointAction(WaypointActionType.ROTATE_AIRCRAFT, point.getHeading()));
                    djiWaypoint.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                    break;
            }
            builder.addWaypoint(djiWaypoint);
        }

        builder.waypointCount(totalWaypoints);

        WaypointMission builtMission = builder.build();
        return builtMission;
    }

    public WaypointMission getWaypointMission() {
        return djiMission;
    }


    public List<ProbeWaypoint> getWaypointList() {
        return waypointList;
    }

    public int getTotalWaypoints() {
        return totalWaypoints;
    }

    /**
     * Set the index value of the last uploaded waypoint, waypoints are uploaded in ascending order.
     * If no waypoint is uploaded, the index is -1
     * @param index
     */
    public void setUploadProgress(int index) {
        uploadProgress = index;
    }

    public int getUploadProgress() {
        return (uploadProgress+1)*100/totalWaypoints;
    }

    public int getUploadIndex() {
        return uploadProgress;
    }

    public int getTargetWaypoint() {
        return targetWaypoint;
    }

    public void setTargetWaypoint(int targetWaypoint) {
        this.targetWaypoint = targetWaypoint;
    }

    public void setWaypointReached(int index) {
        waypointList.get(index).setReached(true);
    }

    public int getMissionProgress() {
        return (targetWaypoint*100)/totalWaypoints;
    }

    public boolean isDisplayed() {
        return isDisplayed;
    }

    public void setDisplayed(boolean displayed) {
        isDisplayed = displayed;
    }

    public void setErrorDescription(String errorDescription) {
        Timber.e("Error in waypoint mission: " + errorDescription);
        this.errorDescription = errorDescription;
    }

    public MissionState getState() {
        return state;
    }

    public void setState(MissionState state) {
        this.state = state;
    }

    public MissionType getMissionType() {
        return missionType;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public String getMissionName() {
        return missionName;
    }

}


