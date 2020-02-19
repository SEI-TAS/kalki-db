package edu.cmu.sei.ttg.kalki.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import edu.cmu.sei.ttg.kalki.models.*;

public class PolicyRuleTest extends AUsesDatabase {
    private static PolicyRule policyRule;

    @Test
    public void testFindPolicyById(){
        policyRule = new PolicyRule(1, 1, 1, 1);
        policyRule.insert();

        PolicyRule test = Postgres.findPolicyRule(policyRule.getId());
        assertEquals(policyRule.toString(), test.toString());
    }

    @Test
    public void testFindPolicyByStateDevCond() {
        policyRule = new PolicyRule(1, 1, 1, 1);
        policyRule.insert();

        PolicyRule test = Postgres.findPolicyRule(1, 1, 1);
        assertEquals(policyRule.toString(), test.toString());
    }

    @Test
    public void testInsertPolicy(){
        policyRule = new PolicyRule(1, 1, 1, 1);
        policyRule.insert();

        assertEquals(1, policyRule.getId());
    }

    @Test
    public void testDeletePolicy(){
        policyRule = new PolicyRule(1, 1, 1, 1);
        policyRule.insert();

        boolean success = Postgres.deletePolicyRule(policyRule.getId());
        assertEquals(true, success);
    }

    @Test
    public void testUpdatePolicy(){
        policyRule = new PolicyRule(1, 1, 1, 1);
        policyRule.insert();
        assertEquals(1, policyRule.getSamplingRate());

        policyRule.setSamplingRate(2);
        Postgres.updatePolicyRule(policyRule);

        assertEquals(2, policyRule.getSamplingRate());
    }

    public void insertData() {
        DeviceType deviceType = new DeviceType(-1, "Device Type");
        deviceType.insert();

        StateTransition stateTransition = new StateTransition(1, 2);
        Postgres.insertStateTransition(stateTransition);

        PolicyCondition policyCondition = new PolicyCondition(1, null);
        Postgres.insertPolicyCondition(policyCondition);
    }
}
