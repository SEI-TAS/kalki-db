package edu.cmu.sei.kalki.db.database;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.cmu.sei.kalki.db.daos.PolicyConditionDAO;
import edu.cmu.sei.kalki.db.daos.PolicyRuleDAO;
import edu.cmu.sei.kalki.db.daos.StateTransitionDAO;
import edu.cmu.sei.kalki.db.models.DeviceType;
import edu.cmu.sei.kalki.db.models.PolicyCondition;
import edu.cmu.sei.kalki.db.models.PolicyRule;
import edu.cmu.sei.kalki.db.models.StateTransition;
import org.junit.jupiter.api.Test;

public class PolicyRuleTest extends AUsesDatabase {
    private static PolicyRule policyRule;

    private static int BASE_POLICIE_RULES = 2;

    @Test
    public void testFindPolicyById(){
        policyRule = new PolicyRule(1, 1, 1, 1);
        policyRule.insert();

        PolicyRule test = PolicyRuleDAO.findPolicyRule(policyRule.getId());
        assertEquals(policyRule.toString(), test.toString());
    }

    @Test
    public void testFindPolicyByStateDevCond() {
        policyRule = new PolicyRule(1, 1, 1, 1);
        policyRule.insert();

        PolicyRule test = PolicyRuleDAO.findPolicyRule(1, 1, 1);
        assertEquals(policyRule.toString(), test.toString());
    }

    @Test
    public void testInsertPolicy(){
        policyRule = new PolicyRule(1, 1, 1, 1);
        policyRule.insert();

        assertEquals(BASE_POLICIE_RULES + 1, policyRule.getId());
    }

    @Test
    public void testDeletePolicy(){
        policyRule = new PolicyRule(1, 1, 1, 1);
        policyRule.insert();

        boolean success = PolicyRuleDAO.deletePolicyRule(policyRule.getId());
        assertEquals(true, success);
    }

    @Test
    public void testUpdatePolicy(){
        policyRule = new PolicyRule(1, 1, 1, 1);
        policyRule.insert();
        assertEquals(1, policyRule.getSamplingRateFactor());

        policyRule.setSamplingRateFactor(2);
        PolicyRuleDAO.updatePolicyRule(policyRule);

        assertEquals(2, policyRule.getSamplingRateFactor());
    }

    public void insertData() {
        DeviceType deviceType = new DeviceType(-1, "Device Type");
        deviceType.insert();

        StateTransition stateTransition = new StateTransition(1, 2);
        StateTransitionDAO.insertStateTransition(stateTransition);

        PolicyCondition policyCondition = new PolicyCondition(1, null);
        PolicyConditionDAO.insertPolicyCondition(policyCondition);
    }
}
