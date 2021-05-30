package itc.com.disasterprobe.data.drone;

import android.content.Context;
import android.graphics.PointF;
import android.os.Environment;

import androidx.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Singleton;

import dji.common.camera.SettingsDefinitions;
import dji.common.camera.SystemState;
import dji.common.error.DJIError;
//import dji.sdk.camera.DownloadListener;
import dji.common.util.CommonCallbacks;
import dji.sdk.camera.Camera;
import dji.sdk.media.DownloadListener;
import dji.sdk.media.MediaFile;
//import dji.sdk.camera.MediaFile;
import dji.sdk.sdkmanager.DJISDKManager;
import itc.com.disasterprobe.data.DataManager;
import itc.com.disasterprobe.data.drone.model.PhotoEvent;
import itc.com.disasterprobe.data.drone.model.PhotoState;
import itc.com.disasterprobe.data.drone.model.PhotoTask;
import itc.com.disasterprobe.data.drone.model.PhotoTaskList;
import itc.com.disasterprobe.data.drone.model.ProbeMission;
import itc.com.disasterprobe.data.drone.model.ProductState;
import timber.log.Timber;

/**
 * Created by anne on 1-5-18.
 */


@Singleton
public class CameraHelper {

    private final DataManager mDataManager;
    private final Context mApplicationContext;
    private AtomicBoolean isInitialized = new AtomicBoolean(false);
    private PhotoTaskList photoQue = new PhotoTaskList();
    File destDir = new File(Environment.getExternalStorageDirectory().getPath() + "/disasterprobe_cache/");

    public CameraHelper(Context applicationContext, DataManager dataManager) {
        mApplicationContext = applicationContext;
        mDataManager = dataManager;

        EventBus.getDefault().register(this);
    }

    @Subscribe
    public void onProductState(ProductState productState) {
        if (productState.getSdkRegistered() && productState.getFlightControllerConnected()) {
            if (null != DJISDKManager.getInstance().getProduct() && isInitialized.compareAndSet(false, true)) {
                mDataManager.makeToast("camera helper registered");
                startCameraListener();
            }
        }
        if (!productState.getFlightControllerConnected() && isInitialized.get()) {
           isInitialized.set(false);
        }
    }

    @Subscribe
    public void onCurrentMission(ProbeMission currentMission) {
        if (currentMission.getTargetWaypoint() >= 1) { // first waypoint
            focusCamera();
        }
    }

    public void focusCamera() {
        Camera camera = DJISDKManager.getInstance().getProduct().getCamera();

        if (camera.isAdjustableApertureSupported()) {
            camera.setFocusMode(SettingsDefinitions.FocusMode.AUTO, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError == null) {
                        camera.setFocusTarget(new PointF(0.5f, 0.5f), null);
                    } else {
                        Timber.e("Autofocus error: " + djiError.getDescription());
                    }
                }
            });

        } else {
            Timber.e("Adjustable focus not supported, no auto focus");
        }

    }

    private void startCameraListener() {
        // TODO: sometimes called on null???
        DJISDKManager.getInstance().getProduct().getCamera()
                .setSystemStateCallback(new SystemState.Callback() {
                    @Override
                    public void onUpdate(SystemState systemState) {
                        if(systemState.isShootingSinglePhoto()) {
                            Timber.i("Shooting single photo");
                            PhotoEvent event = new PhotoEvent();
                            event.setType("single");
                            EventBus.getDefault().post(event);
                        }

                        if(systemState.isShootingIntervalPhoto()){
                            Timber.i("Shooting interval photo");
                            PhotoEvent event = new PhotoEvent();
                            event.setType("interval");
                            EventBus.getDefault().post(event);
                        }
                    }
                });

        DJISDKManager.getInstance().getProduct().getCamera()
                .setMediaFileCallback(new MediaFile.Callback() {
            @Override
            public void onNewFile(@NonNull MediaFile mediaFile) {
                mDataManager.makeToast("New mediafile");
                Timber.i("New mediafile");
                // List of image types, to prevent video download
                MediaFile.MediaType[] imageTypes = {
                        MediaFile.MediaType.JPEG,
                        MediaFile.MediaType.TIFF};
//                        MediaFile.MediaType.RAW_DNG};
                MediaFile.MediaType type = mediaFile.getMediaType();
                if (!Arrays.asList(imageTypes).contains(type)) {
                    return;
                }
                String filename = mediaFile.getFileName();
                PhotoTask photoTask = new PhotoTask(destDir,filename, PhotoState.ON_SDCARD);
                photoTask.setDownloadSize(mediaFile.getDownloadedSize()); // TODO compare to total with onprogress
                photoQue.add(photoTask);
                EventBus.getDefault().post(photoQue);
                // call mediaManager first due to bug: https://github.com/dji-sdk/Mobile-SDK-Android/issues/143
                mDataManager.getCameraInstance().getMediaManager();
                mediaFile.fetchFileData(destDir, null, new DownloadListener<String>() {
                    @Override
                    public void onStart() {
                        Timber.i("Started download of " + filename);
                        photoTask.setState(PhotoState.DOWNLOADING);
                        EventBus.getDefault().post(photoQue);
                    }

                    @Override
                    public void onRateUpdate(long l, long l1, long l2) {

                    }

                    @Override
                    public void onProgress(long total, long current) {
                        photoTask.setDownloadProgress(current);
                        EventBus.getDefault().post(photoQue);
                    }

                    @Override
                    public void onSuccess(String s) {
                        Timber.i("Finished download");
                        photoTask.setState(PhotoState.DOWNLOADED);
                        EventBus.getDefault().post(photoQue);
                    }

                    @Override
                    public void onFailure(DJIError djiError) {
                        Timber.e("Media download error: " + djiError.getDescription());
                        photoTask.setState(PhotoState.DOWNLOAD_FAILED);
                        EventBus.getDefault().post(photoQue);
                        mDataManager.makeToast("Downlaod failed: " + djiError.getDescription());
                    }
                });
            }
        });

    }
}
