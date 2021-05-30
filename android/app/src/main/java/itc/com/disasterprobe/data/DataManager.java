package itc.com.disasterprobe.data;

import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import itc.com.disasterprobe.data.drone.CameraHelper;
import itc.com.disasterprobe.data.drone.ConnectionHelper;
import itc.com.disasterprobe.data.drone.LiveStreamHelper;
import itc.com.disasterprobe.data.drone.MissionHelper;
import itc.com.disasterprobe.data.nsd.NsdHelper;
import itc.com.disasterprobe.data.socket.SocketHelper;
import itc.com.disasterprobe.di.ApplicationContext;

import android.content.Context;
import android.widget.Toast;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by anne on 17-3-18.
 */

@Singleton
public class DataManager {

    private final Context mContext;
    private final NsdHelper mNsdHelper;
    private final SocketHelper mSocketHelper;
    private final ConnectionHelper mConnectionHelper;
    private final CameraHelper mCameraHelper;
    private final MissionHelper mMissionHelper;
    private final LiveStreamHelper mLiveStreamHelper;

    @Inject
    public DataManager(@ApplicationContext Context context) {
        mContext = context;
        // TODO Dependency Injection
        mNsdHelper = new NsdHelper(mContext, this);
        mSocketHelper = new SocketHelper(mContext, this);
        mConnectionHelper = new ConnectionHelper(mContext, this);
        mCameraHelper = new CameraHelper(mContext, this);
        mMissionHelper = new MissionHelper(mContext, this);
        mLiveStreamHelper = new LiveStreamHelper(mContext, this);
    }

    public void startDiscovery(){
       mNsdHelper.discoverServices();
    }

    public void makeToast(String text) {
        Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
    }

    public void loginDJIAccount(Context activityContext) {
        mConnectionHelper.loginAccount(activityContext);
    }

    public void uploadMission() {
        mMissionHelper.uploadWayPointMission();
    }

    public void loadMission() {
        mMissionHelper.loadWayPointMission();
    }

    public void startMission() {
        mMissionHelper.startWaypointMission();
    }

    public void stopMission() {
        mMissionHelper.stopWaypointMission();
    }

    public Camera getCameraInstance() {
        return mConnectionHelper.getCameraInstance();
    }

    public BaseProduct getProductInstance() {
        return mConnectionHelper.getProductInstance();
    }

    public void startDJISDKRegistration() {
        mConnectionHelper.startDJISDKRegistration();
    }



}
