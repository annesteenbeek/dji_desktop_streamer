package itc.com.disasterprobe.data.drone.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class PhotoEvent {

    private long timestamp;
    private String type = "";

    public PhotoEvent() {
        timestamp = new Date().getTime();
    }

    public void setType(String type) {
       this.type = type;
    }

    public JSONObject toJson() {
       JSONObject result = new JSONObject();

       try {
           result.put("timestamp", timestamp);
           if (type != "") {
               result.put("photoType", type);
           }
       } catch (JSONException e) {
           e.printStackTrace();
       }

       return result;
    }

}
