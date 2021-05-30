package itc.com.disasterprobe.data.drone;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Singleton;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.useraccount.UserAccountState;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;
import dji.sdk.products.HandHeld;
import dji.sdk.sdkmanager.DJISDKInitEvent;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.sdk.sdkmanager.LDMManager;
import dji.sdk.useraccount.UserAccountManager;

import itc.com.disasterprobe.BuildConfig;
import itc.com.disasterprobe.data.DataManager;
import itc.com.disasterprobe.data.drone.model.ProductState;
import itc.com.disasterprobe.data.drone.model.UAVState;
import timber.log.Timber;


@Singleton
public class ConnectionHelper {

    private final Context mApplicationContext;
    private final DataManager mDataManager;
    private static BaseProduct mProduct;
    private FlightController mFlightController;
    private boolean isAircraftConnected = false;
    private boolean isRemoteConnected = false;
    private Aircraft mAircraft;
    private HandHeld mHandheld;
    private boolean isLoggedIn = false;
    private boolean isSDKRegistered = false;
    private ProductState mProductState;
    private int lastProcess = -1;

    public ConnectionHelper(Context applicationContext, DataManager dataManager) {
        mApplicationContext = applicationContext;
        mDataManager = dataManager;
        mProductState = new ProductState();
        publishState();

//        Check the permissions before registering the application for android system 6.0 above.
        int permissionCheck = ContextCompat.checkSelfPermission(mApplicationContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionCheck2 = ContextCompat.checkSelfPermission(mApplicationContext, android.Manifest.permission.READ_PHONE_STATE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || (permissionCheck == 0 && permissionCheck2 == 0)) {
            //This is used to start SDK services and initiate SDK.
            DJISDKManager.getInstance().registerApp(mApplicationContext, mDJISDKManagerCallback);
        } else {
            Timber.d("Please check if permission is granted");
        }
    }

    public void publishState() {
        EventBus.getDefault().post(mProductState);
    }

    public void startDJISDKRegistration() {
       AsyncTask.execute(new Runnable() {
           @Override
           public void run() {
               DJISDKManager.getInstance().registerApp(mApplicationContext, mDJISDKManagerCallback);
           }
       });
    }

    public static synchronized BaseProduct getProductInstance() {
        if (null == mProduct) {
            mProduct = DJISDKManager.getInstance().getProduct();
        }
        return mProduct;
    }

    public static synchronized Camera getCameraInstance() {

        if (getProductInstance() == null) return null;

        Camera camera = null;

        if (getProductInstance() != null){
            camera = getProductInstance().getCamera();
        }

        return camera;
    }

    /**
     * This functions is used to register the app with the DJI servers, using the API key
     */
    private DJISDKManager.SDKManagerCallback mDJISDKManagerCallback = new DJISDKManager.SDKManagerCallback() {

        @Override
        public void onRegister(DJIError error) {
            if(error == DJISDKError.REGISTRATION_SUCCESS) {
                Timber.i("SDK registration success");
                isSDKRegistered = true;
                mProductState.setSdkRegistered(isSDKRegistered);
                DJISDKManager.getInstance().startConnectionToProduct();
                publishState();
                // Used to connect to SDK bridge app
                if (BuildConfig.BUILD_TYPE == "debug") {
                    //                    DJISDKManager.getInstance().enableBridgeModeWithBridgeAppIP("192.168.1.240");
                }
            } else {
                Timber.e("Register failed: " + error.getDescription());
            }
        }

        // The base product is a collection of components (remote controller, aircraft, camera, etc..)
        @Override
        public void onProductDisconnect() {
            Timber.i("Product disconnected");
            mProductState.setConnected(false);
            publishState();
        }

        @Override
        public void onProductConnect(BaseProduct baseProduct) {
            mProductState.setConnected(true);
            initFlightController(); // for state callback
            publishState();
        }

        @Override
        public void onProductChanged(BaseProduct product) {

        }

        @Override
        public void onComponentChange(BaseProduct.ComponentKey componentKey, BaseComponent oldComponent, BaseComponent newComponent) {
            if (newComponent != null) {
                Timber.i(String.format("onComponentChange key:%s, oldComponent:%s, newComponent:%s",
                    componentKey,
                    oldComponent,
                    newComponent));

                setComponentConnectivity(componentKey, newComponent.isConnected());
                newComponent.setComponentListener(new BaseComponent.ComponentListener() {
                    @Override
                    public void onConnectivityChange(boolean isConnected) {
                        setComponentConnectivity(componentKey, isConnected);
                    }
                });
            }
       }

        @Override
        public void onInitProcess(DJISDKInitEvent djisdkInitEvent, int i) {

        }

        @Override
        public void onDatabaseDownloadProgress(long current, long total) {
            int process = (int) (100 * current / total);
            if (process == lastProcess) {
                return;
            }
            lastProcess = process;
//            showProgress(process);
            if (process % 25 == 0){
                mDataManager.makeToast("DB load process : " + process);
            }else if (process == 0){
                mDataManager.makeToast("Flysafe DB load begin");
            }
        }
    };

    private void setComponentConnectivity(BaseProduct.ComponentKey key, boolean isConnected) {
        Timber.i("Changed component key: " + key + " [connected: " + isConnected + "]");
        switch (key) {
            case REMOTE_CONTROLLER:
                mProductState.setRemoteConnected(isConnected);
                publishState();
                break;
            case FLIGHT_CONTROLLER:
                mProductState.setFlightControllerConnected(isConnected);
                publishState();
                break;
        }
    }

    // Used to login to DJI account
    public void loginAccount(Context activityContext) {
        UserAccountManager.getInstance().logIntoDJIUserAccount(activityContext,
                new CommonCallbacks.CompletionCallbackWith<UserAccountState>() {
                    @Override
                    public void onSuccess(final UserAccountState userAccountState) {
                        Timber.i("DJI login Success");
                        mProductState.setLoggedIn(true);
                        publishState();
                    }

                    @Override
                    public void onFailure(DJIError error) {
                        Timber.e("DJI login Error:"
                                + error.getDescription());
                    }
                });
    }
    /**
     * This function is used to initiate the Flight controller which is used to control the aircraft
     * and read the sensors.
     * The Flight controller is a subcomponent of the Product (aircraft or handheld)
     */
    private void initFlightController() {
        mFlightController = ((Aircraft) getProductInstance()).getFlightController();

        if (mFlightController != null) {
            Timber.d("Registering state callback");
            mFlightController.setStateCallback(new FlightControllerState.Callback() {

            /**
             * This method is called 10 times every second, and is used to update the state of
             * the aircraft.
             * @param flightControllerState
             */
            @Override
            public void onUpdate(@NonNull FlightControllerState flightControllerState) {
                if (null != flightControllerState) {
                    EventBus.getDefault().post(new UAVState(flightControllerState));
                }
            }
        });
        }


    }


}