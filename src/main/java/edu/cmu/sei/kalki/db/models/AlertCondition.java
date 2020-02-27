package edu.cmu.sei.kalki.db.models;

import edu.cmu.sei.kalki.db.daos.AlertConditionDAO;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.postgresql.util.HStoreConverter;

public class AlertCondition {
    private int id;
    private Integer deviceId;
    private String deviceName;
    private Integer alertTypeLookupId;
    private String alertTypeName;
    private Map<String, String> variables;

    private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    public AlertCondition() {
    }

    public AlertCondition(Integer deviceId, Integer alertTypeLookupId, Map<String, String> variables) {
        this.deviceId = deviceId;
        this.alertTypeLookupId = alertTypeLookupId;
        this.variables = variables;
    }

    public AlertCondition(Integer deviceId, String deviceName, Integer alertTypeLookupId, String alertTypeName, Map<String, String> variables) {
        this(deviceId, alertTypeLookupId, variables);
        this.deviceName = deviceName;
        this.alertTypeName = alertTypeName;
    }

    public AlertCondition(int id, Integer deviceId, String deviceName, Integer alertTypeLookupId, String alertTypeName, Map<String, String> variables) {
        this(deviceId, deviceName, alertTypeLookupId, alertTypeName, variables);
        this.id = id;
    }

    /**
     * Extract an AlertCondition from the result set of a database query.
     */
    public static AlertCondition createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) return null;
        int id = rs.getInt("id");
        int deviceId = rs.getInt("device_id");
        String deviceName = rs.getString("device_name");
        int alertTypeLookupId = rs.getInt("alert_type_lookup_id");
        String alertTypeName = rs.getString("alert_type_name");
        Map<String, String> variables = null;
        if (rs.getString("variables") != null) {
            variables = HStoreConverter.fromString(rs.getString("variables"));
        }
        return new AlertCondition(id, deviceId, deviceName, alertTypeLookupId, alertTypeName, variables);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Integer deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() { return this.deviceName; }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public Integer getAlertTypeLookupId() {
        return alertTypeLookupId;
    }

    public void setAlertTypeLookupId(Integer alertTypeLookupId) {
        this.alertTypeLookupId = alertTypeLookupId;
    }

    public String getAlertTypeName() {
        return alertTypeName;
    }

    public void setAlertTypeName(String alertTypeName) {
        this.alertTypeName = alertTypeName;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }

    public Integer insertOrUpdate() {
        setId(AlertConditionDAO.insertAlertCondition(this));
        return getId();
    }

    public String toString() {
        try {
            return ow.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "Bad AlertCondition";
        }
    }
}
