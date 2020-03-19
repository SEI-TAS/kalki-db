package edu.cmu.sei.kalki.db.models;

import edu.cmu.sei.kalki.db.daos.PolicyRuleLogDAO;

import java.sql.Timestamp;

public class PolicyRuleLog extends Model {
    private int id;
    private int policyRuleId;
    private int deviceId;
    private Timestamp timestamp;

    public PolicyRuleLog() {}

    public PolicyRuleLog(int policyRuleId, int deviceId) {
        this.policyRuleId = policyRuleId;
        this.deviceId = deviceId;

        long millis = System.currentTimeMillis();
        this.timestamp = new Timestamp(millis);
    }

    public PolicyRuleLog(int id, int policyRuleId, int deviceId, Timestamp timestamp) {
        this.id = id;
        this.policyRuleId = policyRuleId;
        this.deviceId = deviceId;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPolicyRuleId() {
        return policyRuleId;
    }

    public void setPolicyRuleId(int policyRuleId) {
        this.policyRuleId = policyRuleId;
    }

    public int getDeviceId() { return deviceId; }

    public void setDeviceId(int deviceId) { this.deviceId = deviceId; }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public void insert() {
        int id = PolicyRuleLogDAO.insertPolicyRuleLog(this);
        if(id>0)
            this.id = id;
    }
}
