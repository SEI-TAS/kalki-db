package kalkidb.models;

import java.sql.Timestamp;

public class AlertHistory {

    private int id;
    private Timestamp timestamp;
    private String umboxExternalId;
    private String info;

    public AlertHistory() {

    }

    public AlertHistory(Timestamp timestamp, String umboxExternalId, String info) {
        this.timestamp = timestamp;
        this.umboxExternalId = umboxExternalId;
        this.info = info;
    }


    public AlertHistory(int id, Timestamp timestamp, String umboxExternalId, String info) {
        this.id = id;
        this.timestamp = timestamp;
        this.umboxExternalId = umboxExternalId;
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

    public String getUmboxExternalId() {
        return umboxExternalId;
    }

    public void setUmboxExternalId(String umboxExternalId) {
        this.umboxExternalId = umboxExternalId;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String toString() {
        return "AlertHistory Info: id: "+Integer.toString(id)+", umboxExternalId: "+umboxExternalId+", info: "+info;
    }
}