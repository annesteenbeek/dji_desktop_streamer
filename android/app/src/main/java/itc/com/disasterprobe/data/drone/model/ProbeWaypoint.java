package itc.com.disasterprobe.data.drone.model;

/**
 * Created by anne on 24-3-18.
 */

/**
 * This class is used to hold a single waypoint
 */
public class ProbeWaypoint {

    private double latitude;
    private double longitude;
    private float altitude;
    private int gimbalPitch;
    private int heading;
    private float shootPhotoDistanceInterval = 0;
    private boolean reached = false;

    public ProbeWaypoint(double latitude, double longitude, float altitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }

    public ProbeWaypoint() {}

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public float getAltitude() {
        return altitude;
    }

    public void setAltitude(float altitude) {
        this.altitude = altitude;
    }

    public int getGimbalPitch() {
        return gimbalPitch;
    }

    public void setGimbalPitch(int gimbalPitch) {
        this.gimbalPitch = gimbalPitch;
    }

    public int getHeading() {
        return heading;
    }

    public void setHeading(int heading) {
        this.heading = heading;
    }

    public float getShootPhotoDistanceInterval() {
        return shootPhotoDistanceInterval;
    }

    public void setShootPhotoDistanceInterval(float shootPhotoDistanceInterval) {
        this.shootPhotoDistanceInterval = shootPhotoDistanceInterval;
    }

    public boolean isReached() {
        return reached;
    }

    public void setReached(boolean reached) {
        this.reached = reached;
    }


}
