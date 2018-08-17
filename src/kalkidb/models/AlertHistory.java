package kalkidb.models;

import java.sql.Timestamp;

public class AlertHistory {

    private int id;
    private Timestamp timestamp;
    private int externalId;
    private String name;

    public AlertHistory() {

    }

    public AlertHistory(int id, Timestamp timestamp, int externalId, String name) {
        this.id = id;
        this.timestamp = timestamp;
        this.externalId = externalId;
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

    public int getExternalId() {
        return externalId;
    }

    public void setExternalId(int externalId) {
        this.externalId = externalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}