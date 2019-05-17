package kalkidb.models;
import kalkidb.database.Postgres;

public class DeviceCommand {
    private int deviceId;
    private int stateId;
    private String name;

    public DeviceCommand(int deviceId, int stateId, String name){
        this.deviceId = deviceId;
        this.stateId = stateId;
        this.name = name;
    }

    public void setStateId(int stateId) {
        this.stateId = stateId;
    }

    public int getStateId() {
        return stateId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public int getDeviceId() {
        return deviceId;
    }
}