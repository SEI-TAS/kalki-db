package edu.cmu.sei.kalki.db.models;

import edu.cmu.sei.kalki.db.daos.DeviceTypeDAO;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.sql.ResultSet;
import java.sql.SQLException;

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

    /**
     * Extract a DeviceType from the result set of a database query.
     */
    public static DeviceType createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) return null;
        int id = rs.getInt("id");
        String name = rs.getString("name");
        byte[] policyFile = rs.getBytes("policy_file");
        String policyFileName = rs.getString("policy_file_name");
        return new DeviceType(id, name, policyFile, policyFileName);
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
