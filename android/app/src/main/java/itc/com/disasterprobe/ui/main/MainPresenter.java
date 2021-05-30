package itc.com.disasterprobe.ui.main;

import android.content.Context;

import com.google.android.gms.common.util.IOUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

import javax.inject.Inject;

import itc.com.disasterprobe.data.DataManager;
import itc.com.disasterprobe.data.drone.model.PhotoState;
import itc.com.disasterprobe.data.drone.model.PhotoTask;
import itc.com.disasterprobe.data.drone.model.PhotoTaskList;
import itc.com.disasterprobe.data.drone.model.ProbeMission;
import itc.com.disasterprobe.data.drone.model.ProductState;
import itc.com.disasterprobe.data.nsd.model.NetworkService;
import itc.com.disasterprobe.data.socket.model.SocketConnection;
import itc.com.disasterprobe.ui.base.BasePresenter;
import timber.log.Timber;

/**
 * Created by anne on 18-3-18.
 */

public class MainPresenter<V extends MainMvpView> extends BasePresenter<V>
        implements MainMvpPresenter<V> {

    @Inject
    public MainPresenter(DataManager dataManager) {
        super(dataManager);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onAttach(V mvpView) {
        super.onAttach(mvpView);
    }

    @Override
    public void onStartDiscoverClick() {
        getDataManager().startDiscovery();
    }

    @Override
    public void onLoginDJIAccountClick(Context activityContext) {
       getDataManager().loginDJIAccount(activityContext);
    }

    public void onLoadMissionClick() {
        getDataManager().loadMission();
    }

    @Override
    public void submitPCNetwork(String networkinfo) {
        Pattern p = Pattern.compile("^"
                + "(((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}" // Domain name
                + "|"
                + "localhost" // localhost
                + "|"
                + "(([0-9]{1,3}\\.){3})[0-9]{1,3})" // Ip
                + ":"
                + "[0-9]{1,5}$"); // Port

        if (p.matcher(networkinfo).matches()) {
                String[] arr = networkinfo.split(":");
                String host = arr[0];
                int port = Integer.parseInt(arr[1]);
                NetworkService networkService = new NetworkService(host, port);
                EventBus.getDefault().post(networkService);
        } else {
            getDataManager().makeToast("Incorrect hostname format");
        }
    }

    public void registerDJISDK() {
//        getDataManager().startDJISDKRegistration();
    }

    public void onUploadPictureClick(Context activityContext) {
        File file = null;
        try {
            file = File.createTempFile("testimage", ".jpg");
            file.deleteOnExit();
            FileOutputStream out = new FileOutputStream(file);
            InputStream inputStream = activityContext.getAssets().open("DJI_0415.JPG");
            IOUtils.copyStream(inputStream, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        PhotoTaskList photoTaskList = new PhotoTaskList();
        photoTaskList.add(new PhotoTask(file.getParentFile(), file.getName(), PhotoState.DOWNLOADED));
        EventBus.getDefault().post(photoTaskList);
    }

    @Override
    public void onUploadMissionClick() {
        getDataManager().uploadMission();
    }

    @Override
    public void onStartMissionClick() {
        getDataManager().startMission();
    }

    @Override
    public void onStopMissionClick(){
        getDataManager().stopMission();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onProductState(ProductState productState) {
        Timber.i("got State");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onProbeMission(ProbeMission probeMission) {
        getMvpView().displayMissionMap(probeMission);
        getMvpView().displayMissionState(probeMission);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPhotoTaskList(PhotoTaskList photoQue) {
        getMvpView().displayPhotoInfo(photoQue);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSocketConnection(SocketConnection socketConnection) {
        getMvpView().displayConnectionStatus(socketConnection.isConnected());
        if (socketConnection.isConnected()) {
            getDataManager().makeToast("Connected to pc client");
        }
    }



}
