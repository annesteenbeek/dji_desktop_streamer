package itc.com.disasterprobe.ui.base;

/**
 * Created by anne on 18-3-18.
 */

public interface MvpPresenter<V extends MvpView> {

    void onAttach(V mvpView);

    void onDetach();

}
