package edu.cmu.sei.kalki.db.models;

import edu.cmu.sei.kalki.db.daos.AlertContextDAO;

import java.util.ArrayList;
import java.util.List;

public class AlertContext extends Model  {
    private int id;
    private Integer deviceTypeId;
    private String deviceTypeName;
    private Integer alertTypeLookupId;
    private String alertTypeName;
    private String logicalOperator;
    private List<AlertCondition> conditions;
    private Integer deviceId;

    public AlertContext() {
    }

    public AlertContext(Integer alertTypeLookupId, String logicalOperator) {
        this.alertTypeLookupId = alertTypeLookupId;
        this.logicalOperator = logicalOperator;
        this.conditions = new ArrayList<>();
    }

    public AlertContext(Integer alertTypeLookupId, LogicalOperator logicalOperator) {
        this(alertTypeLookupId, logicalOperator.convert());
    }

    public AlertContext(Integer deviceTypeId, String deviceTypeName, String logicalOperator, Integer alertTypeLookupId, String alertTypeName, Integer deviceId) {
        this(alertTypeLookupId, logicalOperator);
        this.deviceTypeId = deviceTypeId;
        this.deviceTypeName = deviceTypeName;
        this.alertTypeName = alertTypeName;
        this.deviceId = deviceId;
    }

    public AlertContext(Integer deviceTypeId, String deviceTypeName, LogicalOperator logicalOperator, Integer alertTypeLookupId, String alertTypeName, Integer deviceId) {
        this(deviceTypeId, deviceTypeName, logicalOperator.convert(), alertTypeLookupId, alertTypeName, deviceId);
    }

    public AlertContext(int id, Integer deviceTypeId, String deviceTypeName, String logicalOperator, Integer alertTypeLookupId, String alertTypeName, Integer deviceId) {
        this(deviceTypeId, deviceTypeName, logicalOperator, alertTypeLookupId, alertTypeName, deviceId);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getDeviceTypeId() {
        return deviceTypeId;
    }

    public void setDeviceTypeId(Integer deviceTypeId) {
        this.deviceTypeId = deviceTypeId;
    }

    public Integer getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Integer deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() { return this.deviceTypeName; }

    public void setDeviceName(String deviceTypeName) {
        this.deviceTypeName = deviceTypeName;
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
