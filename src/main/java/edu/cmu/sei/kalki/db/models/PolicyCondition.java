package edu.cmu.sei.kalki.db.models;

import java.util.List;

import edu.cmu.sei.kalki.db.daos.PolicyConditionDAO;

public class PolicyCondition extends Model  {
    private int threshold;
    private List<Integer> alertTypeIds;

    public PolicyCondition() {}

    public PolicyCondition(int threshold, List<Integer> alertTypeIds) {
        this.threshold = threshold;
        this.alertTypeIds = alertTypeIds;
    }

    public PolicyCondition(int id, int threshold, List<Integer> alertTypeIds) {
        this(threshold, alertTypeIds);
        this.id = id;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public List<Integer> getAlertTypeIds() {
        return alertTypeIds;
    }

    public void setAlertTypeIds(List<Integer> alertTypeIds) {
        this.alertTypeIds = alertTypeIds;
    }

    public int insert(){
        int id = PolicyConditionDAO.insertPolicyCondition(this);
        if(id > 0)
            this.id = id;

        return this.id;
    }
}
