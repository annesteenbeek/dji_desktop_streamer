package itc.com.disasterprobe.di.component;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Component;
import itc.com.disasterprobe.DisasterProbeApplication;
import itc.com.disasterprobe.data.DataManager;
import itc.com.disasterprobe.data.nsd.NsdHelper;
import itc.com.disasterprobe.di.module.ApplicationModule;
import itc.com.disasterprobe.di.ApplicationContext;

/**
 * Created by anne on 18-3-18.
 */

@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {

    void inject(DisasterProbeApplication app);

    @ApplicationContext
    Context context();

    Application application();

    DataManager getDataManager();

//    NsdHelper getNsdHelper();
}
