package edu.cmu.sei.kalki.db.models;

import edu.cmu.sei.kalki.db.daos.AlertContextDAO;

import java.util.ArrayList;
import java.util.List;

public class AlertContext extends Model  {
    private int id;
    private Integer deviceTypeId;
    private int alertTypeLookupId;
    private String alertTypeName;
    private String logicalOperator;
    private List<AlertCondition> conditions = new ArrayList<>();

    public AlertContext() {
    }

    public AlertContext(Integer alertTypeLookupId, String logicalOperator) {
        this.alertTypeLookupId = alertTypeLookupId;
        this.logicalOperator = logicalOperator;
    }

    public AlertContext(Integer alertTypeLookupId, LogicalOperator logicalOperator) {
        this(alertTypeLookupId, logicalOperator.convert());
    }

    public AlertContext(Integer deviceTypeId, String logicalOperator, Integer alertTypeLookupId, String alertTypeName) {
        this(alertTypeLookupId, logicalOperator);
        this.deviceTypeId = deviceTypeId;
        this.alertTypeName = alertTypeName;
    }

    public AlertContext(Integer deviceTypeId, LogicalOperator logicalOperator, Integer alertTypeLookupId, String alertTypeName) {
        this(deviceTypeId, logicalOperator.convert(), alertTypeLookupId, alertTypeName);
    }

    public AlertContext(int id, Integer deviceTypeId, String logicalOperator, Integer alertTypeLookupId, String alertTypeName) {
        this(deviceTypeId, logicalOperator, alertTypeLookupId, alertTypeName);
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

    public void insertOrUpdate() {
        AlertContext data = AlertContextDAO.insertOrUpdateAlertContext(this);
        this.id = data.getId();
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
