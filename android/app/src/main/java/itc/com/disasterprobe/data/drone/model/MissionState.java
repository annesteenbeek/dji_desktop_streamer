package itc.com.disasterprobe.data.drone.model;

/**
 * Created by anne on 24-3-18.
 */

public enum MissionState {
    PROTOTYPE,
    INVALID,
    LOADED,
    UPLOAD_FAILED,
    UPLOADING,
    UPLOADED,
    RUNNING,
    STOPPED,
    LOAD_FAILED,
    FINISHED
}
