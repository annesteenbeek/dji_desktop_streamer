package itc.com.disasterprobe;

import android.content.Context;
import android.provider.ContactsContract;

import com.koushikdutta.ion.Ion;

//import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import androidx.test.core.app.ApplicationProvider;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import io.socket.client.Socket;
import itc.com.disasterprobe.data.DataManager;
import itc.com.disasterprobe.data.drone.model.PhotoState;
import itc.com.disasterprobe.data.drone.model.PhotoTask;
import itc.com.disasterprobe.data.drone.model.PhotoTaskList;
import itc.com.disasterprobe.data.nsd.model.NetworkService;
import itc.com.disasterprobe.data.socket.SocketHelper;
import timber.log.Timber;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Created by anne on 23-3-18.
 */

@RunWith(MockitoJUnitRunner.class)
public class PhotoTests {
    private String serverIP = "192.168.2.9";
    private int serverPort = 3004;
    private SocketHelper mSocketHelper;
    private String imageName = "DJI_0415.JPG";
    private String rootDir = System.getProperty("user.dir");
    private String dirLocation = "/src/test/java/itc/com/disasterprobe/assets/";
    private Socket socket;
    private NetworkService service;
    private Context testContext = ApplicationProvider.getApplicationContext();

    @Mock
    DataManager mMockDataManager;

    @Mock
    Context mMockApplicationContext;

    public class TestTask extends PhotoTask {

        public TestTask(File directory, String filename, PhotoState state) {
            super(directory, filename, state);
        }

        @Override
        public String getEncodedImage() {
            File imagefile = getFullPath();
            int size = (int) imagefile.length();
            byte[] bytes = new byte[size];
            try {
                BufferedInputStream buf = new BufferedInputStream(new FileInputStream(imagefile));
                buf.read(bytes, 0, bytes.length);
                buf.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
//            String encoded = Base64.encodeBase64String(bytes);
            String encoded = "nonemptystring";
            return encoded;
        }
    }

    @Before
    public void setUp() {
        mSocketHelper = new SocketHelper(mMockApplicationContext, mMockDataManager);

        service = new NetworkService(serverIP, serverPort);
        mSocketHelper.startSocket(service);

        socket = mSocketHelper.getSocket();
        while (!socket.connected()) {
            Timber.i("waiting");
        }
    }

    @Test
    public void localPostImage() {
         Ion.with(testContext)
                .load("localhost")
                .setMultipartParameter("goop", "noop")
                .setMultipartFile("archive", "application/zip", new File(rootDir+dirLocation+imageName))
                .asJsonObject();
//                .setCallback(...)
        assertTrue(true);
    }

    @Test
    public void connectSocket() {
        while (!socket.connected()){
            Timber.i("waiting");
        }
        assertTrue(socket.connected());
    }

    @Test
    public void uploadImage() {
        TestTask task = new TestTask(new File(rootDir + dirLocation), imageName, PhotoState.DOWNLOADED);

        try {
            mSocketHelper.sendPhoto(task);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while (task.getState() != PhotoState.UPLOADED) {
            Timber.i("waiting");
        }
        assertEquals(task.getState(), PhotoState.UPLOADED);
    }

    @Test
    public void uploadTaskList() {
        PhotoTaskList photoTaskList = new PhotoTaskList();
        PhotoTask task1 = new TestTask(new File(rootDir + dirLocation), imageName, PhotoState.DOWNLOADED);
        PhotoTask task2 = new TestTask(new File(rootDir + dirLocation), imageName, PhotoState.DOWNLOADED);
        PhotoTask task3 = new TestTask(new File(rootDir + dirLocation), imageName, PhotoState.DOWNLOADED);

        photoTaskList.add(task1);
        photoTaskList.add(task2);

        mSocketHelper.onPhotoTaskList(photoTaskList);
        assertEquals(task1.getState(), PhotoState.UPLOADING);
        assertEquals(task2.getState(), PhotoState.UPLOADING);

        photoTaskList.add(task3);
        mSocketHelper.onPhotoTaskList(photoTaskList);
        assertEquals(task3.getState(), PhotoState.UPLOADING);
        while (task3.getState() != PhotoState.UPLOADED) {
            Timber.i("waiting");
        }
        assertEquals(task3.getState(), PhotoState.UPLOADED);
    }

    @Test
    public void disconnectedTaskList() {
        PhotoTaskList photoTaskList = new PhotoTaskList();
        PhotoTask task1 = new TestTask(new File(rootDir + dirLocation), imageName, PhotoState.DOWNLOADED);
        PhotoTask task2 = new TestTask(new File(rootDir + dirLocation), imageName, PhotoState.DOWNLOADED);
        PhotoTask task3 = new TestTask(new File(rootDir + dirLocation), imageName, PhotoState.DOWNLOADED);

        photoTaskList.add(task1);
        photoTaskList.add(task2);

        mSocketHelper.stopSocket(); // manually disconnect
        mSocketHelper.onPhotoTaskList(photoTaskList);
        assertEquals(task1.getState(), PhotoState.UPLOADING);
        assertEquals(task2.getState(), PhotoState.UPLOADING);
        // connect with files in queue
        mSocketHelper.startSocket(service);
        while(task2.getState() != PhotoState.UPLOADED) {
            Timber.i("waiting");
        }

        photoTaskList.add(task3);
        mSocketHelper.onPhotoTaskList(photoTaskList);
        assertEquals(task3.getState(), PhotoState.UPLOADING);
        while (task3.getState() != PhotoState.UPLOADED) {
            Timber.i("waiting");
        }
        assertEquals(task3.getState(), PhotoState.UPLOADED);
    }


}