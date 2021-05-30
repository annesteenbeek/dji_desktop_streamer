package itc.com.disasterprobe;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.android.gms.common.util.IOUtils;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import io.socket.client.Socket;
import itc.com.disasterprobe.data.DataManager;
import itc.com.disasterprobe.data.drone.ConnectionHelper;
import itc.com.disasterprobe.data.drone.MissionHelper;
import itc.com.disasterprobe.data.drone.model.PhotoState;
import itc.com.disasterprobe.data.drone.model.PhotoTask;
import itc.com.disasterprobe.data.nsd.model.NetworkService;
import itc.com.disasterprobe.data.socket.SocketHelper;
import timber.log.Timber;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4ClassRunner.class)
public class ExampleInstrumentedTest {
    private String serverIP = "192.168.2.9";
    private Socket socket;
    private NetworkService service;
    private int serverPort = 3004;
    private Context mAppContext;
    private MissionHelper mMissionHelper;
    private ConnectionHelper mConnectionHelper;
    private SocketHelper mSocketHelper;
    private String imageName = "DJI_0415.JPG";

    @Mock
    DataManager mMockDataManager;

        public class TestTask extends PhotoTask {

        public TestTask(PhotoState state) {
            super(new File("/"), "TestImage.JPG", state);
        }

        @Override
        public String getEncodedImage() {
            String enc_img = "nonemptystring";
            try {
                enc_img = encodeImage(imageName);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return enc_img;
        }
    }


    @Before
    public void setUp() {
   }

    public void setupSocket() {
        mAppContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        mSocketHelper = new SocketHelper(mAppContext, mMockDataManager);

        service = new NetworkService(serverIP, serverPort);
        mSocketHelper.startSocket(service);

        socket = mSocketHelper.getSocket();
        while (!socket.connected()) {
            Timber.i("waiting");
        }
    }

    @Test
    public void encodeImage() throws IOException {
        String img_enc = encodeImage(imageName);
        assertTrue(img_enc instanceof String);
    }

    @Test
    public void postImage() throws IOException {
        Context testContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        File file = File.createTempFile("testimage", ".jpg");
        file.deleteOnExit();
        FileOutputStream out = new FileOutputStream(file);
        InputStream inputStream = testContext.getAssets().open(imageName);
        IOUtils.copyStream(inputStream, out);

        Ion.with(testContext)
                .load("http://192.168.2.9:3005/testimg")
                .setMultipartParameter("Goop", "noop")
                .setMultipartFile("image", "image/jpeg", file)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject> () {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        Timber.i("completed!!!!!!");
                    }
                });


//        Future<String> res = Ion.with(testContext)
//                .load("http://192.168.2.9:3005/testpost")
//                .setBodyParameter("goop", "noop")
//                .asString();

        assertTrue(true);
    }

    @Test
    public void getSocket() {
        assertTrue(socket.connected());
    }

    @Test
    public void uploadImage() {
        setupSocket();
        TestTask task = new TestTask(PhotoState.DOWNLOADED);

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
    public void useAppContext() throws Exception {
        // Context of the app under test.
        assertEquals("itc.com.disasterprobe[.test]", mAppContext.getPackageName());
    }

    static String readFile(String filename) throws IOException {
        Context testContext = InstrumentationRegistry.getInstrumentation().getContext();
        AssetManager assetManager = testContext.getAssets();
        StringBuilder buf = new StringBuilder();
        InputStream json = assetManager.open(filename);
        BufferedReader in =
                new BufferedReader(new InputStreamReader(json, "UTF-8"));
        String str;

        while ((str=in.readLine()) != null) {
            buf.append(str);
        }

        in.close();
        return buf.toString();
    }

    private String encodeImage(String filename) throws IOException {
        Context testContext = InstrumentationRegistry.getInstrumentation().getContext();
        AssetManager assetManager = testContext.getAssets();
        InputStream fis = assetManager.open(filename);
        Bitmap bm = BitmapFactory.decodeStream(fis);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] b = baos.toByteArray();
        String encImage = Base64.encodeToString(b, Base64.DEFAULT);
        //Base64.de
        return encImage;
    }
}
