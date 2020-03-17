package edu.cmu.sei.kalki.db.models;

import edu.cmu.sei.kalki.db.daos.DeviceTypeDAO;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;

public class DeviceType {

    private int id;
    private String name;

    private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    public DeviceType() {

    }

    public DeviceType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public DeviceType(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer insert() {
        this.id = DeviceTypeDAO.insertDeviceType(this);
        return this.id;
    }

    public Integer insertOrUpdate() {
        this.id = DeviceTypeDAO.insertOrUpdateDeviceType(this);
        return this.id;
    }

    public String toString() {
        try {
            return ow.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "Bad DeviceType";
        }
    }
}
