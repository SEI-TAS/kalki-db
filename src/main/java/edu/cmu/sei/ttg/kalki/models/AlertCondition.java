package edu.cmu.sei.ttg.kalki.models;

import edu.cmu.sei.ttg.kalki.database.Postgres;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;

public class AlertCondition {
    private int id;
    private Map<String, String> variables;
    private Integer deviceId;
    private Integer deviceTypeId;
    private int alertTypeId;

    private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    public AlertCondition() {
    }

    public AlertCondition(Map<String, String> variables, Integer deviceId, int alertTypeId) {
        this.variables = variables;
        this.deviceId = deviceId;
        this.alertTypeId = alertTypeId;
        this.deviceTypeId = null;
    }

    public AlertCondition(Map<String, String> variables, Integer deviceId, int alertTypeId, Integer deviceTypeId) {
        this(variables, deviceId, alertTypeId);
        this.deviceTypeId = deviceTypeId;
    }

    public AlertCondition(int id, Map<String, String> variables, Integer deviceId, int alertTypeId) {
        this.id = id;
        this.variables = variables;
        this.deviceId = deviceId;
        this.alertTypeId = alertTypeId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }

    public Integer getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Integer deviceId) {
        this.deviceId = deviceId;
    }

    public Integer getDeviceTypeId() {
        return deviceTypeId;
    }

    public void setDeviceTypeId(Integer deviceTypeId) {
        this.deviceTypeId = deviceTypeId;
    }

    public int getAlertTypeId() {
        return alertTypeId;
    }

    public void setAlertTypeId(int alertTypeId) {
        this.alertTypeId = alertTypeId;
    }

    public Integer insert() {
        if (deviceId != null) {
            this.id = Postgres.insertAlertCondition(this);
            return this.id;
        } else {
            return Postgres.insertAlertConditionByDeviceType(this);
        }
    }

    public Integer insertOrUpdate() {
        this.id = Postgres.insertOrUpdateAlertCondition(this);
        return this.id;
    }

    public String toString() {
        try {
            return ow.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "Bad AlertCondition";
        }
    }
}
