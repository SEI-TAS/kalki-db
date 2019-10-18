package edu.cmu.sei.ttg.kalki.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import edu.cmu.sei.ttg.kalki.database.Postgres;
import edu.cmu.sei.ttg.kalki.models.*;

import edu.cmu.sei.ttg.kalki.database.AUsesDatabase;

public class CommandTest extends AUsesDatabase {
    private static SecurityState securityState;
    private static SecurityState securityStateTwo;
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

    /*
        Command Action Tests
     */

    @Test
    public void testFindCommand() {
        assertEquals(deviceCommand.toString(), Postgres.findCommand(deviceCommand.getId()).toString());
    }

    @Test
    public void testFindAllCommands() {
        assertEquals(2, Postgres.findAllCommands().size());
    }

    @Test
    public void testFindCommandsByDevice() {
        device.setCurrentState(deviceSecurityStateTwo);
        device.insertOrUpdate();

        ArrayList<DeviceCommand> foundCommands = new ArrayList<DeviceCommand>(Postgres.findCommandsByDevice(device));

        assertEquals(1, foundCommands.size());
        assertEquals(deviceCommand.toString(), foundCommands.get(0).toString());
    }

    @Test
    public void testFindCommandsForGroup() {
        device.setCurrentState(deviceSecurityStateTwo);
        device.insertOrUpdate();

        ArrayList<DeviceCommand> foundCommands = new ArrayList<DeviceCommand>(Postgres.findCommandsForGroup(deviceTwo, device));

        assertEquals(1, foundCommands.size());
        assertEquals(deviceCommandTwo.toString(), foundCommands.get(0).toString());
    }

    @Test
    public void testInsertOrUpdateCommand() {
        assertEquals(2, Postgres.findAllCommands().size());

        deviceCommand.setName("new command");
        deviceCommand.insertOrUpdate();

        assertEquals(deviceCommand.getName(), Postgres.findCommand(deviceCommand.getId()).getName());
        assertEquals(2, Postgres.findAllCommands().size());

        DeviceCommand newCommand = new DeviceCommand("new command 2", deviceType.getId());

        int newId = newCommand.insertOrUpdate();

        assertEquals(3, Postgres.findAllCommands().size());
        assertEquals(newCommand.toString(), Postgres.findCommand(newId).toString());
    }

    @Test
    public void testDeleteCommand() {
        assertEquals(deviceCommand.toString(), Postgres.findCommand(deviceCommand.getId()).toString());

        Postgres.deleteCommandLookup(deviceCommandLookup.getId());
        Postgres.deleteCommand(deviceCommand.getId());

        assertEquals(null, Postgres.findCommand(deviceCommand.getId()));
    }

    public void insertData() {
        securityState = Postgres.findSecurityState(1);
        securityStateTwo = Postgres.findSecurityState(2);

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

        // insert command
        deviceCommand = new DeviceCommand("Test Command", deviceType.getId());
        deviceCommand.insert();

        deviceCommandTwo = new DeviceCommand("Second Command", deviceTypeTwo.getId());
        deviceCommandTwo.insert();

        deviceCommandLookup = new DeviceCommandLookup(deviceCommand.getId(), securityStateTwo.getId(), securityState.getId(), deviceType.getId());
        deviceCommandLookup.insert();

        deviceCommandLookupTwo = new DeviceCommandLookup(deviceCommandTwo.getId(), securityStateTwo.getId(), securityState.getId(), deviceType.getId());
        deviceCommandLookupTwo.insert();
    }
}
