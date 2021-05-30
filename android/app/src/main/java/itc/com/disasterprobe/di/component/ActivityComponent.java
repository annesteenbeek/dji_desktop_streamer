package itc.com.disasterprobe.di.component;

import dagger.Component;
import itc.com.disasterprobe.di.module.ActivityModule;
import itc.com.disasterprobe.di.PerActivity;
import itc.com.disasterprobe.ui.main.MainActivity;

/**
 * Created by anne on 18-3-18.
 */

@PerActivity
@Component(dependencies = ApplicationComponent.class, modules = ActivityModule.class)
public interface ActivityComponent {

    void inject(MainActivity activity);

}
