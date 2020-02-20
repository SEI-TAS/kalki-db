package edu.cmu.sei.kalki.db.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

import edu.cmu.sei.kalki.db.database.Postgres;

public class PolicyCondition {
    private int id;
    private int threshold;
    private List<Integer> alertTypeIds;

    private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    public PolicyCondition() {}

    public PolicyCondition(int threshold, List<Integer> alertTypeIds) {
        this.threshold = threshold;
        this.alertTypeIds = alertTypeIds;
    }

    public PolicyCondition(int id, int threshold, List<Integer> alertTypeIds) {
        this(threshold, alertTypeIds);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public void insert(){
        int id = Postgres.insertPolicyCondition(this);
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
