package edu.cmu.sei.kalki.db.models;

import edu.cmu.sei.kalki.db.daos.AlertContextDAO;
import sun.rmi.runtime.Log;

import java.util.ArrayList;
import java.util.List;

public class AlertContext extends Model  {
    private int id;
    private Integer deviceId;
    private String deviceName;
    private Integer alertTypeLookupId;
    private String alertTypeName;
    private String logicalOperator;
    private List<AlertCondition> conditions;

    public AlertContext() {
    }

    public AlertContext(Integer deviceId, Integer alertTypeLookupId, String logicalOperator) {
        this.deviceId = deviceId;
        this.alertTypeLookupId = alertTypeLookupId;
        this.logicalOperator = logicalOperator;
        this.conditions = new ArrayList<AlertCondition>();
    }

    public AlertContext(Integer deviceId, Integer alertTypeLookupId, LogicalOperator logicalOperator) {
        this(deviceId, alertTypeLookupId, logicalOperator.convert());
    }

    public AlertContext(Integer deviceId, String deviceName, String logicalOperator, Integer alertTypeLookupId, String alertTypeName) {
        this(deviceId, alertTypeLookupId, logicalOperator);
        this.deviceName = deviceName;
        this.alertTypeName = alertTypeName;
    }

    public AlertContext(Integer deviceId, String deviceName, LogicalOperator logicalOperator, Integer alertTypeLookupId, String alertTypeName) {
        this(deviceId, deviceName, logicalOperator.convert(), alertTypeLookupId, alertTypeName);
    }

    public AlertContext(int id, Integer deviceId, String deviceName, String logicalOperator, Integer alertTypeLookupId, String alertTypeName) {
        this(deviceId, deviceName, logicalOperator, alertTypeLookupId, alertTypeName);
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

    public String getLogicalOperator() {
        return logicalOperator;
    }

    public void setLogicalOperator(String logicalOperator) {
        this.logicalOperator = logicalOperator;
    }

    public void setLogicalOperator(LogicalOperator logicalOperator) {
        this.logicalOperator = logicalOperator.convert();
    }

    public List<AlertCondition> getConditions() {
        return conditions;
    }

    public void setConditions(List<AlertCondition> conditions) {
        this.conditions = conditions;
    }

    public void addCondition(AlertCondition cond) {
        this.conditions.add(cond);
    }

    public void removeCondition(AlertCondition cond) {
        this.conditions.remove(cond);
    }

    public int insert() {
        AlertContext data = AlertContextDAO.insertAlertContext(this);
        setId(data.getId());
        return getId();
    }


    public enum LogicalOperator {
        AND,
        OR,
        NONE;

        private LogicalOperator() {}

        public String convert() {
            switch (this) {
                case AND:
                    return "AND";
                case OR:
                    return "OR";
                case NONE:
                    return "NONE";
                default:
                    return "Unsupported operator";
            }
        }
    }

}
