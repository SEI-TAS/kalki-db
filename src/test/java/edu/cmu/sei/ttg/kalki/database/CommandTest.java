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
    private static Device device;
    private static DeviceType deviceType;
    private static DeviceType deviceTypeTwo;
    private static DeviceCommand deviceCommand;
    private static DeviceCommandLookup deviceCommandLookup;
    private static DeviceSecurityState deviceSecurityState;

    /*
        Command Action Tests
     */

    @Test
    public void testFindCommand() {
        assertEquals(deviceCommand.toString(), Postgres.findCommand(deviceCommand.getId()).toString());
    }

    @Test
    public void testFindAllCommands() {
        assertEquals(1, Postgres.findAllCommands().size());
    }

    @Test
    public void testFindCommandsByDevice() {
        ArrayList<DeviceCommand> foundCommands = new ArrayList<DeviceCommand>(Postgres.findCommandsByDevice(device));

        assertEquals(1, foundCommands.size());
        assertEquals(deviceCommand.toString(), foundCommands.get(0).toString());
    }

    @Test
    public void testInsertOrUpdateCommand() {
        assertEquals(1, Postgres.findAllCommands().size());

        deviceCommand.setName("new command");
        deviceCommand.insertOrUpdate();

        assertEquals(deviceCommand.getName(), Postgres.findCommand(deviceCommand.getId()).getName());
        assertEquals(1, Postgres.findAllCommands().size());

        DeviceCommand newCommand = new DeviceCommand("new command 2", deviceType.getId());

        int newId = newCommand.insertOrUpdate();

        assertEquals(2, Postgres.findAllCommands().size());
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
        // insert security state(s)
        securityState = new SecurityState("Normal");
        securityState.insert();

        // insert device_type
        deviceType = new DeviceType(0, "Udoo Neo");
        deviceType.insert();

        // insert device
        device = new Device("Device 1", "this is a test device", deviceType, "0.0.0.0", 1, 1);
        device.insert();

        // insert command
        deviceCommand = new DeviceCommand("Test Command", deviceType.getId());
        deviceCommand.insert();

        deviceCommandLookup = new DeviceCommandLookup(securityState.getId(), deviceCommand.getId());
        deviceCommandLookup.insert();

        // insert device_security_state
        deviceSecurityState = new DeviceSecurityState(device.getId(), securityState.getId());
        deviceSecurityState.insert();

        device.setCurrentState(deviceSecurityState);
    }
}
