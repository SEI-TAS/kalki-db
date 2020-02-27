package edu.cmu.sei.kalki.db.models;

import edu.cmu.sei.kalki.db.daos.DeviceCommandLookupDAO;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DeviceCommandLookup {
    private int id;
    private int commandId;
    private int policyRuleId;

    private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    public DeviceCommandLookup() {
    }

    public DeviceCommandLookup(int commandId, int policyRuleId) {
        this.commandId = commandId;
        this.policyRuleId = policyRuleId;
    }

    public DeviceCommandLookup(int id, int commandId, int policyRuleId) {
        this(commandId, policyRuleId);
        this.id = id;
    }

    /**
     * Extract a Command from the result set of a database query.
     */
    public static DeviceCommandLookup createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) { return null; }
        int id = rs.getInt("id");
        int commandId = rs.getInt("command_id");
        int policyRuleId = rs.getInt("policy_rule_id");
        return new DeviceCommandLookup(id, commandId, policyRuleId);
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

    public int getPolicyRuleId() {
        return policyRuleId;
    }

    public void setPolicyRuleId(int policyRuleId) {
        this.policyRuleId = policyRuleId;
    }

    public int insert() {
        this.id = DeviceCommandLookupDAO.insertCommandLookup(this);
        return this.id;
    }

    public int insertOrUpdate() {
        this.id = DeviceCommandLookupDAO.insertOrUpdateCommandLookup(this);
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
