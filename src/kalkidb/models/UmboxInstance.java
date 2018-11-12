package kalkidb.models;

import java.sql.Timestamp;
import kalkidb.database.Postgres;

public class UmboxInstance {

    private int id;
    private String alerterId;
    private int umboxImageId;
    private String containerId;
    private int deviceId;
    private Timestamp startedAt;

    public UmboxInstance() {

    }

    public UmboxInstance(int id, String alerterId, int umboxImageId, String containerId, int deviceId, Timestamp startedAt) {
        this.id = id;
        this.alerterId = alerterId;
        this.umboxImageId = umboxImageId;
        this.containerId = containerId;
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

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId){
        this.containerId = containerId;
    }

    public int getDeviceId() {
        return deviceId;
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

    public void insert() { Postgres.insertUmboxInstance(this); }
}
