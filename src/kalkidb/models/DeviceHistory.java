package kalkidb.models;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import kalkidb.database.Postgres;

public class DeviceHistory {

    private int id;
    private Timestamp timestamp;
    private Map<String, String> attributes;
    private int deviceId;

    public DeviceHistory(int deviceId){
        this.attributes = new HashMap<String, String>();
        long millis = System.currentTimeMillis() % 1000;
        this.timestamp = new Timestamp(millis);
        this.deviceId = deviceId;
    }

    public DeviceHistory(int deviceId, Map<String, String> attributes) {
        this(deviceId);
        this.attributes = attributes;
        long millis = System.currentTimeMillis() % 1000;
        this.timestamp = new Timestamp(millis);
    }

    public DeviceHistory(int deviceId, Map<String, String> attributes, Timestamp timestamp) {
        this(deviceId, attributes);
        this.timestamp = timestamp;
    }

    public DeviceHistory(int deviceId, Map<String, String> attributes, Timestamp timestamp, int id) {
        this(deviceId, attributes, timestamp);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public Timestamp getTimestamp() { return this.timestamp; }

    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void addAttribute(String key, String value){
        attributes.put(key, value);
    }

    public int getDeviceId() { return this.deviceId; }

    public void setDeviceId(int id) { this.deviceId = id; }

    public void insert(){
        Postgres.insertDeviceHistory(this).thenApplyAsync(id -> {
            this.id = id;
            return id;
        });
    }

    public void update(){
        Postgres.updateDeviceHistory(this);
    }

    public void insertOrUpdate(){
        Postgres.insertOrUpdateDeviceHistory(this);
    }

    public String toString() {
        String result = "DeviceHistory Info: deviceId: " + Integer.toString(deviceId) + ",";
        for(String key : attributes.keySet()){
            result += key + ": " + attributes.get(key) + ", ";
        }
        return result;
    }
}
