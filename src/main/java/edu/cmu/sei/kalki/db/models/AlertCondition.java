package edu.cmu.sei.kalki.db.models;

import edu.cmu.sei.kalki.db.daos.AlertConditionDAO;

import java.util.Map;

public class AlertCondition extends Model  {
    private int id;
    private Integer deviceId;
    private String deviceName;
    private Integer alertTypeLookupId;
    private String alertTypeName;
    private Map<String, String> variables;

    public AlertCondition() {
    }

    public AlertCondition(Integer deviceId, Integer alertTypeLookupId, Map<String, String> variables) {
        this.deviceId = deviceId;
        this.alertTypeLookupId = alertTypeLookupId;
        this.variables = variables;
    }

    public AlertCondition(Integer deviceId, String deviceName, Integer alertTypeLookupId, String alertTypeName, Map<String, String> variables) {
        this(deviceId, alertTypeLookupId, variables);
        this.deviceName = deviceName;
        this.alertTypeName = alertTypeName;
    }

    public AlertCondition(int id, Integer deviceId, String deviceName, Integer alertTypeLookupId, String alertTypeName, Map<String, String> variables) {
        this(deviceId, deviceName, alertTypeLookupId, alertTypeName, variables);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Integer deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() { return this.deviceName; }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public Integer getAlertTypeLookupId() {
        return alertTypeLookupId;
    }

    public void setAlertTypeLookupId(Integer alertTypeLookupId) {
        this.alertTypeLookupId = alertTypeLookupId;
    }

    public String getAlertTypeName() {
        return alertTypeName;
    }

    public void setAlertTypeName(String alertTypeName) {
        this.alertTypeName = alertTypeName;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }

    public int insert() {
        setId(AlertConditionDAO.insertAlertCondition(this));
        return getId();
    }
}
