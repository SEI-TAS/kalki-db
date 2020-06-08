package edu.cmu.sei.kalki.db.models;

import edu.cmu.sei.kalki.db.daos.AlertConditionDAO;
import edu.cmu.sei.kalki.db.daos.AlertContextDAO;

public class AlertCondition extends Model {
    private int deviceId;
    private int attributeId;
    private String attributeName;
    private int numStatues;
    private String compOperator;
    private String calculation;
    private int thresholdId;
    private String thresholdValue;

    public AlertCondition() {}

    public AlertCondition(int deviceId, int attributeId, String attributeName, int numStatues, String compOperator, String calculation, int thresholdId, String thresholdValue) {
        this.deviceId = deviceId;
        this.attributeId = attributeId;
        this.attributeName = attributeName;
        this.numStatues = numStatues;
        this.compOperator = compOperator;
        this.calculation = calculation;
        this.thresholdId = thresholdId;
        this.thresholdValue = thresholdValue;
    }

    public AlertCondition(int deviceId, int attributeId, String attributeName, int numStatues, ComparisonOperator compOperator, Calculation calc, int thresholdId, String thresholdValue) {
        this(deviceId, attributeId, attributeName, numStatues, compOperator.convert(), calc.convert(), thresholdId, thresholdValue);
    }


    public AlertCondition(int id, int deviceId, int attributeId, String attributeName, int numStatues, String compOperator, String calculation, int thresholdId, String thresholdValue) {
        this(deviceId, attributeId, attributeName, numStatues, compOperator, calculation, thresholdId, thresholdValue);
        this.id = id;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public int getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(int attributeId) {
        this.attributeId = attributeId;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public int getNumStatues() {
        return numStatues;
    }

    public void setNumStatues(int numStatues) {
        this.numStatues = numStatues;
    }

    public String getCompOperator() {
        return compOperator;
    }

    public void setCompOperator(String compOperator) {
        this.compOperator = compOperator;
    }

    public void setCompOperator(ComparisonOperator compOperator) {
        this.compOperator = compOperator.convert();
    }

    public String getCalculation() {
        return calculation;
    }

    public void setCalculation(String calculation) {
        this.calculation = calculation;
    }

    public void setCalculation(Calculation calculation) { this.calculation = calculation.convert(); }

    public int getThresholdId() {
        return thresholdId;
    }

    public void setThresholdId(int thresholdId) {
        this.thresholdId = thresholdId;
    }

    public String getThresholdValue() {
        return thresholdValue;
    }

    public void setThresholdValue(String thresholdValue) {
        this.thresholdValue = thresholdValue;
    }

    @Override
    public int insert() {
        int id = AlertConditionDAO.insertAlertCondition(this).getId();
        this.id = id;
        return id;
    }

    public void insertOrUpdate() {
        AlertCondition cond = AlertConditionDAO.insertOrUpdateAlertCondition(this);
        this.id = cond.getId();
    }

    public enum ComparisonOperator {
        EQUAL,
        GREATER,
        GREATER_OR_EQUAL,
        LESS,
        LESS_OR_EQUAL;

        private ComparisonOperator() {}

        public String convert() {
            switch (this) {
                case EQUAL:
                    return "=";
                case GREATER:
                    return ">";
                case GREATER_OR_EQUAL:
                    return ">=";
                case LESS:
                    return "<";
                case LESS_OR_EQUAL:
                    return "<=";
                default:
                    return "Unsupported operator";
            }
        }
    }

    public enum Calculation {
        AVERAGE,
        SUM,
        NONE;

        private Calculation() {}

        public String convert() {
            switch (this) {
                case AVERAGE:
                    return "Average";
                case SUM:
                    return "Sum";
                case NONE:
                    return "None";
                default:
                    return "Unsupported calculation";
            }
        }
    }
}
