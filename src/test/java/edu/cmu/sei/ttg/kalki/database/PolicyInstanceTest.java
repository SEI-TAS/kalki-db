package edu.cmu.sei.ttg.kalki.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import edu.cmu.sei.ttg.kalki.database.Postgres;
import edu.cmu.sei.ttg.kalki.models.*;

import edu.cmu.sei.ttg.kalki.database.AUsesDatabase;

public class PolicyInstanceTest extends AUsesDatabase {
    private DeviceType deviceType;
    private StateTransition stateTransition;
    private PolicyCondition policyCondition;
    private Policy policy;

    @Test
    public void findPolicyCondition() {
        PolicyInstance instance = new PolicyInstance(policy.getId());
        instance.insert();

        PolicyInstance test = Postgres.findPolicyInstance(instance.getId());
        assertEquals(instance.toString(), test.toString());
    }

    @Test
    public void testInsertPolicyInstance() {
        PolicyInstance instance = new PolicyInstance(policy.getId());
        instance.insert();

        assertEquals(1, instance.getId());
    }

    @Test
    public void testDeletePolicyInstance() {
        PolicyInstance instance = new PolicyInstance(policy.getId());
        instance.insert();

        boolean success = Postgres.deletePolicyInstance(instance.getId());
        assert success;

        PolicyInstance cond = Postgres.findPolicyInstance(instance.getId());
        assertEquals(null, cond);
    }

    public void insertData() {
        deviceType = new DeviceType(-1, "Device Type");
        deviceType.insert();

        stateTransition = new StateTransition(1, 2);
        Postgres.insertStateTransition(stateTransition);

        policyCondition = new PolicyCondition(1, null);
        policyCondition.insert();

        policy = new Policy(1,1,1,1);
        policy.insert();
    }
}
