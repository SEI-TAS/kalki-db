package kalkidb.models;

import java.sql.Timestamp;

public class AlertHistory {

    private int id;
    private Timestamp timestamp;
    private String umboxExternalId;
    private String name;

    public AlertHistory() {

    }

    public AlertHistory(int id, Timestamp timestamp, String umboxExternalId, String name) {
        this.id = id;
        this.timestamp = timestamp;
        this.umboxExternalId = umboxExternalId;
        this.name = name;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}