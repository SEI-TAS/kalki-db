package edu.cmu.sei.kalki.db.models;

import edu.cmu.sei.kalki.db.daos.DeviceStatusDAO;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.postgresql.util.HStoreConverter;

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

    /**
     * Extract a DeviceStatus from the result set of a database query.
     */
    public static DeviceStatus createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) return null;
        int deviceId = rs.getInt("device_id");
        Map<String, String> attributes = HStoreConverter.fromString(rs.getString("attributes"));
        Timestamp timestamp = rs.getTimestamp("timestamp");
        int statusId = rs.getInt("id");
        return new DeviceStatus(deviceId, attributes, timestamp, statusId);
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

    public Integer insert(){
        this.id = DeviceStatusDAO.insertDeviceStatus(this);
        return this.id;
    }

    public Integer update(){
        return DeviceStatusDAO.updateDeviceStatus(this);
    }

    public Integer insertOrUpdate(){
        this.id = DeviceStatusDAO.insertOrUpdateDeviceStatus(this);
        return this.id;
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
