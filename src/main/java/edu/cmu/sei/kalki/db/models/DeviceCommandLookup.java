package edu.cmu.sei.kalki.db.models;

import edu.cmu.sei.kalki.db.daos.DeviceCommandLookupDAO;

public class DeviceCommandLookup extends Model  {
    private int id;
    private int commandId;
    private int policyRuleId;

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
}
