package edu.cmu.sei.kalki.db.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import edu.cmu.sei.kalki.db.models.AlertType;
import edu.cmu.sei.kalki.db.models.PolicyCondition;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;

import edu.cmu.sei.kalki.db.models.*;

public class PolicyConditionTest extends AUsesDatabase {
    private AlertType alertType;

    @Test
    public void findPolicyCondition() {
        PolicyCondition policyCondition = new PolicyCondition(1, new ArrayList<Integer>());
        policyCondition.insert();

        PolicyCondition test = Postgres.findPolicyCondition(policyCondition.getId());
        assertEquals(policyCondition.toString(), test.toString());
    }

    @Test
    public void testInsertPolicyCondition() {
        PolicyCondition policyCondition = new PolicyCondition(1, new ArrayList<Integer>());
        policyCondition.insert();

        assertEquals(1, policyCondition.getId());
    }

    @Test
    public void testUpdatePolicyCondition() {
        List<Integer> alertTypeIds = new ArrayList<Integer>();
        PolicyCondition policyCondition = new PolicyCondition(1, alertTypeIds);
        policyCondition.insert();

        alertTypeIds.add(alertType.getId());
        policyCondition.setAlertTypeIds(alertTypeIds);
        policyCondition.setThreshold(2);

        Postgres.updatePolicyCondition(policyCondition);

        assertEquals(1, policyCondition.getAlertTypeIds().size());
        assertEquals((int)alertType.getId(), (int)policyCondition.getAlertTypeIds().get(0));
        assertEquals(2, policyCondition.getThreshold());
    }

    @Test
    public void testDeletePolicyCondition() {
        PolicyCondition policyCondition = new PolicyCondition(1, null);
        policyCondition.insert();

        boolean success = Postgres.deletePolicyCondition(policyCondition.getId());
        assert success;

        PolicyCondition cond = Postgres.findPolicyCondition(policyCondition.getId());
        assertEquals(null, cond);
    }
    public void insertData() {
        alertType = new AlertType("name", "description", "source");
        alertType.insert();

    }
}
