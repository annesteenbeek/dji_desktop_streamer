package itc.com.disasterprobe;

import android.app.Application;
import android.content.Context;

import com.secneo.sdk.Helper;

import javax.inject.Inject;

import itc.com.disasterprobe.data.DataManager;
import itc.com.disasterprobe.di.component.ApplicationComponent;
import itc.com.disasterprobe.di.component.DaggerApplicationComponent;
import itc.com.disasterprobe.di.module.ApplicationModule;
import itc.com.disasterprobe.utils.AppLogger;

/*
 * * Created by anne on 18-3-18
 */

public class DisasterProbeApplication extends Application {

    @Inject
    DataManager mDataManager;

    private ApplicationComponent mApplicationComponent;

    @Override
    protected void attachBaseContext(Context paramContext) {
        super.attachBaseContext(paramContext);
        Helper.install(DisasterProbeApplication.this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mApplicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this)).build();

        mApplicationComponent.inject(this);

        AppLogger.init();
    }

    public ApplicationComponent getComponent() {
        return mApplicationComponent;
    }

    // Needed to replace the component with a test specific one
    public void setComponent(ApplicationComponent applicationComponent) {
        mApplicationComponent = applicationComponent;
    }

}

