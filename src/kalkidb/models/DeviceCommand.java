package kalkidb.models;
import kalkidb.database.Postgres;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;

public class DeviceCommand {
    private int deviceId;
    private int stateId;
    private String name;

    private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

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

    public String toString() {
        try {
            return ow.writeValueAsString(this);
        }
        catch (JsonProcessingException e) {
            return "Bad DeviceCommand";
        }
    }
}