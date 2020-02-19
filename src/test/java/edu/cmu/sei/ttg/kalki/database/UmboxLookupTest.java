package edu.cmu.sei.ttg.kalki.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import edu.cmu.sei.ttg.kalki.models.*;

public class UmboxLookupTest extends AUsesDatabase {
    private static SecurityState securityState;
    private static SecurityState securityStateTwo;
    private static DeviceType deviceType;
    private static DeviceType deviceTypeTwo;
    private static Group group;
    private static Device device;
    private static Device deviceTwo;
    private static PolicyCondition policyCondition;
    private static PolicyRule policyRule;
    private static StateTransition stateTransition;
    private static UmboxImage umboxImage;
    private static UmboxLookup umboxLookup;

    /*
        test umbox lookup actions
     */

    @Test
    public void testFindUmboxLookup() {
        assertEquals(umboxLookup.toString(), Postgres.findUmboxLookup(umboxLookup.getId()).toString());
    }

    @Test
    public void testFindUmboxLookupsByDevice() {
        ArrayList<UmboxLookup> foundLookups =
                new ArrayList<UmboxLookup>(Postgres.findUmboxLookupsByDevice(device.getId()));

        assertEquals(1, foundLookups.size());
        assertEquals(umboxLookup.toString(), foundLookups.get(0).toString());
    }

    @Test
    public void testFindAllUmboxLookups() {
        assertEquals(1, Postgres.findAllUmboxLookups().size());
    }

    @Test
    public void testInsertOrUpdateUmboxLookup() {
        UmboxLookup newUmboxLookup = new UmboxLookup(policyRule.getId(), deviceType.getId(), umboxImage.getId(), 2);
        newUmboxLookup.insertOrUpdate();

        assertEquals(newUmboxLookup.toString(), Postgres.findUmboxLookup(newUmboxLookup.getId()).toString());

        umboxLookup.setDagOrder(2);
        umboxLookup.insertOrUpdate();

        assertEquals(umboxLookup.toString(), Postgres.findUmboxLookup(umboxLookup.getId()).toString());
    }

    @Test
    public void testDeleteUmboxLookup() {
        assertEquals(umboxLookup.toString(), Postgres.findUmboxLookup(umboxLookup.getId()).toString());
        Postgres.deleteUmboxLookup(umboxLookup.getId());
        assertEquals(null, Postgres.findUmboxLookup(umboxLookup.getId()));
    }

    public void insertData() {
        // insert security state(s)
        securityState = new SecurityState("Normal");
        securityState.insert();

        // insert security state(s)
        securityStateTwo = new SecurityState("Suspicious");
        securityStateTwo.insert();

        // insert state transition
        stateTransition = new StateTransition(securityState.getId(), securityStateTwo.getId());
        stateTransition.insert();

        // insert device_type
        deviceType = new DeviceType(0, "Udoo Neo");
        deviceType.insert();

        deviceTypeTwo = new DeviceType(0, "test device type");
        deviceTypeTwo.insert();

        // insert Group
        group = new Group("Test Group");
        group.insert();

        // insert device
        device = new Device("Device 1", "this is a test device", deviceType, "0.0.0.0", 1, 1);
        device.insert();

        deviceTwo = new Device("Device 2", "this is also a test device", deviceTypeTwo.getId(), group.getId(), "0.0.0.1", 1, 1, 1);
        deviceTwo.insert();

        // insert umbox_image
        umboxImage = new UmboxImage("UmboxImage", "path/to/image");
        umboxImage.insert();

        // insert policy condition
        policyCondition = new PolicyCondition(1, null);
        policyCondition.insert();

        // insert policy
        policyRule = new PolicyRule(stateTransition.getId(), policyCondition.getId(), deviceType.getId(), 1);
        policyRule.insert();

        // insert umbox_lookup (should be handle by umbox_image)
        umboxLookup = new UmboxLookup(policyRule.getId(), umboxImage.getId(), 1);
        umboxLookup.insertOrUpdate();

    }
}
