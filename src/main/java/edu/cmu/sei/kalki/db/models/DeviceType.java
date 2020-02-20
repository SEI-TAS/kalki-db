package edu.cmu.sei.kalki.db.models;

import edu.cmu.sei.kalki.db.database.Postgres;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;

public class DeviceType {

    private int id;
    private String name;
    private byte[] policyFile;
    private String policyFileName;

    private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    public DeviceType() {

    }

    public DeviceType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public DeviceType(String name, byte[] policyFile, String policyFileName) {
        this.name = name;
        this.policyFile = policyFile;
        this.policyFileName = policyFileName;
    }

    public DeviceType(int id, String name, byte[] policyFile, String policyFileName) {
        this.id = id;
        this.name = name;
        this.policyFile = policyFile;
        this.policyFileName = policyFileName;
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

    public byte[] getPolicyFile() {
        return policyFile;
    }

    public void setPolicyFile(byte[] policyFile) {
        this.policyFile = policyFile;
    }

    public String getPolicyFileName() {
        return policyFileName;
    }

    public void setPolicyFileName(String policyFileName) {
        this.policyFileName = policyFileName;
    }

    public Integer insert() {
        this.id = Postgres.insertDeviceType(this);
        return this.id;
    }

    public Integer insertOrUpdate() {
        this.id = Postgres.insertOrUpdateDeviceType(this);
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
