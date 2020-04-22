package edu.cmu.sei.kalki.db.database;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.cmu.sei.kalki.db.daos.PolicyRuleLogDAO;
import edu.cmu.sei.kalki.db.daos.StateTransitionDAO;
import edu.cmu.sei.kalki.db.models.DeviceType;
import edu.cmu.sei.kalki.db.models.PolicyCondition;
import edu.cmu.sei.kalki.db.models.PolicyRule;
import edu.cmu.sei.kalki.db.models.PolicyRuleLog;
import edu.cmu.sei.kalki.db.models.StateTransition;
import org.junit.jupiter.api.Test;

import edu.cmu.sei.kalki.db.models.*;

public class PolicyRuleLogTest extends AUsesDatabase {
    private DeviceType deviceType;
    private StateTransition stateTransition;
    private PolicyCondition policyCondition;
    private PolicyRule policyRule;
    private Device device;

    @Test
    public void findPolicyCondition() {
        PolicyRuleLog policyRuleLog = new PolicyRuleLog(policyRule.getId(), device.getId());
        policyRuleLog.insert();

        PolicyRuleLog test = PolicyRuleLogDAO.findPolicyRuleLog(policyRuleLog.getId());
        assertEquals(policyRuleLog.toString(), test.toString());
    }

    @Test
    public void testInsertPolicyRuleLog() {
        PolicyRuleLog policyRuleLog = new PolicyRuleLog(policyRule.getId(), device.getId());
        policyRuleLog.insert();

        assertEquals(1, policyRuleLog.getId());
    }

    @Test
    public void testDeletePolicyRuleLog() {
        PolicyRuleLog policyRuleLog = new PolicyRuleLog(policyRule.getId(), device.getId());
        policyRuleLog.insert();

        boolean success = PolicyRuleLogDAO.deletePolicyRuleLog(policyRuleLog.getId());
        assert success;

        PolicyRuleLog cond = PolicyRuleLogDAO.findPolicyRuleLog(policyRuleLog.getId());
        assertEquals(null, cond);
    }

    public void insertData() {
        deviceType = new DeviceType(-1, "Device Type");
        deviceType.insert();

        DataNode dataNode = new DataNode("Test Node", "localhost");
        dataNode.insert();

        device = new Device("Device 1", "this is a test device", deviceType, "0.0.0.0", 1, 1, dataNode);
        device.insert();

        stateTransition = new StateTransition(1, 2);
        StateTransitionDAO.insertStateTransition(stateTransition);

        policyCondition = new PolicyCondition(1, null);
        policyCondition.insert();

        policyRule = new PolicyRule(1,1,1,1);
        policyRule.insert();
    }
}
