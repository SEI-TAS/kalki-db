package kalkidb.models;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import kalkidb.database.Postgres;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;

public class DeviceStatus {

    private int id;
    private Timestamp timestamp;
    private Map<String, String> attributes;
    private int deviceId;
    private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    public DeviceStatus(int deviceId){
        this.attributes = new HashMap<String, String>();
        long millis = System.currentTimeMillis();
        this.timestamp = new Timestamp(millis);
        this.deviceId = deviceId;
    }

    public DeviceStatus(int deviceId, Map<String, String> attributes) {
        this(deviceId);
        this.attributes = attributes;
        long millis = System.currentTimeMillis();
        this.timestamp = new Timestamp(millis);
    }

    public DeviceStatus(int deviceId, Map<String, String> attributes, Timestamp timestamp) {
        this(deviceId, attributes);
        this.timestamp = timestamp;
    }

    public DeviceStatus(int deviceId, Map<String, String> attributes, Timestamp timestamp, int id) {
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
        Postgres.insertDeviceStatus(this).thenApplyAsync(id -> {
            this.id = id;
            return id;
        });
    }

    public void update(){
        Postgres.updateDeviceStatus(this);
    }

    public void insertOrUpdate(){
        Postgres.insertOrUpdateDeviceStatus(this);
    }

    public String toString() {
        try {
            return ow.writeValueAsString(this);
        }
        catch (JsonProcessingException e) {
            return "Bad device";
        }
    }
}
