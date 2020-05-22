package edu.cmu.sei.kalki.db.models;

import edu.cmu.sei.kalki.db.daos.AlertContextDAO;

import java.util.Map;

public class AlertContext extends Model  {
    private int id;
    private Integer deviceId;
    private String deviceName;
    private Integer alertTypeLookupId;
    private String alertTypeName;

    public AlertContext() {
    }

    public AlertContext(Integer deviceId, Integer alertTypeLookupId) {
        this.deviceId = deviceId;
        this.alertTypeLookupId = alertTypeLookupId;
    }

    public AlertContext(Integer deviceId, String deviceName, Integer alertTypeLookupId, String alertTypeName) {
        this(deviceId, alertTypeLookupId);
        this.deviceName = deviceName;
        this.alertTypeName = alertTypeName;
    }

    public AlertContext(int id, Integer deviceId, String deviceName, Integer alertTypeLookupId, String alertTypeName) {
        this(deviceId, deviceName, alertTypeLookupId, alertTypeName);
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

    public int insert() {
        setId(AlertContextDAO.insertAlertContext(this));
        return getId();
    }
}
