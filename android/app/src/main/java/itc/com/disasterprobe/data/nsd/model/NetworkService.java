package itc.com.disasterprobe.data.nsd.model;

/**
 * Created by anne on 20-3-18.
 */

public class NetworkService {

    private String address;
    private int port;

    public NetworkService(String address, int port) {
        this.address = address;
        this.port = port;
    }

    public String getString() {
       return address + ":" + Integer.toString(port);
    };

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

}
