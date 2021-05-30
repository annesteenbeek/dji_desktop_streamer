package itc.com.disasterprobe.data.nsd;


import android.content.Context;

import com.github.druk.dnssd.BrowseListener;
import com.github.druk.dnssd.DNSSD;
import com.github.druk.dnssd.DNSSDBindable;
import com.github.druk.dnssd.DNSSDException;
import com.github.druk.dnssd.DNSSDService;
import com.github.druk.dnssd.ResolveListener;

import org.greenrobot.eventbus.EventBus;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import itc.com.disasterprobe.data.DataManager;
import itc.com.disasterprobe.data.nsd.model.NetworkService;
import itc.com.disasterprobe.di.ApplicationContext;
import timber.log.Timber;


@Singleton
public class NsdHelper {

    private final DNSSD mDnssd;
    private DNSSDService browseService;
    private final Context mApplicationContext;
    private final DataManager mDataManager;

    public NsdHelper(Context applicationContext, DataManager dataManager) {
        mDnssd = new DNSSDBindable(applicationContext);
        mApplicationContext = applicationContext;
        mDataManager = dataManager;
        discoverServices();
    }

    public void discoverServices() {
        try {
            if (browseService == null) {
                Timber.i("Starting service discovery.");
                browseService = mDnssd.browse("_http._tcp", new BrowseListener() {

                    @Override
                    public void serviceFound(DNSSDService browser, int flags, int ifIndex,
                                             final String serviceName, String regType, String domain) {
                        if(serviceName.equals("disasterprobe")) {
                            Timber.i("Found " + serviceName + " resolving...");
                            startResolve(flags, ifIndex, serviceName, regType, domain);
                        }
                    }

                    @Override
                    public void serviceLost(DNSSDService browser, int flags, int ifIndex,
                                            String serviceName, String regType, String domain) {
                        Timber.i("Lost " + serviceName);
                    }

                    @Override
                    public void operationFailed(DNSSDService service, int errorCode) {
                        Timber.e( "error: " + errorCode);
                    }
                });
            }
        } catch (DNSSDException e) {
            Timber.e("error", e);
        }
    }

    private void startResolve(int flags, int ifIndex, final String serviceName, final String regType, final String domain) {
        try {
            mDnssd.resolve(flags, ifIndex, serviceName, regType, domain, new ResolveListener() {
                @Override
                public void serviceResolved(DNSSDService resolver, int flags, int ifIndex, String fullName, String hostName, int port, Map<String, String> txtRecord) {
                    hostName = hostName.substring(0, hostName.length() - 1); // remove dot
                    Timber.d( "Resolved " + hostName);
                    String address = hostName + ":" + Integer.toString(port);
                    EventBus.getDefault().post(new NetworkService(hostName, port));
                }

                @Override
                public void operationFailed(DNSSDService service, int errorCode) {

                }
            });
        } catch (DNSSDException e) {
            e.printStackTrace();
        }
    }
}
