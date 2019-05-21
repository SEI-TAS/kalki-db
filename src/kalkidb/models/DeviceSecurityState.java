package kalkidb.models;

import java.sql.Timestamp;
import kalkidb.database.Postgres;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;

public class DeviceSecurityState {
    private int id;
    private int deviceId;
    private int stateId;
    private Timestamp timestamp;
    private String name;

    private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    public DeviceSecurityState() {

    }

    public DeviceSecurityState(int deviceId, int stateId) {
        this.deviceId = deviceId;
        this.stateId = stateId;
        long millis = System.currentTimeMillis();
        this.timestamp = new Timestamp(millis);
    }

    public DeviceSecurityState(int deviceId, int stateId, String name) {
        this.deviceId = deviceId;
        this.name = name;
        long millis = System.currentTimeMillis();
        this.timestamp = new Timestamp(millis);
    }

    public DeviceSecurityState(int deviceId, int stateId, Timestamp timestamp, String name) {
        this.deviceId = deviceId;
        this.stateId = stateId;
        this.timestamp = timestamp;
        this.name = name;
    }

    public DeviceSecurityState(int id, int deviceId, int stateId, Timestamp timestamp, String name) {
        this.id = id;
        this.deviceId = deviceId;
        this.stateId = stateId;
        this.timestamp = timestamp;
        this.name = name;
    }

    public int getId() { return  id; }

    public void setId(int id) {
        this.id = id;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public int getStateId() {
        return stateId;
    }

    public void setStateId(int stateId) {
        this.stateId = stateId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void insert(){
        Postgres.insertDeviceSecurityState(this).thenApplyAsync(id -> {
            this.id = id;
            return id;
        });
    }

    public String toString() {
        try {
            return ow.writeValueAsString(this);
        }
        catch (JsonProcessingException e) {
            return "Bad DeviceSecurityState";
        }
    }
}