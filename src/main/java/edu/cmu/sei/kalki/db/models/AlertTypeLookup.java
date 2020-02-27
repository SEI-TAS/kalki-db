package edu.cmu.sei.kalki.db.models;

import edu.cmu.sei.kalki.db.daos.AlertTypeLookupDAO;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.postgresql.util.HStoreConverter;

public class AlertTypeLookup {
    private int id;
    private int alertTypeId;
    private int deviceTypeId;
    private Map<String, String> variables;

    private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    public AlertTypeLookup() { }

    public AlertTypeLookup(int alertTypeId, int deviceTypeId, Map<String, String> variables) {
        this.alertTypeId = alertTypeId;
        this.deviceTypeId = deviceTypeId;
        this.variables = variables;
    }

    public AlertTypeLookup(int id, int alertTypeId, int deviceTypeId, Map<String, String> variables) {
        this(alertTypeId, deviceTypeId, variables);
        this.id = id;
    }

    /**
     * Extract an AlertTypeLookup from the result set of a database query.
     */
    public static AlertTypeLookup createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) return null;
        int id = rs.getInt("id");
        int alertTypeId = rs.getInt("alert_type_id");
        int deviceTypeId = rs.getInt("device_type_id");
        Map<String, String> variables = null;
        if (rs.getString("variables") != null) {
            variables = HStoreConverter.fromString(rs.getString("variables"));
        }
        return new AlertTypeLookup(id, alertTypeId, deviceTypeId, variables);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAlertTypeId() {
        return alertTypeId;
    }

    public void setAlertTypeId(int alertTypeId) {
        this.alertTypeId = alertTypeId;
    }

    public int getDeviceTypeId() {
        return deviceTypeId;
    }

    public void setDeviceTypeId(int deviceTypeId) {
        this.deviceTypeId = deviceTypeId;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }

    public int insert() {
        setId(AlertTypeLookupDAO.insertAlertTypeLookup(this));
        return getId();
    }

    public int insertOrUpdate() {
        setId(AlertTypeLookupDAO.insertOrUpdateAlertTypeLookup(this));
        return getId();
    }

    public String toString() {
        try {
            return ow.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "Bad AlertTypeLookup";
        }
    }
}
