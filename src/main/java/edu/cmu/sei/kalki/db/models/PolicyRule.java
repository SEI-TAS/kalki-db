package edu.cmu.sei.kalki.db.models;

import edu.cmu.sei.kalki.db.daos.PolicyRuleDAO;

public class PolicyRule extends Model {
    private static final int DEFAULT_SAMPLING_RATE_FACTOR = 1;

    private int stateTransId;
    private int policyCondId;
    private int devTypeId;
    private int samplingRateFactor;

    public PolicyRule() {}

    public PolicyRule(int stateTransId, int policyCondId, int devTypeId){
        this(stateTransId, policyCondId, devTypeId, DEFAULT_SAMPLING_RATE_FACTOR);
    }

    public PolicyRule(int stateTransId, int policyCondId, int devTypeId, int samplingRateFactor){
        this.stateTransId = stateTransId;
        this.policyCondId = policyCondId;
        this.devTypeId = devTypeId;
        this.samplingRateFactor = samplingRateFactor;
    }

    public PolicyRule(int id, int stateTransId, int policyCondId, int devTypeId, int samplingRateFactor){
        this(stateTransId, policyCondId, devTypeId, samplingRateFactor);
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

    public int getSamplingRateFactor() {
        return samplingRateFactor;
    }

    public void setSamplingRateFactor(int samplingRateFactor) {
        this.samplingRateFactor = samplingRateFactor;
    }

    public int insert() {
        int id = PolicyRuleDAO.insertPolicyRule(this);
        if(id > 0)
            this.id = id;
        return this.id;
    }
}
