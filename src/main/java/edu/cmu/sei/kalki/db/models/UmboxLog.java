package edu.cmu.sei.kalki.db.models;

import java.sql.Timestamp;
import edu.cmu.sei.kalki.db.database.Postgres;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;

public class UmboxLog {
    private int id;
    private String alerter_id;
    private String details;
    private Timestamp timestamp;

    private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    public UmboxLog() {}

    public UmboxLog(String alerter_id, String details){
        this.alerter_id = alerter_id;
        this.details = details;
    }

    public UmboxLog(int id, String alerter_id, String details, Timestamp timestamp) {
        this.id = id;
        this.alerter_id = alerter_id;
        this.details = details;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAlerterId() {
        return alerter_id;
    }

    public void setAlerterId(String alerter_id) {
        this.alerter_id = alerter_id;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public void insert() {
        this.id = Postgres.insertUmboxLog(this);

        UmboxLog temp = Postgres.findUmboxLog(this.id);
        this.timestamp = temp.getTimestamp();
    }

    public String toString() {
        try {
            return ow.writeValueAsString(this);
        }
        catch (JsonProcessingException e) {
            return "Bad Alert";
        }
    }
}

