package edu.cmu.sei.kalki.db.database;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.cmu.sei.kalki.db.daos.DeviceCommandLookupDAO;
import edu.cmu.sei.kalki.db.models.DataNode;
import edu.cmu.sei.kalki.db.models.Device;
import edu.cmu.sei.kalki.db.models.DeviceCommand;
import edu.cmu.sei.kalki.db.models.DeviceCommandLookup;
import edu.cmu.sei.kalki.db.models.DeviceType;
import edu.cmu.sei.kalki.db.models.PolicyCondition;
import edu.cmu.sei.kalki.db.models.PolicyRule;
import edu.cmu.sei.kalki.db.models.SecurityState;
import edu.cmu.sei.kalki.db.models.StateTransition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class CommandLookupTest extends AUsesDatabase {
    private static SecurityState securityState;
    private static SecurityState securityStateTwo;
    private static StateTransition stateTransition;
    private static StateTransition stateTransitionTwo;
    private static PolicyCondition policyCondition;
    private static PolicyRule policyRuleOne;
    private static PolicyRule policyRuleTwo;
    private static DeviceType deviceType;
    private static DeviceType deviceTypeTwo;
    private static DeviceCommand deviceCommand;
    private static DeviceCommand deviceCommandTwo;
    private static DeviceCommandLookup deviceCommandLookup;
    private static DeviceCommandLookup deviceCommandLookupTwo;
    private static Device device;

    /*
        Command Lookup Action Tests
     */

    @Test
    public void testFindAllCommandLookups() {
        assertEquals(2, DeviceCommandLookupDAO.findAllCommandLookups().size());
    }

    @Test
    public void testFindCommandLookupsByDevice() {
        ArrayList<DeviceCommandLookup> foundLookups =
                new ArrayList<DeviceCommandLookup>(DeviceCommandLookupDAO.findCommandLookupsByDevice(device.getId()));

        assertEquals(1, foundLookups.size());
        assertEquals(deviceCommandLookup.toString(), foundLookups.get(0).toString());
    }

    @Test
    public void testFindCommandLookup() {
        Assertions.assertEquals(deviceCommandLookup.toString(),
                DeviceCommandLookupDAO.findCommandLookup(deviceCommandLookup.getId()).toString());
    }

    @Test
    public void testInsertOrUpdateCommandLookup() {
        assertEquals(2, DeviceCommandLookupDAO.findAllCommandLookups().size());

        deviceCommandLookup.setPolicyRuleId(policyRuleTwo.getId());
        deviceCommandLookup.insertOrUpdate();

        Assertions.assertEquals(deviceCommandLookup.getPolicyRuleId(),
                DeviceCommandLookupDAO.findCommandLookup(deviceCommandLookup.getId()).getPolicyRuleId());
        assertEquals(2, DeviceCommandLookupDAO.findAllCommandLookups().size());

        DeviceCommandLookup newLookup =
                new DeviceCommandLookup(deviceCommand.getId(), policyRuleOne.getId());

        int newId = newLookup.insertOrUpdate();

        assertEquals(3, DeviceCommandLookupDAO.findAllCommandLookups().size());
        Assertions.assertEquals(newLookup.toString(), DeviceCommandLookupDAO.findCommandLookup(newId).toString());
    }

    @Test
    public void testDeleteCommandLookup() {
        Assertions.assertEquals(deviceCommandLookup.toString(),
                DeviceCommandLookupDAO.findCommandLookup(deviceCommandLookup.getId()).toString());

        DeviceCommandLookupDAO.deleteCommandLookup(deviceCommandLookup.getId());

        Assertions.assertEquals(null, DeviceCommandLookupDAO.findCommandLookup(deviceCommandLookup.getId()));
    }

    public void insertData() {
        // insert security state(s)
        securityState = new SecurityState("Normal");
        securityState.insert();

        securityStateTwo = new SecurityState("Suspicious");
        securityStateTwo.insert();

        stateTransition = new StateTransition(securityState.getId(), securityStateTwo.getId());
        stateTransition.insert();

        stateTransitionTwo  = new StateTransition(securityStateTwo.getId(), securityState.getId());
        stateTransitionTwo.insert();

        // insert device_type
        deviceType = new DeviceType(0, "Udoo Neo");
        deviceType.insert();

        deviceTypeTwo = new DeviceType(0, "test device type");
        deviceTypeTwo.insert();

        // insert policy related objects
        policyCondition = new PolicyCondition(1, null);
        policyCondition.insert();

        policyRuleOne = new PolicyRule(stateTransition.getId(), policyCondition.getId(), deviceType.getId(), 1);
        policyRuleOne.insert();

        policyRuleTwo = new PolicyRule(stateTransitionTwo.getId(), policyCondition.getId(), deviceType.getId(), 1);
        policyRuleTwo.insert();

        // insert command_lookups
        deviceCommand = new DeviceCommand("Test Command", deviceType.getId());
        deviceCommand.insert();

        deviceCommandTwo = new DeviceCommand("Test Command two", deviceTypeTwo.getId());
        deviceCommandTwo.insert();

        deviceCommandLookup = new DeviceCommandLookup(deviceCommand.getId(), policyRuleOne.getId());
        deviceCommandLookup.insert();

        deviceCommandLookupTwo = new DeviceCommandLookup(deviceCommandTwo.getId(), policyRuleOne.getId());
        deviceCommandLookupTwo.insert();

        DataNode dataNode = new DataNode("Test Node", "localhost");
        dataNode.insert();

        // insert device
        device = new Device("Device 1", "this is a test device", deviceType, "0.0.0.0", 1, 1, dataNode);
        device.insert();
    }
}
