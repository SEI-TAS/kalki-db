package edu.cmu.sei.ttg.kalki.models;

import edu.cmu.sei.ttg.kalki.database.Postgres;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;

public class DeviceCommandLookup {
    private int id;
    private int commandId;
    private int policyId;

    private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    public DeviceCommandLookup() {
    }

    public DeviceCommandLookup(int commandId, int policyId) {
        this.commandId = commandId;
        this.policyId = policyId;
    }

    public DeviceCommandLookup(int id, int commandId, int policyId) {
        this(commandId, policyId);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCommandId(int commandId) {
        this.commandId = commandId;
    }

    public int getCommandId() {
        return commandId;
    }

    public int getPolicyId() {
        return policyId;
    }

    public void setPolicyId(int policyId) {
        this.policyId = policyId;
    }

    public int insert() {
        this.id = Postgres.insertCommandLookup(this);
        return this.id;
    }

    public int insertOrUpdate() {
        this.id = Postgres.insertOrUpdateCommandLookup(this);
        return this.id;
    }

    public String toString() {
        try {
            return ow.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "Bad DeviceCommandLookup";
        }
    }
}
