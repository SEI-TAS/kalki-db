package edu.cmu.sei.kalki.db.models;

import java.sql.Timestamp;

import edu.cmu.sei.kalki.db.daos.AlertDAO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;

public class Alert {

    private int id;
    private String name;
    private Timestamp timestamp;
    private String alerterId;
    private int deviceId;
    private Integer deviceStatusId;
    private int alertTypeId;
    private String info;

    private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    public Alert(String name, String alerterId, int alertTypeId, String info){
        this.name = name;
        this.alerterId = alerterId;
        this.alertTypeId = alertTypeId;
        this.deviceId = 0;
        this.deviceStatusId = 0;
        this.info = info;
        long millis = System.currentTimeMillis();
        this.timestamp = new Timestamp(millis);
    }

    public Alert(int deviceId, String name, int alertTypeId, String info) {
        this.name = name;
        this.alerterId = null;
        this.deviceStatusId = 0;
        this.deviceId = deviceId;
        this.alertTypeId = alertTypeId;
        this.info = info;
        long millis = System.currentTimeMillis();
        this.timestamp = new Timestamp(millis);
    }

    public Alert(String name, Integer deviceStatusId, int alertTypeId, String info) {
        this.name = name;
        this.deviceStatusId = deviceStatusId;
        this.alertTypeId = alertTypeId;
        this.deviceId = 0;
        this.alerterId = null;
        this.info = info;
        long millis = System.currentTimeMillis();
        this.timestamp = new Timestamp(millis);
    }

    public Alert(String name, Timestamp timestamp, String alerterId, Integer deviceStatusId, int alertTypeId, String info) {
        this.name = name;
        this.timestamp = timestamp;
        this.alerterId = alerterId;
        this.deviceId = 0;
        this.deviceStatusId = deviceStatusId;
        this.alertTypeId = alertTypeId;
        this.info = info;
    }

    public Alert(int id, String name, Timestamp timestamp, String alerterId, int deviceId, Integer deviceStatusId, int alertTypeId, String info) {
        this.id = id;
        this.name = name;
        this.timestamp = timestamp;
        this.alerterId = alerterId;
        this.deviceStatusId = deviceStatusId;
        this.deviceId = deviceId;
        this.alertTypeId = alertTypeId;
        this.info = info;
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

    public Integer getDeviceId() { return deviceId; }

    public void setDeviceId(Integer deviceId) { this.deviceId = deviceId; }

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

    public String getInfo() { return info; }

    public void setInfo(String info ) { this.info = info; }

    public Integer insert() {
        this.id = AlertDAO.insertAlert(this);
        return this.id;
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
