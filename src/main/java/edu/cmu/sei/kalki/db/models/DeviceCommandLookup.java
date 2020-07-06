package edu.cmu.sei.kalki.db.models;

import edu.cmu.sei.kalki.db.daos.DeviceCommandLookupDAO;

import java.util.ArrayList;
import java.util.List;

public class DeviceCommandLookup extends Model  {
    private int commandId;
    private int policyRuleId;
    private List<Integer> commandIds;

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

    public DeviceCommandLookup(List<Integer> commandIds) {
        this.commandIds = commandIds;
    }

    public DeviceCommandLookup(List<Integer> commandIds, int policyRuleId) {
        this.commandIds = commandIds;
        this.policyRuleId = policyRuleId;
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

    public List<Integer> getCommandIds() {
        return this.commandIds;
    }

    public void setCommandIds(List<Integer> commandIds) {
        this.commandIds = commandIds;
    }

    public int insert() {
        this.id = DeviceCommandLookupDAO.insertCommandLookup(this);
        return this.id;
    }

    public List<Integer> insertMultiple() {
        List<Integer> lookupIds = new ArrayList<>();
        if (this.commandIds != null && this.commandIds.size() > 0) {
            for (int id : this.commandIds) {
                setCommandId(id);
                lookupIds.add(DeviceCommandLookupDAO.insertCommandLookup(this));
            }
        } else {
            lookupIds.add(-1);
        }
        return lookupIds;
    }

    public int insertOrUpdate() {
        this.id = DeviceCommandLookupDAO.insertOrUpdateCommandLookup(this);
        return this.id;
    }
}
