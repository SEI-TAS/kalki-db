package edu.cmu.sei.kalki.db.database;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.cmu.sei.kalki.db.daos.DeviceCommandDAO;
import edu.cmu.sei.kalki.db.daos.DeviceCommandLookupDAO;
import edu.cmu.sei.kalki.db.daos.SecurityStateDAO;
import edu.cmu.sei.kalki.db.models.Device;
import edu.cmu.sei.kalki.db.models.DeviceCommand;
import edu.cmu.sei.kalki.db.models.DeviceCommandLookup;
import edu.cmu.sei.kalki.db.models.DeviceSecurityState;
import edu.cmu.sei.kalki.db.models.DeviceType;
import edu.cmu.sei.kalki.db.models.PolicyCondition;
import edu.cmu.sei.kalki.db.models.PolicyRule;
import edu.cmu.sei.kalki.db.models.PolicyRuleLog;
import edu.cmu.sei.kalki.db.models.SecurityState;
import edu.cmu.sei.kalki.db.models.StateTransition;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class CommandTest extends AUsesDatabase {
    private static SecurityState securityState;
    private static SecurityState securityStateTwo;
    private static StateTransition stateTransition;
    private static Device device;
    private static Device deviceTwo;
    private static DeviceType deviceType;
    private static DeviceType deviceTypeTwo;
    private static DeviceCommand deviceCommand;
    private static DeviceCommand deviceCommandTwo;
    private static DeviceCommandLookup deviceCommandLookup;
    private static DeviceCommandLookup deviceCommandLookupTwo;
    private static DeviceSecurityState deviceSecurityState;
    private static DeviceSecurityState deviceSecurityStateTwo;
    private static PolicyCondition policyCondition;
    private static PolicyRule policyRule;
    private static PolicyRuleLog policyRuleLog;

    /*
        Command Action Tests
     */

    @Test
    public void testFindCommand() {
        assertEquals(deviceCommand.toString(), DeviceCommandDAO.findCommand(deviceCommand.getId()).toString());
    }

    @Test
    public void testFindAllCommands() {
        assertEquals(2, DeviceCommandDAO.findAllCommands().size());
    }

    @Test
    public void testFindCommandsByPolicyRuleLog() {
        device.setCurrentState(deviceSecurityStateTwo);
        device.insertOrUpdate();

        ArrayList<DeviceCommand> foundCommands = new ArrayList<DeviceCommand>(DeviceCommandDAO.findCommandsByPolicyRuleLog(policyRuleLog.getId()));

        assertEquals(2, foundCommands.size());
        assertEquals(deviceCommand.toString(), foundCommands.get(0).toString());
    }

    @Test
    public void testInsertOrUpdateCommand() {
        assertEquals(2, DeviceCommandDAO.findAllCommands().size());

        deviceCommand.setName("new command");
        deviceCommand.insertOrUpdate();

        assertEquals(deviceCommand.getName(), DeviceCommandDAO.findCommand(deviceCommand.getId()).getName());
        assertEquals(2, DeviceCommandDAO.findAllCommands().size());

        DeviceCommand newCommand = new DeviceCommand("new command 2", deviceType.getId());

        int newId = newCommand.insertOrUpdate();

        assertEquals(3, DeviceCommandDAO.findAllCommands().size());
        assertEquals(newCommand.toString(), DeviceCommandDAO.findCommand(newId).toString());
    }

    @Test
    public void testDeleteCommand() {
        assertEquals(deviceCommand.toString(), DeviceCommandDAO.findCommand(deviceCommand.getId()).toString());

        DeviceCommandLookupDAO.deleteCommandLookup(deviceCommandLookup.getId());
        DeviceCommandDAO.deleteCommand(deviceCommand.getId());

        assertEquals(null, DeviceCommandDAO.findCommand(deviceCommand.getId()));
    }

    public void insertData() {
        securityState = SecurityStateDAO.findSecurityState(1);
        securityStateTwo = SecurityStateDAO.findSecurityState(2);
        stateTransition = new StateTransition(securityState.getId(), securityStateTwo.getId());
        stateTransition.insert();

        // insert device_type
        deviceType = new DeviceType(0, "Udoo Neo");
        deviceType.insert();

        deviceTypeTwo = new DeviceType(0, "DLink Camera");
        deviceTypeTwo.insert();

        // insert device
        device = new Device("Device 1", "this is a test device", deviceType, "0.0.0.0", 1, 1);
        device.insert();

        deviceTwo = new Device("Device 2", "", deviceTypeTwo, "0.0.0.1", 1, 1);
        deviceTwo.insert();

        deviceSecurityState = device.getCurrentState();

        deviceSecurityStateTwo = new DeviceSecurityState(device.getId(), 2);
        deviceSecurityStateTwo.insert();

        // insert policy
        policyCondition = new PolicyCondition(1, null);
        policyCondition.insert();

        policyRule = new PolicyRule(stateTransition.getId(), policyCondition.getId(), deviceType.getId(), 1);
        policyRule.insert();

        policyRuleLog = new PolicyRuleLog(policyRule.getId(), device.getId());
        policyRuleLog.insert();

        // insert command
        deviceCommand = new DeviceCommand("Test Command", deviceType.getId());
        deviceCommand.insert();

        deviceCommandTwo = new DeviceCommand("Second Command", deviceTypeTwo.getId());
        deviceCommandTwo.insert();

        deviceCommandLookup = new DeviceCommandLookup(deviceCommand.getId(), policyRule.getId());
        deviceCommandLookup.insert();

        deviceCommandLookupTwo = new DeviceCommandLookup(deviceCommandTwo.getId(), policyRule.getId());
        deviceCommandLookupTwo.insert();
    }
}
