package edu.cmu.sei.kalki.db.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;

import edu.cmu.sei.kalki.db.database.Postgres;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PolicyRule
{
    private int id;
    private int stateTransId;
    private int policyCondId;
    private int devTypeId;
    private int samplingRate;

    private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

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

    /**
     * Converts a ResultSet obj to a Policy
     */
    public static PolicyRule createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) return null;
        int id = rs.getInt("id");
        int stateTransId = rs.getInt("state_trans_id");
        int policyCondId = rs.getInt("policy_cond_id");
        int devTypeId = rs.getInt("device_type_id");
        int samplingRate = rs.getInt("sampling_rate");
        return new PolicyRule(id, stateTransId, policyCondId, devTypeId, samplingRate);
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
        int id = Postgres.insertPolicyRule(this);
        if(id > 0)
            this.id = id;
    }

    public String toString() {
        try {
            return ow.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "Bad policy";
        }
    }
}
