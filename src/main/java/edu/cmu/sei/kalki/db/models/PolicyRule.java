package edu.cmu.sei.kalki.db.models;

import edu.cmu.sei.kalki.db.daos.PolicyRuleDAO;

public class PolicyRule extends Model {
    private int id;
    private int stateTransId;
    private int policyCondId;
    private int devTypeId;
    private int samplingRate;

    public PolicyRule() {}

    public PolicyRule(int stateTransId, int policyCondId, int devTypeId, int samplingRate){
        this.stateTransId = stateTransId;
        this.policyCondId = policyCondId;
        this.devTypeId = devTypeId;
        this.samplingRate = samplingRate;
    }

    public PolicyRule(int id, int stateTransId, int policyCondId, int devTypeId, int samplingRate){
        this(stateTransId, policyCondId, devTypeId, samplingRate);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStateTransId() {
        return stateTransId;
    }

    public void setStateTransId(int stateTransId) {
        this.stateTransId = stateTransId;
    }

    public int getPolicyCondId() {
        return policyCondId;
    }

    public void setPolicyCondId(int policyCondId) {
        this.policyCondId = policyCondId;
    }

    public int getDevTypeId() {
        return devTypeId;
    }

    public void setDevTypeId(int devTypeId) {
        this.devTypeId = devTypeId;
    }

    public int getSamplingRate() {
        return samplingRate;
    }

    public void setSamplingRate(int samplingRate) {
        this.samplingRate = samplingRate;
    }

    public void insert() {
        int id = PolicyRuleDAO.insertPolicyRule(this);
        if(id > 0)
            this.id = id;
    }
}
