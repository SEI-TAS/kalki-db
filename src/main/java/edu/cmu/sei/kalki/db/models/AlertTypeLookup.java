package edu.cmu.sei.kalki.db.models;

import edu.cmu.sei.kalki.db.daos.AlertTypeLookupDAO;

import java.util.Map;

public class AlertTypeLookup extends Model  {
    private int alertTypeId;
    private int deviceTypeId;
    private Map<String, String> variables;

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
        setId(AlertTypeLookupDAO.insertAlertTypeLookup(this));
        return getId();
    }

    public int insertOrUpdate() {
        setId(AlertTypeLookupDAO.insertOrUpdateAlertTypeLookup(this));
        return getId();
    }
}
