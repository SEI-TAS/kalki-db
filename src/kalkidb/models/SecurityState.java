package kalkidb.models;

import java.sql.Timestamp;
import kalkidb.database.Postgres;

public class SecurityState {

    private int id;
    private int deviceId;
    private Timestamp timestamp;
    private String state;

    public SecurityState() {

    }

    public SecurityState(int deviceId, String state) {
        this.deviceId = deviceId;
        this.state = state;
        long millis = System.currentTimeMillis() % 1000;
        this.timestamp = new Timestamp(millis);
    }

    public SecurityState(int id, int deviceId, Timestamp timestamp, String state) {
        this.id = id;
        this.deviceId = deviceId;
        this.timestamp = timestamp;
        this.state = state;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void insert(){
        Postgres.insertSecurityState(this).thenApplyAsync(id -> {
            this.id = id;
            return id;
        });
    }
}