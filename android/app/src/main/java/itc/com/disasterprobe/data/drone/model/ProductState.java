package itc.com.disasterprobe.data.drone.model;

import org.json.JSONException;
import org.json.JSONObject;

import dji.common.product.Model;

/**
 * Created by anne on 22-3-18.
 */

public class ProductState {

    private Boolean connected = false;
    private Model model = null;
    private String modelName = "N/A";
    private Boolean loggedIn = false;
    private Boolean sdkRegistered = false;
    private Boolean remoteConnected = false;
    private Boolean flightControllerConnected = false;

    public Boolean getConnected() {
        return connected;
    }

    public void setConnected(Boolean connected) {
        this.connected = connected;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
        this.modelName = ((model == null) ? "N/A" : model.name());
    }

    public Boolean getLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(Boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public Boolean getSdkRegistered() {
        return sdkRegistered;
    }

    public void setSdkRegistered(Boolean sdkRegistered) {
        this.sdkRegistered = sdkRegistered;
    }

    public Boolean getRemoteConnected() {
        return remoteConnected;
    }

    public void setRemoteConnected(Boolean remoteConnected) {
        this.remoteConnected = remoteConnected;
    }

    public void setFlightControllerConnected(Boolean connected) {
        this.flightControllerConnected = connected;
    }

    public Boolean getFlightControllerConnected() {
        return  this.flightControllerConnected;
    }

    public String getModelName(){
        return modelName;
    }

    public JSONObject toJson() {
       JSONObject result = new JSONObject();
        try {
            result.put("connected", connected);
            result.put("model", (model == null) ? "null" : model.toString());
            result.put("modelName", modelName);
            result.put("loggedIn", loggedIn);
            result.put("sdkRegistered", sdkRegistered);
            result.put("remoteConnected", remoteConnected);
            result.put("flightControllerConnected", flightControllerConnected);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

}
