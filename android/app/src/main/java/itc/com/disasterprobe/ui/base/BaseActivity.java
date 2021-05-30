package itc.com.disasterprobe.ui.base;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import butterknife.Unbinder;
import itc.com.disasterprobe.DisasterProbeApplication;
import itc.com.disasterprobe.di.component.ActivityComponent;
import itc.com.disasterprobe.di.component.DaggerActivityComponent;
import itc.com.disasterprobe.di.module.ActivityModule;

/**
 * Created by anne on 18-3-18.
 */

public abstract class BaseActivity extends FragmentActivity implements MvpView {

    private ActivityComponent mActivityComponent;

    private Unbinder mUnbinder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityComponent = DaggerActivityComponent.builder()
                .activityModule(new ActivityModule(this))
                .applicationComponent(((DisasterProbeApplication) getApplication()).getComponent())
                .build();
    }

    @Override
    protected void onDestroy() {
        if (mUnbinder != null) {
            mUnbinder.unbind();
        }
        super.onDestroy();
    }

    @Override
    public ActivityComponent getActivityComponent() {
        return mActivityComponent;
    }

    @Override
    public void setUnBinder(Unbinder unBinder) {
        mUnbinder = unBinder;
    }

    @Override
    public void makeToast(String text){
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

}
