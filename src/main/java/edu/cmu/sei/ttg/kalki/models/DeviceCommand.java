package edu.cmu.sei.ttg.kalki.models;

import edu.cmu.sei.ttg.kalki.database.Postgres;
import java.util.concurrent.CompletionStage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;

public class DeviceCommand {
    private Integer id;
    private Integer lookupId;
    private Integer deviceTypeId;
    private Integer stateId;
    private String name;

    private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    public DeviceCommand() {
    }

    public DeviceCommand(String name) {
        this.name = name;
    }

    public DeviceCommand(Integer id, String name) {
        this.deviceTypeId = null;
        this.stateId = null;
        this.id = id;
        this.name = name;
    }

    public DeviceCommand(Integer deviceTypeId, Integer stateId, String name) {
        this.deviceTypeId = deviceTypeId;
        this.stateId = stateId;
        this.name = name;
    }

    public DeviceCommand(Integer id, Integer lookupId, Integer deviceTypeId, Integer stateId, String name) {
        this(deviceTypeId, stateId, name);
        this.id = id;
        this.lookupId = lookupId;
    }

    public DeviceCommand(Integer id, Integer lookupId, Integer deviceTypeId, Integer stateId) {
        this.id = id;
        this.lookupId = lookupId;
        this.deviceTypeId = deviceTypeId;
        this.stateId = stateId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getLookupId() {
        return lookupId;
    }

    public void setLookupId(Integer lookupId) {
        this.lookupId = lookupId;
    }

    public void setStateId(Integer stateId) {
        this.stateId = stateId;
    }

    public Integer getStateId() {
        return stateId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDeviceTypeId(Integer deviceTypeId) {
        this.deviceTypeId = deviceTypeId;
    }

    public Integer getDeviceTypeId() {
        return deviceTypeId;
    }

    public int insert() {
        this.id = Postgres.insertCommand(this);
        return this.id;
    }

    public Integer insertOrUpdate() {
        return Postgres.insertOrUpdateCommand(this);
    }

    public CompletionStage<Integer> insertOrUpdateCommandLookup() {
        return Postgres.insertOrUpdateCommandLookup(this);
    }

    public String toString() {
        try {
            return ow.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "Bad DeviceCommand";
        }
    }
}
