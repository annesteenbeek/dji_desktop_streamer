package itc.com.disasterprobe.data.socket;

import android.content.Context;
import android.util.Base64;


import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.inject.Singleton;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.EngineIOException;
import itc.com.disasterprobe.data.DataManager;
import itc.com.disasterprobe.data.drone.model.MissionState;
import itc.com.disasterprobe.data.drone.model.PhotoEvent;
import itc.com.disasterprobe.data.drone.model.PhotoState;
import itc.com.disasterprobe.data.drone.model.PhotoTask;
import itc.com.disasterprobe.data.drone.model.PhotoTaskList;
import itc.com.disasterprobe.data.drone.model.ProbeMission;
import itc.com.disasterprobe.data.drone.model.ProductState;
import itc.com.disasterprobe.data.drone.model.UAVState;
import itc.com.disasterprobe.data.nsd.model.NetworkService;
import itc.com.disasterprobe.data.socket.model.SocketConnection;
import timber.log.Timber;

@Singleton
public class SocketHelper {
    private final DataManager mDataManager;
    private final Context mApplicationContext;
    private Socket mSocket = null;
    private ProductState cachedProductState = null;
    private ProbeMission cachedProbeMission = null;
    private AtomicBoolean isCreated = new AtomicBoolean(false);
    private ExecutorService photoThread;
    private final Object connectionLock = new Object();

    private float UAVState_freq = 2.0f;
    private long last_state_pub = 0;

    private NetworkService currentNetworkService;

    public SocketHelper(Context applicationContext, DataManager dataManager) {
        mDataManager = dataManager;
        mApplicationContext = applicationContext;

        photoThread = Executors.newFixedThreadPool(1);

        EventBus.getDefault().register(this);
    }

    @Subscribe
    public void onNetworkService(NetworkService networkService) {
        if (isCreated.compareAndSet(false, true)) {
            currentNetworkService = networkService;
            startSocket(networkService);
        }
    }

    public void startSocket(NetworkService service){
        String address = service.getString();
        Timber.i("Attempting socket connection with: " + address);
        try {
            mSocket = IO.socket("http://" + address);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_RECONNECT, onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectionError);
        mSocket.on("mission_csv", onMissionCSV);
        mSocket.on("new_mission", onNewMission);
        mSocket.open();
    }

    public void stopSocket() {
        mSocket.close();
        Timber.i("Stopped socket connection.");
    }

    private synchronized boolean emit(String event, Object... args) {
        if(mSocket != null && mSocket.connected()) {
            mSocket.emit(event, args);
            return true;
        } else {
            return false;
        }
    }

    public Socket getSocket() {
        return mSocket;
    }

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Timber.i("Disconnected");
            EventBus.getDefault().postSticky(new SocketConnection(false));
        }
    };

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (cachedProductState != null) {
                onProductState(cachedProductState);
            }
            if (cachedProbeMission != null) {
                onProbeMission(cachedProbeMission);
            }
            synchronized (connectionLock) {
                connectionLock.notify();
            }
            EventBus.getDefault().postSticky(new SocketConnection(true));
        }
    };

    private  Emitter.Listener onConnectionError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Timber.i("Connection error");
            if (args.length> 0 && args[0] instanceof EngineIOException){
                ((EngineIOException)args[0]).printStackTrace();
            }
        }
    };

    // Missions defined in previous CSV filetype (still sent in json format)
    private Emitter.Listener onMissionCSV = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
//            ProbeMission probeMission = new ProbeMission((JSONArray) args[0], MissionState.PROTOTYPE);
//            EventBus.getDefault().post(probeMission);
        }
    };

    private Emitter.Listener onNewMission = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Timber.i("Received new mission JSON");
            ProbeMission probeMission = new ProbeMission((JSONObject) args[0]);

            JSONObject resp = new JSONObject();
            try {
                if (probeMission.getState() == MissionState.PROTOTYPE) {
                    EventBus.getDefault().post(probeMission);
                    resp.put("success", true);
                } else {
                    resp.put("success", false);
                    resp.put("error", probeMission.getErrorDescription());
                }
            } catch(JSONException e) {
                e.printStackTrace();
            }

            Ack ack = (Ack) args[args.length - 1];
            ack.call(resp);
        }
    };

    @Subscribe(sticky = true)
    public void onProductState(ProductState productState) {
        cachedProductState = productState;
        emit("productState", productState.toJson());
    }

    @Subscribe
    public void onPhotoEvent(PhotoEvent event) {
        emit("photoEvent", event.toJson());
    }

    @Subscribe
    public void onUAVState(UAVState uavState) throws JSONException {
        long now = System.currentTimeMillis();
        if (now - last_state_pub > 1./(UAVState_freq*1000)) {
            JSONObject state = new JSONObject();
            state.put("timestamp", uavState.getTimestamp());
            state.put("aircraftLocation", uavState.getLocationJson());
            state.put("attitude", uavState.getAttitudeJson());
            emit("UAVState", state);

            last_state_pub = now;
        }
    }

    @Subscribe
    public void onPhotoTaskList(PhotoTaskList photoTaskList) {
        ArrayList<PhotoTask> downloaded = photoTaskList.filterList(PhotoState.DOWNLOADED);
           for (PhotoTask task : downloaded) {
               task.setState(PhotoState.UPLOADING);
               Timber.i("Adding image upload task for: " + task.getFilename());
               photoThread.submit(new Runnable() {
                   @Override
                   public void run() {
                       try {
                           Timber.i("Sending " + task.getFilename());
//                           sendPhoto(task);
                           postPhoto(task);
                           EventBus.getDefault().post(photoTaskList);
                       } catch (InterruptedException e) {
                           e.printStackTrace();
                       }
                   }
               });

           }
    }

    @Subscribe
    public void onProbeMission(ProbeMission probeMission) {
        cachedProbeMission = probeMission;
        emit("probeMission", probeMission.toJson());
    }

    public void postPhoto(PhotoTask task) throws InterruptedException {
        // check if address is already set
        // check retry timer

        String addr = "http://" + currentNetworkService.getAddress() + ":3005/mission_image";
        String missionName = "default";
        if (cachedProbeMission != null) {
            missionName = cachedProbeMission.getMissionName();
        }

        Ion.with(mApplicationContext)
                .load(addr)
                .setMultipartParameter("missionName", missionName)
                .setMultipartFile("image", "image/jpeg", task.getFullPath())
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject> () {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                       Timber.i("completed!!!!!!");
                       task.setState(PhotoState.UPLOADED);
                       // TODO handle exception
                    }
                });
    }

    public boolean sendPhoto(PhotoTask task) throws InterruptedException {
        String encodedImage = task.getEncodedImage();
        int chunksize = 102400;
//        int chunksize = 1024000000;
        Iterable<String> image_chunks = Splitter.fixedLength(chunksize).split(encodedImage);

        int nChunks = Iterables.size(image_chunks);
        int n = 1;
        final Object receivedLock = new Object();
        for(String chunk : image_chunks) {
            JSONObject sendData = new JSONObject();
            try {
                sendData.put("image_chunk", chunk);
                sendData.put("filename", task.getFilename());
                sendData.put("chunk_index", n++);
                sendData.put("total_chunks", nChunks);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            synchronized (connectionLock) {
                while (!mSocket.connected()) {
                    Timber.d("Attempting chunk emit while disconnected, waiting...");
                    connectionLock.wait();
                }
            }

            Timber.d("Sending chunk " + (n-1) + "/"+nChunks);
            emit("photo_chunk", sendData, new Ack() {

                @Override
                public void call(Object... args) {
                    boolean success = (boolean) args[0];
                    Timber.d("Chunk received");
                    synchronized (receivedLock) {
                        if (!success) {
                            task.setState(PhotoState.UPLOAD_FAILED);
                        }
                        receivedLock.notify();
                    }
                }
            });

            synchronized (receivedLock) {
                receivedLock.wait(10);
            }

            if (task.getState() == PhotoState.UPLOAD_FAILED) {
                return false;
            }
        }


        task.setState(PhotoState.UPLOADED);
        return true;
    }


}
