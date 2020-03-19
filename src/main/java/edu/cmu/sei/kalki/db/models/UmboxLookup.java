package edu.cmu.sei.kalki.db.models;

import edu.cmu.sei.kalki.db.daos.UmboxLookupDAO;

public class UmboxLookup extends Model  {
    private Integer policyRuleId;
    private Integer umboxImageId;
    private Integer dagOrder;

    public UmboxLookup() {
    }

    public UmboxLookup(Integer policyRuleId, Integer umboxImageId, Integer dagOrder) {
        this.policyRuleId = policyRuleId;
        this.umboxImageId = umboxImageId;
        this.dagOrder = dagOrder;
    }

    public UmboxLookup(int id, Integer policyRuleId, Integer umboxImageId, Integer dagOrder) {
        this(policyRuleId, umboxImageId, dagOrder);
        this.id = id;
    }

    public Integer getPolicyRuleId() {
        return policyRuleId;
    }

    public void setPolicyRuleId(Integer policyRuleId) {
        this.policyRuleId = policyRuleId;
    }

    public Integer getUmboxImageId() {
        return umboxImageId;
    }

    public void setUmboxImageId(Integer umboxImageId) {
        this.umboxImageId = umboxImageId;
    }

    public Integer getDagOrder() {
        return dagOrder;
    }

    public void setDagOrder(Integer dagOrder) {
        this.dagOrder = dagOrder;
    }

    public int insert() {
        this.id = UmboxLookupDAO.insertUmboxLookup(this);
        return this.id;
    }

    public int insertOrUpdate() {
        this.id = UmboxLookupDAO.insertOrUpdateUmboxLookup(this);
        return this.id;
    }
}
