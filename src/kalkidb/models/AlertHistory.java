package kalkidb.models;

import java.sql.Timestamp;
import kalkidb.database.Postgres;

public class AlertHistory {

    private int id;
    private String name;
    private Timestamp timestamp;
    private String source;
    private String alerterId;
    private int deviceStatusId;

    public AlertHistory() {

    }

    public AlertHistory(String name, String source, String alerterId, int deviceStatusId) {
        this.name = name;
        this.source = source;
        this.alerterId = alerterId;
        this.deviceStatusId = deviceStatusId;

        long millis = System.currentTimeMillis() % 1000;
        this.timestamp = new Timestamp(millis);
    }

    public AlertHistory(String name, Timestamp timestamp, String source, String alerterId, int deviceStatusId) {
        this.name = name;
        this.timestamp = timestamp;
        this.source = source;
        this.alerterId = alerterId;
        this.deviceStatusId = deviceStatusId;
    }


    public AlertHistory(int id, String name, Timestamp timestamp, String source, String alerterId, int deviceStatusId) {
        this.id = id;
        this.name = name;
        this.timestamp = timestamp;
        this.source = source;
        this.alerterId = alerterId;
        this.deviceStatusId = deviceStatusId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getAlerterId() {
        return alerterId;
    }

    public void setAlerterId(String alerterId) {
        this.alerterId = alerterId;
    }

    public int getDeviceStatusId() {
        return deviceStatusId;
    }

    public void setDeviceStatusId(int deviceStatusId) {
        this.deviceStatusId = deviceStatusId;
    }

    public void insert() {
        Postgres.insertAlertHistory(this).thenApplyAsync(id -> {
            this.id = id;
            return id;
        });
    }

    public String toString() {
        return "AlertHistory Info: id: "+Integer.toString(id)+", alerterId: "+ alerterId +", name: "+name;
    }
}