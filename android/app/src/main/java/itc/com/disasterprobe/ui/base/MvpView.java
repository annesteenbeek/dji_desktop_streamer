package itc.com.disasterprobe.ui.base;

import butterknife.Unbinder;
import itc.com.disasterprobe.di.component.ActivityComponent;

/**
 * Created by anne on 18-3-18.
 */

public interface MvpView {

    void setUnBinder(Unbinder unBinder);

    ActivityComponent getActivityComponent();

    void makeToast(String text);

}
