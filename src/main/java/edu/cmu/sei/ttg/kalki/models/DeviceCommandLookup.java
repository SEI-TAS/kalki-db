package edu.cmu.sei.ttg.kalki.models;

import edu.cmu.sei.ttg.kalki.database.Postgres;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;

public class DeviceCommandLookup {
    private int id;
    private int commandId;
    private int currentStateId;
    private int previousStateId;
    private int deviceTypeId;

    private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    public DeviceCommandLookup() {
    }

    public DeviceCommandLookup(int commandId, int currentStateId, int previousStateId, int deviceTypeId) {
        this.currentStateId = currentStateId;
        this.previousStateId = previousStateId;
        this.deviceTypeId = deviceTypeId;
        this.commandId = commandId;
    }

    public DeviceCommandLookup(int id, int commandId, int currentStateId, int previousStateId, int deviceTypeId) {
        this.id = id;
        this.currentStateId = currentStateId;
        this.previousStateId = previousStateId;
        this.deviceTypeId = deviceTypeId;
        this.commandId = commandId;
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

    public void setCurrentStateId(int currentStateId) {
        this.currentStateId = currentStateId;
    }

    public int getCurrentStateId() {
        return currentStateId;
    }

    public void setPreviousStateId(int previousStateId) {
        this.previousStateId = previousStateId;
    }

    public int getPreviousStateId() {
        return previousStateId;
    }

    public int getDeviceTypeId() {
        return deviceTypeId;
    }

    public void setDeviceTypeId(int deviceTypeId) {
        this.deviceTypeId = deviceTypeId;
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
