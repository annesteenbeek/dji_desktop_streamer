package itc.com.disasterprobe.ui.main;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import dji.sdk.camera.VideoFeeder;
import dji.ux.panel.CameraSettingAdvancedPanel;
import dji.ux.panel.PreFlightCheckListPanel;
import dji.ux.widget.FPVWidget;
import dji.ux.widget.MapWidget;
import com.dji.mapkit.core.maps.DJIMap;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
//import com.dji.mapkit.models.DJIBitmapDescriptor;
//import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.dji.mapkit.core.models.DJILatLng;
//import com.google.android.gms.maps.model.LatLng;
import com.dji.mapkit.core.models.annotations.DJIMarker;
import com.dji.mapkit.core.models.annotations.DJIMarkerOptions;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dji.ux.widget.controls.CameraControlsWidget;
import itc.com.disasterprobe.BuildConfig;
import itc.com.disasterprobe.R;
import itc.com.disasterprobe.data.drone.model.MissionState;
import itc.com.disasterprobe.data.drone.model.PhotoState;
import itc.com.disasterprobe.data.drone.model.PhotoTaskList;
import itc.com.disasterprobe.data.drone.model.ProbeMission;
import itc.com.disasterprobe.data.drone.model.ProbeWaypoint;
import itc.com.disasterprobe.ui.base.BaseActivity;
import timber.log.Timber;

public class MainActivity extends BaseActivity implements MainMvpView {

    private MapWidget mapWidget;
    private DJIMap mMap = null;
    private MissionState currentState = null;
    private ProbeMission currentMission = null;
    private List<DJIMarker> markerList = new ArrayList<>();
    private int waypointCount = 0;


    private boolean isMapMini = true;

    private int height;
    private int width;
    private int margin;
    private int deviceWidth;
    private int deviceHeight;

    private static final String[] REQUIRED_PERMISSION_LIST = new String[] {
            Manifest.permission.VIBRATE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECORD_AUDIO
    };
    private static final int REQUEST_PERMISSION_CODE = 12345;
    private List<String> missingPermission = new ArrayList<>();

    @BindView(R.id.mission_state) TextView missionStateText;
    @BindView(R.id.download_counter) TextView downloadCounter;
    @BindView(R.id.upload_counter) TextView uploadCounter;
    @BindView(R.id.photo_counter) TextView photoCounter;

    @BindView(R.id.download_progress) ProgressBar downloadProgress;
    @BindView(R.id.photo_progress) ProgressBar photoProgress;
    @BindView(R.id.upload_progress) ProgressBar uploadProgress;

    @BindView(R.id.searching_connection) ProgressBar searchingConnection;
    @BindView(R.id.laptop_connection) ImageView laptopConnection;

    @BindView(R.id.root_view) ViewGroup parentView;
    @BindView(R.id.fpv_widget) FPVWidget fpvWidget;
//    @BindView(R.id.fpv_widget) VideoFeedView fpvWidget;
    @BindView(R.id.fpv_container) RelativeLayout primaryVideoView;


    @Inject
    MainMvpPresenter<MainMvpView> mPresenter;

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_main);
        getActivityComponent().inject(this);
        setUnBinder(ButterKnife.bind(this));
        mPresenter.onAttach(this);

        height = dip2px(this, 100);
        width = dip2px(this, 150);
        margin = dip2px(this, 30);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        final Display display = windowManager.getDefaultDisplay();
        Point outPoint = new Point();
        display.getRealSize(outPoint);
        deviceHeight = outPoint.y;
        deviceWidth = outPoint.x;

        // TODO recheck permissions at launch (look at UX example)
        checkAndRequestPermissions();

        mapWidget = findViewById(R.id.map_widget);
        mapWidget.initGoogleMap(new MapWidget.OnMapReadyListener() {
            @Override
            public void onMapReady(@NonNull DJIMap map) {
                mMap = map;
                map.setOnMapClickListener(new DJIMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(DJILatLng latLng) {
                        onViewClick(mapWidget);
                    }
                });
            }
        });
        mapWidget.onCreate(savedInstance);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
////                startLiveShow();
//                Timber.i("RegesteringLiveVideo");
//                if (VideoFeeder.getInstance() == null) {
//                    Timber.e("Videofeeder is null!");
//                }
//                fpvWidget.registerLiveVideo(VideoFeeder.getInstance().getPrimaryVideoFeed(), true);
//            }
//        }, 10000);

        if(BuildConfig.BUILD_TYPE == "debug") {
            findViewById(R.id.debug_actions).setVisibility(View.VISIBLE);
        }

    }

    /**
     * Checks if there is any missing permissions, and
     * requests runtime permission if needed.
     */
    private void checkAndRequestPermissions() {
        // Check for permissions
        for (String eachPermission : REQUIRED_PERMISSION_LIST) {
            if (ContextCompat.checkSelfPermission(this, eachPermission) != PackageManager.PERMISSION_GRANTED) {
                missingPermission.add(eachPermission);
            }
        }
        // Request for missing permissions
        if (missingPermission.isEmpty()) {
            mPresenter.registerDJISDK();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,
                    missingPermission.toArray(new String[missingPermission.size()]),
                    REQUEST_PERMISSION_CODE);
        }

    }

    /**
     * Result of runtime permission request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Check for granted permission and remove from missing list
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = grantResults.length - 1; i >= 0; i--) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    missingPermission.remove(permissions[i]);
                }
            }
        }
        // If there is enough permission, we will start the registration
        if (missingPermission.isEmpty()) {
            mPresenter.registerDJISDK();
        } else {
            makeToast("Missing permissions!!!");
        }
    }

    @Override
    public void displayConnectionStatus(boolean connected) {
        if (connected) {
            searchingConnection.setVisibility(View.GONE);
            laptopConnection.setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.searching_connection).setVisibility(View.VISIBLE);
            laptopConnection.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void displayPhotoInfo(PhotoTaskList photoList) {
        int nTotal = waypointCount;
        int nPhotos = photoList.size();
        int nDownloaded = photoList.getDownloadedCount();
        int nUploaded = photoList.filterList(PhotoState.UPLOADED).size();

        photoCounter.setText(nPhotos + "/" + nTotal);
        downloadCounter.setText(nDownloaded + "/" + nTotal);
        uploadCounter.setText(nUploaded + "/" + nTotal);

        photoProgress.setProgress(nPhotos);
        downloadProgress.setProgress(nDownloaded);
        uploadProgress.setProgress(nUploaded);
    }

    @Override
    public void displayMissionState(ProbeMission probeMission) {
       // Display mission state and progress.
        MissionState newState = probeMission.getState();
        if (newState != currentState || currentMission != probeMission) {
            // Display new state
            String text = "Mission state";
            switch (newState) {
                case LOADED:
                    waypointCount = probeMission.getTotalWaypoints();
                    String emptyCount = "0/"+waypointCount;
                    photoCounter.setText(emptyCount);
                    downloadCounter.setText(emptyCount);
                    uploadCounter.setText(emptyCount);

                    photoProgress.setMax(waypointCount);
                    downloadProgress.setMax(waypointCount);
                    uploadProgress.setMax(waypointCount);


                    text = "Ready for upload";
                    break;
                case RUNNING:
                    text = "Running";
                    break;
                case STOPPED:
                    text = "Stopped";
                    break;
                case FINISHED:
                    text = "Finished";
                    break;
                case UPLOADED:
                    text = "Mission Uploaded";
                    break;
                case PROTOTYPE:
                    text = "Ready to load mission";
                    break;
                case UPLOADING:
                    text = "Uploading mission";
                    break;
                case UPLOAD_FAILED:
                    text = "Upload failed";
                    break;
                case LOAD_FAILED:
                    text = "Unable to load";
                    if (probeMission.getErrorDescription() != "") {
                        makeToast("Unable to load: " + probeMission.getErrorDescription());
                    }
                    break;
            }
            missionStateText.setText(text);
            currentState = newState;
            currentMission = probeMission;
        }
    }

    @Override
    public void displayMissionMap(ProbeMission probeMission) {
        if (mMap != null) {
            if (probeMission.isDisplayed()) {
                // Map has already been drawn
                // TODO change mission icon (depending on mission state)
            } else {
                // New map, or map has not been drawn yet
                if (!markerList.isEmpty()) {
                    // remove old markers
                    for (DJIMarker marker : markerList) {
                        marker.setVisible(false);
                    }
                    markerList.clear();
                }
                for (ProbeWaypoint point : probeMission.getWaypointList()) {
                    DJILatLng loc = new DJILatLng(point.getLatitude(), point.getLongitude());
                    DJIMarkerOptions options = new DJIMarkerOptions()
                            .position(loc);
                    markerList.add(mMap.addMarker(options));
                }
                probeMission.setDisplayed(true);
            }
        }
    }

    public void showNetworkDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter pc IP and port");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setHint(R.string.iphint);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String networkInfo = input.getText().toString();
                mPresenter.submitPCNetwork(networkInfo);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapWidget.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapWidget.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapWidget.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapWidget.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapWidget.onLowMemory();
    }

    @OnClick(R.id.start_discover)
    void onStartDiscoverClick(View v) {
        this.showNetworkDialog();
    }

    @OnClick(R.id.login_dji)
    void onLoginDJIClick(View v) {
        mPresenter.onLoginDJIAccountClick(this);
    }


    @OnClick(R.id.upload_mission)
    void onUploadMissionClick(View v) {
        mPresenter.onUploadMissionClick();
    }


    @OnClick(R.id.start_mission)
    void onStartMissionClick(View v) {
        mPresenter.onStartMissionClick();
    }

    @OnClick(R.id.stop_mission)
    void onStopMissionClick(View v) {
        mPresenter.onStopMissionClick();
    }

    // DEBUGGING FUNCTIONS
//    @OnClick(R.id.load_mission)
//    void onLoadMissionClick(View v) {
//        mPresenter.onLoadMissionClick();
//    }

    @OnClick(R.id.upload_picture)
    void onUploadPictureClick(View v) {
        mPresenter.onUploadPictureClick(this);
    }

    @OnClick(R.id.camera_settings)
    void onCameraSettingsClick(View v) {
        View adv = findViewById(R.id.cameraSettingsAdvancedPanel);
        View exp = findViewById(R.id.cameraSettingsExposurePanel);

        if (adv.getVisibility() == View.VISIBLE) {
            adv.setVisibility(View.INVISIBLE);
            exp.setVisibility(View.INVISIBLE);
        } else {
            adv.setVisibility(View.VISIBLE);
            exp.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.fpv_widget)
    void onViewClick(View view) {
        Timber.i("View was clicked: " + view.toString());
        Timber.i("View index: " + ((ViewGroup) view.getParent()).indexOfChild(view));
        if (view == fpvWidget && !isMapMini) {
            resizeFPVWidget(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, 0, 0);
            ResizeAnimation mapViewAnimation = new ResizeAnimation(mapWidget, deviceWidth, deviceHeight, width, height, margin);
            mapWidget.startAnimation(mapViewAnimation);
            isMapMini = true;
        } else if (view == mapWidget && isMapMini) {
            resizeFPVWidget(width, height, margin, 2);
            ResizeAnimation mapViewAnimation = new ResizeAnimation(mapWidget, width, height, deviceWidth, deviceHeight, 0);
            mapWidget.startAnimation(mapViewAnimation);
            isMapMini = false;
        }
    }

    private void resizeFPVWidget(int width, int height, int margin, int fpvInsertPosition) {
        Timber.i("Resizing FPVWidget to w: %d, h: %d, margin: %d, index: %d", width, height, margin, fpvInsertPosition);
        RelativeLayout.LayoutParams fpvParams = (RelativeLayout.LayoutParams) primaryVideoView.getLayoutParams();
        fpvParams.height = height;
        fpvParams.width = width;
        fpvParams.rightMargin = margin;
        fpvParams.topMargin = margin;

        if (isMapMini) {
            fpvParams.addRule(RelativeLayout.CENTER_IN_PARENT, 0);
            fpvParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            fpvParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        } else {
            fpvParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            fpvParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            fpvParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        }
        primaryVideoView.setLayoutParams(fpvParams);

        parentView.removeView(primaryVideoView);
        parentView.addView(primaryVideoView, fpvInsertPosition);
    }


    private int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private class ResizeAnimation extends Animation {

        private View mView;
        private int mToHeight;
        private int mFromHeight;

        private int mToWidth;
        private int mFromWidth;
        private int mMargin;

        private ResizeAnimation(View v, int fromWidth, int fromHeight, int toWidth, int toHeight, int margin) {
            Timber.i("Map resize: w: %d->%d, h: %d->%d", fromWidth, toWidth, fromHeight, toHeight);
            mToHeight = toHeight;
            mToWidth = toWidth;
            mFromHeight = fromHeight;
            mFromWidth = fromWidth;
            mView = v;
            mMargin = margin;
            setDuration(300);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            float height = (mToHeight - mFromHeight) * interpolatedTime + mFromHeight;
            float width = (mToWidth - mFromWidth) * interpolatedTime + mFromWidth;
            RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) mView.getLayoutParams();
            p.height = (int) height;
            p.width = (int) width;
            p.rightMargin = mMargin;
            p.topMargin = mMargin;
            mView.requestLayout();
        }
    }

}