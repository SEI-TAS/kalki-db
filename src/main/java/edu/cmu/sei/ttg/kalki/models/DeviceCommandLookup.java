package edu.cmu.sei.ttg.kalki.models;

import edu.cmu.sei.ttg.kalki.database.Postgres;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;

public class DeviceCommandLookup {
    private int id;
    private Integer deviceTypeId;
    private Integer stateId;
    private Integer commandId;

    private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    public DeviceCommandLookup() {
    }

    public DeviceCommandLookup(Integer deviceTypeId, Integer stateId, Integer commandId) {
        this.deviceTypeId = deviceTypeId;
        this.stateId = stateId;
        this.commandId = commandId;
    }

    public DeviceCommandLookup(int id, Integer deviceTypeId, Integer stateId, Integer commandId) {
        this.id = id;
        this.deviceTypeId = deviceTypeId;
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

    public void setDeviceTypeId(Integer deviceTypeId) {
        this.deviceTypeId = deviceTypeId;
    }

    public Integer getDeviceTypeId() {
        return deviceTypeId;
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
