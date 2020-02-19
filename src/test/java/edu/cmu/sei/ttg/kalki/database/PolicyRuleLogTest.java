package edu.cmu.sei.ttg.kalki.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import edu.cmu.sei.ttg.kalki.models.*;

public class PolicyRuleLogTest extends AUsesDatabase {
    private DeviceType deviceType;
    private StateTransition stateTransition;
    private PolicyCondition policyCondition;
    private PolicyRule policyRule;

    @Test
    public void findPolicyCondition() {
        PolicyRuleLog policyRuleLog = new PolicyRuleLog(policyRule.getId());
        policyRuleLog.insert();

        PolicyRuleLog test = Postgres.findPolicyRuleLog(policyRuleLog.getId());
        assertEquals(policyRuleLog.toString(), test.toString());
    }

    @Test
    public void testInsertPolicyRuleLog() {
        PolicyRuleLog policyRuleLog = new PolicyRuleLog(policyRule.getId());
        policyRuleLog.insert();

        assertEquals(1, policyRuleLog.getId());
    }

    @Test
    public void testDeletePolicyRuleLog() {
        PolicyRuleLog policyRuleLog = new PolicyRuleLog(policyRule.getId());
        policyRuleLog.insert();

        boolean success = Postgres.deletePolicyRuleLog(policyRuleLog.getId());
        assert success;

        PolicyRuleLog cond = Postgres.findPolicyRuleLog(policyRuleLog.getId());
        assertEquals(null, cond);
    }

    public void insertData() {
        deviceType = new DeviceType(-1, "Device Type");
        deviceType.insert();

        stateTransition = new StateTransition(1, 2);
        Postgres.insertStateTransition(stateTransition);

        policyCondition = new PolicyCondition(1, null);
        policyCondition.insert();

        policyRule = new PolicyRule(1,1,1,1);
        policyRule.insert();
    }
}
