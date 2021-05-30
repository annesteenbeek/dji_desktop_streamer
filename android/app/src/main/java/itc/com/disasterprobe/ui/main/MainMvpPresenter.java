package itc.com.disasterprobe.ui.main;

import android.content.Context;

import itc.com.disasterprobe.di.PerActivity;
import itc.com.disasterprobe.ui.base.MvpPresenter;

/**
 * Created by anne on 18-3-18.
 */

@PerActivity
public interface MainMvpPresenter<V extends MainMvpView> extends MvpPresenter<V> {

    void onStartDiscoverClick();

    void onLoginDJIAccountClick(Context activityContext);

    void onUploadMissionClick();

    void onLoadMissionClick();

    void onStartMissionClick();

    void onStopMissionClick();

    void onUploadPictureClick(Context activityContext);

    void registerDJISDK();

    void submitPCNetwork(String networkinfo);
}
