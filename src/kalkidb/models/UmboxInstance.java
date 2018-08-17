package kalkidb.models;

import java.sql.Timestamp;

public class UmboxInstance {

    private int id;
    private String umboxExternalId;
    private int deviceId;
    private Timestamp startedAt;

    public UmboxInstance() {

    }

    public UmboxInstance(int id, String umboxExternalId, int deviceId, Timestamp startedAt) {
        this.id = id;
        this.umboxExternalId = umboxExternalId;
        this.deviceId = deviceId;
        this.startedAt = startedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUmboxExternalId() {
        return umboxExternalId;
    }

    public void setUmboxExternalId(String umboxExternalId) {
        this.umboxExternalId = umboxExternalId;
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

}