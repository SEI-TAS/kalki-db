package edu.cmu.sei.kalki.db.models;

import edu.cmu.sei.kalki.db.daos.PolicyRuleDAO;

public class PolicyRule extends Model {
    private static final int DEFAULT_SAMPLING_RATE_FACTOR = 1;

    private int stateTransitionId;
    private int policyConditionId;
    private int deviceTypeId;
    private int samplingRateFactor;

    public PolicyRule() {}

    public PolicyRule(int stateTransitionId, int policyConditionId, int deviceTypeId){
        this(stateTransitionId, policyConditionId, deviceTypeId, DEFAULT_SAMPLING_RATE_FACTOR);
    }

    public PolicyRule(int stateTransitionId, int policyConditionId, int deviceTypeId, int samplingRateFactor){
        this.stateTransitionId = stateTransitionId;
        this.policyConditionId = policyConditionId;
        this.deviceTypeId = deviceTypeId;
        this.samplingRateFactor = samplingRateFactor;
    }

    public PolicyRule(int id, int stateTransitionId, int policyConditionId, int deviceTypeId, int samplingRateFactor){
        this(stateTransitionId, policyConditionId, deviceTypeId, samplingRateFactor);
        this.id = id;
    }

    public int getStateTransitionId() {
        return stateTransitionId;
    }

    public void setStateTransitionId(int stateTransitionId) {
        this.stateTransitionId = stateTransitionId;
    }

    public int getPolicyConditionId() {
        return policyConditionId;
    }

    public void setPolicyConditionId(int policyConditionId) {
        this.policyConditionId = policyConditionId;
    }

    public int getDeviceTypeId() {
        return deviceTypeId;
    }

    public void setDeviceTypeId(int deviceTypeId) {
        this.deviceTypeId = deviceTypeId;
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

    public int insertOrUpdate(){
        int id = PolicyRuleDAO.insertOrUpdatePolicyRule(this);
        if(id > 0)
            this.id = id;
        return this.id;
    }
}
