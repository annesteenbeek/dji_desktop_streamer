package itc.com.disasterprobe.di.module;

import itc.com.disasterprobe.di.ActivityContext;

import android.content.Context;

import androidx.fragment.app.FragmentActivity;

import dagger.Module;
import dagger.Provides;
import itc.com.disasterprobe.di.PerActivity;
import itc.com.disasterprobe.ui.main.MainMvpPresenter;
import itc.com.disasterprobe.ui.main.MainMvpView;
import itc.com.disasterprobe.ui.main.MainPresenter;

/**
 * Created by anne on 18-3-18.
 */

@Module
public class ActivityModule {

    private FragmentActivity mActivity;

    public ActivityModule(FragmentActivity activity) {
        this.mActivity = activity;
    }

    @Provides
    @ActivityContext
    Context provideContext() {
        return mActivity;
    }

    @Provides
    FragmentActivity provideActivity() {
        return mActivity;
    }

    @Provides
    @PerActivity
    MainMvpPresenter<MainMvpView> provideMainPresenter(
            MainPresenter<MainMvpView> presenter) {
        return presenter;
    }
}
