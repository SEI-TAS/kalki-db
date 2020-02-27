package edu.cmu.sei.kalki.db.models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import edu.cmu.sei.kalki.db.daos.UmboxInstanceDAO;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;

public class UmboxInstance {

    private int id;
    private String alerterId;
    private int umboxImageId;
    private int deviceId;
    private Timestamp startedAt;

    private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    public UmboxInstance() {

    }

    public UmboxInstance(String alerterId, int umboxImageId, int deviceId){
        this.alerterId = alerterId;
        this.umboxImageId = umboxImageId;
        this.deviceId = deviceId;
        long millis = System.currentTimeMillis();
        this.startedAt = new Timestamp(millis);
    }

    public UmboxInstance(String alerterId, int umboxImageId, int deviceId, Timestamp timestamp){
        this.alerterId = alerterId;
        this.umboxImageId = umboxImageId;
        this.deviceId = deviceId;
        this.startedAt = timestamp;
    }

    public UmboxInstance(int id, String alerterId, int umboxImageId, int deviceId, Timestamp startedAt) {
        this.id = id;
        this.alerterId = alerterId;
        this.umboxImageId = umboxImageId;
        this.deviceId = deviceId;
        this.startedAt = startedAt;
    }

    /**
     * Extract a UmboxInstance from the result set of a database query.
     */
    public static UmboxInstance createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) return null;
        int id = rs.getInt("id");
        String alerterId = rs.getString("alerter_id");
        int imageId = rs.getInt("umbox_image_id");
        int deviceId = rs.getInt("device_id");
        Timestamp startedAt = rs.getTimestamp("started_at");
        return new UmboxInstance(id, alerterId, imageId, deviceId, startedAt);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAlerterId() {
        return alerterId;
    }

    public void setAlerterId(String alerterId) {
        this.alerterId = alerterId;
    }

    public int getUmboxImageId() { return umboxImageId; }

    public void setUmboxImageId(int umboxImageId) { this.umboxImageId = umboxImageId; }

    public int getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public Timestamp getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Timestamp startedAt) {
        this.startedAt = startedAt;
    }

    public Integer insert() {
        this.id = UmboxInstanceDAO.insertUmboxInstance(this);
        return this.id;
    }

    public String toString() {
        try {
            return ow.writeValueAsString(this);
        }
        catch (JsonProcessingException e) {
            return "Bad UmboxInstance";
        }
    }
}
