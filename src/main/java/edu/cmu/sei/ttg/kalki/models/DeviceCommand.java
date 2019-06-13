package edu.cmu.sei.ttg.kalki.models;
import edu.cmu.sei.ttg.kalki.database.Postgres;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;

public class DeviceCommand {
    private Integer id;
    private Integer deviceTypeId;
    private Integer stateId;
    private String name;

    private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    public DeviceCommand() {}

    public DeviceCommand(String name) {
        this.name = name;
    }

    public DeviceCommand(Integer id, String name) {
        this.deviceTypeId = null;
        this.stateId = null;
        this.id = id;
        this.name = name;
    }

    public DeviceCommand(Integer deviceTypeId, Integer stateId, String name){
        this.deviceTypeId = deviceTypeId;
        this.stateId = stateId;
        this.name = name;
    }

    public DeviceCommand(Integer id, Integer deviceTypeId, Integer stateId, String name){
        this(deviceTypeId, stateId, name);
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public void insert(){
        Postgres.insertCommand(this).thenApplyAsync(id -> {
            this.id = id;
            return id;
        });
    }

    public String toString() {
        try {
            return ow.writeValueAsString(this);
        }
        catch (JsonProcessingException e) {
            return "Bad DeviceCommand";
        }
    }
}
