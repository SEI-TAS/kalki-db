package kalkidb.models;
import kalkidb.database.Postgres;

import java.util.Map;

public class AlertCondition {
    private int id;
    private Map<String,String> variables;
    private int deviceId;
    private int alertTypeId;

    public AlertCondition(Map<String, String> variables, int deviceId, int alertTypeId) {
        this.variables = variables;
        this.deviceId = deviceId;
        this.alertTypeId = alertTypeId;
    }

    public AlertCondition(int id, Map<String, String> variables, int deviceId, int alertTypeId) {
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

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public int getAlertTypeId() {
        return alertTypeId;
    }

    public void setAlertTypeId(int alertTypeId) {
        this.alertTypeId = alertTypeId;
    }

    public void insert() {
        Postgres.insertAlertCondition(this).thenApplyAsync(id->{
            this.id = id;
            return id;
        });
    }
}