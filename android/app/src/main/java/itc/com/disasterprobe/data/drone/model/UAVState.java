package itc.com.disasterprobe.data.drone.model;

import com.koushikdutta.async.http.body.JSONObjectBody;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import dji.common.flightcontroller.Attitude;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.LocationCoordinate3D;
import dji.common.product.Model;
import timber.log.Timber;

/**
 * Created by anne on 22-3-18.
 */

public class UAVState {

    private FlightControllerState state;
    private long timestamp;

    public UAVState(FlightControllerState state) {
        this.state = state;
        timestamp = new Date().getTime();
    }

    public FlightControllerState getState() {
        return state;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public JSONObject getLocationJson() throws JSONException {
        JSONObject location = new JSONObject();
        LocationCoordinate3D djiLocation = state.getAircraftLocation();
        boolean isnan = Double.isNaN(djiLocation.getLatitude());
        location.put("available", isnan ? "false" : "true");
        location.put("latitude", isnan ? 0 : djiLocation.getLatitude());
        location.put("longitude", isnan ? 0 : djiLocation.getLongitude());
        location.put("altitude", isnan ? 0 : djiLocation.getAltitude());

        return location;
    }

    public JSONObject getAttitudeJson() throws JSONException {
        JSONObject attitude = new JSONObject();
        Attitude djiAttitude = state.getAttitude();
        attitude.put("pitch", djiAttitude.pitch);
        attitude.put("roll", djiAttitude.roll);
        attitude.put("yaw", djiAttitude.yaw);

        return attitude;
    }

    public JSONObject getVelocityJson() throws JSONException {
        JSONObject velocity = new JSONObject();
        velocity.put("x", state.getVelocityX());
        velocity.put("y", state.getVelocityY());
        velocity.put("z", state.getVelocityZ());

        return velocity;
    }

    public JSONObject toJson() {
       JSONObject result = new JSONObject();
        try {
            result.put("timestamp", timestamp);
            result.put("motorsOn", state.areMotorsOn());
            result.put("flying", state.isFlying());

            result.put("aircraftLocation", getLocationJson());
            result.put("takeoffLocationAltitude", state.getTakeoffLocationAltitude());
            result.put("attitude", getAttitudeJson());
            result.put("velocity", getVelocityJson());

            result.put("flightTimeInSeconds", state.getFlightTimeInSeconds());

            result.put("landingConfirmationNeeded", state.isLandingConfirmationNeeded());
            result.put("flightMode", state.getFlightModeString());

            result.put("satelliteCount", state.getSatelliteCount());
            result.put("GPSSignalLevel", state.getGPSSignalLevel());
            result.put("isIMUPreheating", state.isIMUPreheating());
            result.put("isUltrasonicBeingUsed", state.isUltrasonicBeingUsed());
            result.put("ultrasonicHeightInMeters", state.getUltrasonicHeightInMeters());
            result.put("doesUltrasonicHaveError", state.doesUltrasonicHaveError());
            result.put("isVisionPositioningSensorBeingUsed", state.isVisionPositioningSensorBeingUsed());

            result.put("orientationMode", state.getOrientationMode());
            result.put("isFailsafeEnabled", state.isFailsafeEnabled());
            result.put("batteryThresholdBehavior", state.getBatteryThresholdBehavior()); // enum
            result.put("isLowerThanBatteryWarningThreshold", state.isLowerThanBatteryWarningThreshold());
            result.put("isLowerThanSeriousBatteryWarningThreshold", state.isLowerThanSeriousBatteryWarningThreshold());
            result.put("flightWindWarning", state.getFlightWindWarning());
//            result.put("flightCount", state.getFightCount());
            result.put("flightLogIndex", state.getFlightLogIndex());
            result.put("isActiveBrakeEngaged", state.isActiveBrakeEngaged());

            result.put("isHomeLocationSet", state.isHomeLocationSet());

            JSONObject homeLocation = new JSONObject();
            homeLocation.put("latitude", state.getHomeLocation().getLatitude());
            homeLocation.put("longitude", state.getHomeLocation().getLongitude());
            result.put("homeLocation", homeLocation);

// GoHomeAssessment
            result.put("goHomeExecutionState", state.getGoHomeExecutionState()); // Enum
            result.put("isGoingHome", state.isGoingHome());
            result.put("goHomeHeight", state.getGoHomeHeight());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

}
