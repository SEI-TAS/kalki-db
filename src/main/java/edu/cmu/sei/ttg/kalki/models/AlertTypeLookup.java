package edu.cmu.sei.ttg.kalki.models;

import edu.cmu.sei.ttg.kalki.database.Postgres;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;

public class AlertTypeLookup {
    private int id;
    private int alertTypeId;
    private int deviceTypeId;
    private Map<String, String> variables;

    private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    public AlertTypeLookup() { }

    public AlertTypeLookup(int alertTypeId, int deviceTypeId, Map<String, String> variables) {
        this.alertTypeId = alertTypeId;
        this.deviceTypeId = deviceTypeId;
        this.variables = variables;
    }

    public AlertTypeLookup(int id, int alertTypeId, int deviceTypeId, Map<String, String> variables) {
        this(alertTypeId, deviceTypeId, variables);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAlertTypeId() {
        return alertTypeId;
    }

    public void setAlertTypeId(int alertTypeId) {
        this.alertTypeId = alertTypeId;
    }

    public int getDeviceTypeId() {
        return deviceTypeId;
    }

    public void setDeviceTypeId(int deviceTypeId) {
        this.deviceTypeId = deviceTypeId;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }

    public int insert() {
        this.id = Postgres.insertAlertTypeLookup(this);
    }

    public int insertOrUpdate() {

    }

    public String toString() {
        try {
            return ow.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "Bad AlertTypeLookup";
        }
    }
}
