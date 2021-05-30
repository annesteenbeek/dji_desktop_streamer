package itc.com.disasterprobe.di.module;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import itc.com.disasterprobe.data.nsd.NsdHelper;
import itc.com.disasterprobe.di.ApplicationContext;

/**
 * Created by anne on 18-3-18.
 */

@Module
public class ApplicationModule {

    private final Application mApplication;

    public ApplicationModule(Application application) {
        mApplication = application;
    }

    @Provides
    @ApplicationContext
    Context provideContext() {
        return mApplication;
    }

    @Provides
    Application provideApplication() {
        return mApplication;
    }

}
