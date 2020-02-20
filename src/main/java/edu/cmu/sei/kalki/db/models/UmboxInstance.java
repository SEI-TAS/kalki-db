package edu.cmu.sei.kalki.db.models;

import java.sql.Timestamp;
import edu.cmu.sei.kalki.db.database.Postgres;
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
        this.id = Postgres.insertUmboxInstance(this);
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
