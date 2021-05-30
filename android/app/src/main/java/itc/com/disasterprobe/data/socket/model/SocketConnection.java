package itc.com.disasterprobe.data.socket.model;

/**
 * Created by anne on 10-5-18.
 */

public class SocketConnection {
    private boolean isConnected;

    public SocketConnection(boolean isConnected) {
        this.isConnected = isConnected;
    }

    public boolean isConnected() {
        return isConnected;
    }
}
