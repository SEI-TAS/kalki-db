package edu.cmu.sei.kalki.db.models;

import edu.cmu.sei.kalki.db.database.Postgres;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.sql.Timestamp;

public class PolicyRuleLog
{
    private int id;
    private int policyRuleId;
    private int deviceId;
    private Timestamp timestamp;

    private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

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
        int id = Postgres.insertPolicyRuleLog(this);
        if(id>0)
            this.id = id;
    }

    public String toString() {
        try {
            return ow.writeValueAsString(this);
        }
        catch (JsonProcessingException e) {
            return "Bad PolicyRuleLog";
        }
    }

}
