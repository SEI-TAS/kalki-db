package kalkidb.models;

import java.sql.Timestamp;
import kalkidb.database.Postgres;

public class Alert {

    private int id;
    private String name;
    private Timestamp timestamp;
    private String alerterId;
    private int deviceStatusId;
    private int alertTypeId;

    public Alert() {

    }

    public Alert(String name, String alerterId, int deviceStatusId) {
        this.name = name;
        this.alerterId = alerterId;
        this.deviceStatusId = deviceStatusId;

        long millis = System.currentTimeMillis() % 1000;
        this.timestamp = new Timestamp(millis);
    }

    public Alert(String name, Timestamp timestamp, String alerterId, int deviceStatusId, int alertTypeId) {
        this.name = name;
        this.timestamp = timestamp;
        this.alerterId = alerterId;
        this.deviceStatusId = deviceStatusId;
        this.alertTypeId = alertTypeId;
    }


    public Alert(int id, String name, Timestamp timestamp, String alerterId, int deviceStatusId, int alertTypeId) {
        this.id = id;
        this.name = name;
        this.timestamp = timestamp;
        this.alerterId = alerterId;
        this.deviceStatusId = deviceStatusId;
        this.alertTypeId = alertTypeId;
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

    public int getAlertTypeId() { return alertTypeId; }

    public void setAlertTypeId(int alertTypeId ) { this.alertTypeId = alertTypeId; }

    public void insert() {
        Postgres.insertAlert(this).thenApplyAsync(id -> {
            this.id = id;
            return id;
        });
    }

    public String toString() {
        return "Alert Info: id: "+Integer.toString(id)+", alerterId: "+ alerterId +", name: "+name;
    }
}