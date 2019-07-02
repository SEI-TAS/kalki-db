package edu.cmu.sei.ttg.kalki.models;

import edu.cmu.sei.ttg.kalki.database.Postgres;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;

public class DeviceCommandLookup {
    private int id;
    private Integer stateId;
    private Integer commandId;

    private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    public DeviceCommandLookup() {
    }

    public DeviceCommandLookup(Integer stateId, Integer commandId) {
        this.stateId = stateId;
        this.commandId = commandId;
    }

    public DeviceCommandLookup(int id, Integer stateId, Integer commandId) {
        this.id = id;
        this.stateId = stateId;
        this.commandId = commandId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setStateId(Integer stateId) {
        this.stateId = stateId;
    }

    public Integer getStateId() {
        return stateId;
    }

    public void setCommandId(Integer commandId) {
        this.commandId = commandId;
    }

    public Integer getCommandId() {
        return commandId;
    }

    public Integer insert() {
        this.id = Postgres.insertCommandLookup(this);
        return this.id;
    }

    public Integer insertOrUpdate() {
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
