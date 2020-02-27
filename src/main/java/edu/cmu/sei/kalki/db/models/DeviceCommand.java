package edu.cmu.sei.kalki.db.models;

import edu.cmu.sei.kalki.db.database.Postgres;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DeviceCommand {
    private int id;
    private String name;
    private Integer deviceTypeId;

    private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    public DeviceCommand() {
    }

    public DeviceCommand(String name, Integer deviceTypeId) {
        this.name = name;
        this.deviceTypeId = deviceTypeId;
    }

    public DeviceCommand(int id, String name, Integer deviceTypeId) {
        this.id = id;
        this.name = name;
        this.deviceTypeId = deviceTypeId;
    }

    /**
     * Extract a Command name from the result set of a database query.
     */
    public static DeviceCommand createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) return null;
        int id = rs.getInt("id");
        String name = rs.getString("name");
        int deviceTypeId = rs.getInt("device_type_id");
        return new DeviceCommand(id, name, deviceTypeId);
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
        this.id = Postgres.insertOrUpdateCommand(this);
        return this.id;
    }

    public String toString() {
        try {
            return ow.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "Bad DeviceCommand";
        }
    }
}
