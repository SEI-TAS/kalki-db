package kalkidb.models;

import java.sql.Timestamp;

public class AlertHistory {

    private int id;
    private Timestamp timestamp;
    private String alerterId;
    private String info;

    public AlertHistory() {

    }

    public AlertHistory(Timestamp timestamp, String alerterId, String info) {
        this.timestamp = timestamp;
        this.alerterId = alerterId;
        this.info = info;
    }


    public AlertHistory(int id, Timestamp timestamp, String alerterId, String info) {
        this.id = id;
        this.timestamp = timestamp;
        this.alerterId = alerterId;
        this.info = info;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String toString() {
        return "AlertHistory Info: id: "+Integer.toString(id)+", alerterId: "+ alerterId +", info: "+info;
    }
}