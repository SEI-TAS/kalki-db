package edu.cmu.sei.ttg.kalki.models;

import edu.cmu.sei.ttg.kalki.database.Postgres;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.sql.Timestamp;

public class PolicyInstance {
    private int id;
    private int policyId;
    private int deviceId;
    private Timestamp timestamp;

    private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    public PolicyInstance() {}

    public PolicyInstance(int policyId) {
        this.policyId = policyId;

        long millis = System.currentTimeMillis();
        this.timestamp = new Timestamp(millis);
    }

    public PolicyInstance(int id, int policyId, int deviceId, Timestamp timestamp) {
        this.id = id;
        this.policyId = policyId;
        this.deviceId = deviceId;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPolicyId() {
        return policyId;
    }

    public void setPolicyId(int policyId) {
        this.policyId = policyId;
    }

    public int getDeviceId() { return deviceId; }

    public void setDeviceId(int device id) { this.deviceId = deviceId; }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public void insert() {
        int id = Postgres.insertPolicyInstance(this);
        if(id>0)
            this.id = id;
    }

    public String toString() {
        try {
            return ow.writeValueAsString(this);
        }
        catch (JsonProcessingException e) {
            return "Bad PolicyInstance";
        }
    }

}
