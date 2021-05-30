package itc.com.disasterprobe.data.drone;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Singleton;

import dji.common.error.DJIError;
import dji.midware.data.config.P3.Ccode;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.sdk.sdkmanager.LiveStreamManager;
import itc.com.disasterprobe.data.DataManager;
import itc.com.disasterprobe.data.drone.model.ProductState;
import itc.com.disasterprobe.data.nsd.model.NetworkService;
import timber.log.Timber;

@Singleton
public class LiveStreamHelper {

    private final DataManager mDataManager;
    private final Context mApplicationContext;

    private AtomicBoolean isInitialized = new AtomicBoolean(false);
    private LiveStreamManager.OnLiveChangeListener listener;
    private String liveShowUrl = "";
//    private String liveShowUrl = "rtmp://192.168.2.21/live"; // for debugging

    public LiveStreamHelper(Context applicationContext, DataManager dataManager) {
        mApplicationContext = applicationContext;
        mDataManager = dataManager;

        EventBus.getDefault().register(this);
    }

    private void initListener() {
        listener = new LiveStreamManager.OnLiveChangeListener() {
            @Override
            public void onStatusChanged(int i) {
               Timber.i("status changed : " + i);
            }
        };
    }

    @Subscribe
    public void onProductState(ProductState productState) {

        if (productState.getSdkRegistered() && productState.getFlightControllerConnected()) {
            if (isInitialized.compareAndSet(false, true)) {
                startLiveShow();
            }
        }
        if (!productState.getFlightControllerConnected() && isInitialized.get()) {
            isInitialized.set(false);
            stopLiveShow();
        }

    }

    @Subscribe
    public void onNetworkService(NetworkService networkService) {
        liveShowUrl = "rtmp://"+networkService.getAddress()+"/live";
        startLiveShow();
    }

    void startLiveShow() {
        if (liveShowUrl == "") { // No RTMP server discovered yet
            Timber.i("No RTMP server discovered yet");
            return;
        }
        if (isInitialized.equals(false)) { // SDK not ready yet
            Timber.i("SDK not ready yet");
            return;
        }
        if (!isLiveStreamManagerOn()) {
            Timber.i("LivestreamManager unavailable");
            return;
        }
        if (DJISDKManager.getInstance().getLiveStreamManager().isStreaming()) {
            Timber.i("Already streaming");
            return;
        }

        mDataManager.makeToast("Starting livestream");
        Timber.i("Liveshow url: " + liveShowUrl);
        new Thread() {
            @Override
            public void run() {
                LiveStreamManager manager = DJISDKManager.getInstance().getLiveStreamManager();
                initListener();
                manager.registerListener(listener);
                manager.setLiveUrl(liveShowUrl);
                manager.setVideoEncodingEnabled(true);
//                manager.setAudioStreamingEnabled(false);
                int result = manager.startStream();

                DJISDKManager.getInstance().getLiveStreamManager().setStartTime();
                Timber.i("Livestream start result: " + result);
                if(result != 0) {
                    DJIError error = DJIError.getDJIError(Ccode.find(result));
                    Timber.e("Received livestream error: " + error.getDescription() );
                }
            }
        }.start();
    }

    public void enableReEncoder() {
        if (!isLiveStreamManagerOn()) {
            return;
        }
        DJISDKManager.getInstance().getLiveStreamManager().setVideoEncodingEnabled(true);
    }

    public void disableReEncoder() {
        if (!isLiveStreamManagerOn()) {
            return;
        }
        DJISDKManager.getInstance().getLiveStreamManager().setVideoEncodingEnabled(false);
    }

    public void stopLiveShow() {
        if (!isLiveStreamManagerOn()) {
            return;
        }
        DJISDKManager.getInstance().getLiveStreamManager().stopStream();
        mDataManager.makeToast("Live show stopped");
    }

    public boolean isLiveStreamManagerOn() {
        if (DJISDKManager.getInstance().getLiveStreamManager() == null) {
            return false;
        }
        return true;
    }

}
