package edu.cmu.sei.ttg.kalki.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import edu.cmu.sei.ttg.kalki.database.Postgres;
import edu.cmu.sei.ttg.kalki.models.*;

import edu.cmu.sei.ttg.kalki.database.AUsesDatabase;

public class PolicyTest extends AUsesDatabase {
    private static Policy policy;

    @Test
    public void testFindPolicyById(){
        policy = new Policy(1, 1, 1, 1);
        policy.insert();

        Policy test = Postgres.findPolicy(policy.getId());
        assertEquals(policy.toString(), test.toString());
    }

    @Test
    public void testFindPolicyByStateDevCond() {
        policy = new Policy(1, 1, 1, 1);
        policy.insert();

        Policy test = Postgres.findPolicy(1, 1, 1);
        assertEquals(policy.toString(), test.toString());
    }

    @Test
    public void testInsertPolicy(){
        policy = new Policy(1, 1, 1, 1);
        policy.insert();

        assertEquals(1, policy.getId());
    }

    @Test
    public void testDeletePolicy(){
        policy = new Policy(1, 1, 1, 1);
        policy.insert();

        boolean success = Postgres.deletePolicy(policy.getId());
        assertEquals(true, success);
    }

    @Test
    public void testUpdatePolicy(){
        policy = new Policy(1, 1, 1, 1);
        policy.insert();
        assertEquals(1, policy.getSamplingRate());

        policy.setSamplingRate(2);
        Postgres.updatePolicy(policy);

        assertEquals(2, policy.getSamplingRate());
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
