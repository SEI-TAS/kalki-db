package edu.cmu.sei.kalki.db.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;
import edu.cmu.sei.kalki.db.database.Postgres;

public class UmboxLookup {

    private int id;
    private Integer policyRuleId;
    private Integer umboxImageId;
    private Integer dagOrder;

    private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

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

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public String toString() {
        try {
            return ow.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "Bad umbox lookup";
        }
    }

    public Integer insert() {
        this.id = Postgres.insertUmboxLookup(this);
        return this.id;
    }

    public Integer insertOrUpdate() {
        this.id = Postgres.insertOrUpdateUmboxLookup(this);
        return this.id;
    }
}
