package itc.com.disasterprobe;

import android.content.Context;
import android.media.ExifInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import io.socket.client.Socket;
import itc.com.disasterprobe.data.DataManager;
import itc.com.disasterprobe.data.drone.ConnectionHelper;
import itc.com.disasterprobe.data.drone.MissionHelper;
import itc.com.disasterprobe.data.drone.model.MissionState;
import itc.com.disasterprobe.data.drone.model.ProbeMission;
import itc.com.disasterprobe.data.nsd.model.NetworkService;
import itc.com.disasterprobe.data.socket.SocketHelper;
import timber.log.Timber;

import static org.junit.Assert.*;

/**
 * Created by anne on 23-3-18.
 */

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class MissionTests  {
    private String serverIP = "192.168.2.9";
    private int serverPort = 3004;
    private MissionHelper mMissionHelper;
    private ConnectionHelper mConnectionHelper;
    private SocketHelper mSocketHelper;
    private String imageName = "DJI_0415.JPG";
//    private String imageName = "testimg.jpg";
    private String rootDir = System.getProperty("user.dir");
    private String dirLocation = "/src/test/java/itc/com/disasterprobe/assets/";
//    private String waypointCoordinatesJson = readFile(dirLocation + "testCoordinates.shorter.json", StandardCharsets.UTF_8);
    private String waypointCoordinatesJson = "";
    private Socket socket;

    @Mock
    DataManager mMockDataManager;

    @Mock
    Context mMockApplicationContext;


    @Before
    public void setUp() {
//        mMissionHelper = new MissionHelper(mMockApplicationContext, mMockDataManager);
//        mConnectionHelper = new ConnectionHelper(mMockApplicationContext, mMockDataManager);
        mSocketHelper = new SocketHelper(mMockApplicationContext, mMockDataManager);

        NetworkService service = new NetworkService(serverIP, serverPort);
        mSocketHelper.startSocket(service);

        socket = mSocketHelper.getSocket();
        while (!socket.connected()) {
            Timber.i("waiting");
        }
    }

    @Test
    public void convertsMisison() throws JSONException {
        JSONObject jsonMission = new JSONObject(waypointCoordinatesJson);
        JSONArray jsonArray = jsonMission.getJSONArray("mission");

        ProbeMission testMission = new ProbeMission(jsonArray, MissionState.PROTOTYPE);
//        WaypointMission builtMission = mMissionHelper.buildMission(testMission);
        assertEquals(testMission.getTotalWaypoints(), 14);
    }

    @Test
    public void readMetadata() {
        try {
            ExifInterface exifInterface = new ExifInterface(imageName);
            // Now you can extract any Exif tag you want
            // Assuming the image is a JPEG or supported raw format
        } catch (IOException e) {
            // Handle any errors
        } finally {

        }
    }
    @Test
    public void connectSocket() {
        while (!socket.connected()){
            Timber.i("waiting");
        }
        assertTrue(socket.connected());
    }

}