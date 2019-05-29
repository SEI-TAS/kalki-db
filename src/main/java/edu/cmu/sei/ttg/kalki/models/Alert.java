package edu.cmu.sei.ttg.kalki.models;

import java.sql.Timestamp;
import edu.cmu.sei.ttg.kalki.database.Postgres;
import java.util.Optional;
import java.lang.NullPointerException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;

public class Alert {

    private int id;
    private String name;
    private Timestamp timestamp;
    private String alerterId;
    private Integer deviceStatusId;
    private int alertTypeId;

    private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();


    public Alert() {

    }

    public Alert(String name, String alerterId, int alertTypeId){
        this.name = name;
        this.alerterId = alerterId;
        this.alertTypeId = alertTypeId;
        this.deviceStatusId = null;
        long millis = System.currentTimeMillis();
        this.timestamp = new Timestamp(millis);
    }

    public Alert(String name, Integer deviceStatusId, int alertTypeId) {
        this.name = name;
        this.deviceStatusId = deviceStatusId;
        this.alertTypeId = alertTypeId;
        this.alerterId = null;
        long millis = System.currentTimeMillis();
        this.timestamp = new Timestamp(millis);
    }

    public Alert(String name, String alerterId, Integer deviceStatusId, int alertTypeId) {
        this.name = name;
        this.alerterId = alerterId;
        this.deviceStatusId = deviceStatusId;
        this.alertTypeId = alertTypeId;

        long millis = System.currentTimeMillis();
        this.timestamp = new Timestamp(millis);
    }

    public Alert(String name, Timestamp timestamp, String alerterId, Integer deviceStatusId, int alertTypeId) {
        this.name = name;
        this.timestamp = timestamp;
        this.alerterId = alerterId;
        this.deviceStatusId = deviceStatusId;
        this.alertTypeId = alertTypeId;
    }


    public Alert(int id, String name, Timestamp timestamp, String alerterId, Integer deviceStatusId, int alertTypeId) {
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

    public Integer getDeviceStatusId() {
        return deviceStatusId;
    }

    public void setDeviceStatusId(Integer deviceStatusId) {
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
        try {
            return ow.writeValueAsString(this);
        }
        catch (JsonProcessingException e) {
            return "Bad Alert";
        }
    }
}