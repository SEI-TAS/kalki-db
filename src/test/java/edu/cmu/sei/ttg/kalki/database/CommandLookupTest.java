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

public class CommandLookupTest extends AUsesDatabase {
    private static SecurityState securityState;
    private static SecurityState securityStateTwo;
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
        assertEquals(2, Postgres.findAllCommandLookups().size());
    }

    @Test
    public void testFindCommandLookupsByDevice() {
        ArrayList<DeviceCommandLookup> foundLookups =
                new ArrayList<DeviceCommandLookup>(Postgres.findCommandLookupsByDevice(device.getId()));

        assertEquals(1, foundLookups.size());
        assertEquals(deviceCommandLookup.toString(), foundLookups.get(0).toString());
    }

    @Test
    public void testFindCommandLookup() {
        assertEquals(deviceCommandLookup.toString(),
                Postgres.findCommandLookup(deviceCommandLookup.getId()).toString());
    }

    @Test
    public void testInsertOrUpdateCommandLookup() {
        assertEquals(2, Postgres.findAllCommandLookups().size());

        deviceCommandLookup.setCurrentStateId(securityStateTwo.getId());
        deviceCommandLookup.insertOrUpdate();

        assertEquals(deviceCommandLookup.getCurrentStateId(),
                Postgres.findCommandLookup(deviceCommandLookup.getId()).getCurrentStateId());
        assertEquals(2, Postgres.findAllCommandLookups().size());

        DeviceCommandLookup newLookup =
                new DeviceCommandLookup(deviceCommand.getId(), securityState.getId(), securityStateTwo.getId(), deviceType.getId());

        int newId = newLookup.insertOrUpdate();

        assertEquals(3, Postgres.findAllCommandLookups().size());
        assertEquals(newLookup.toString(), Postgres.findCommandLookup(newId).toString());
    }

    @Test
    public void testDeleteCommandLookup() {
        assertEquals(deviceCommandLookup.toString(),
                Postgres.findCommandLookup(deviceCommandLookup.getId()).toString());

        Postgres.deleteCommandLookup(deviceCommandLookup.getId());

        assertEquals(null, Postgres.findCommandLookup(deviceCommandLookup.getId()));
    }

    public void insertData() {
        // insert security state(s)
        securityState = new SecurityState("Normal");
        securityState.insert();

        securityStateTwo = new SecurityState("Suspicious");
        securityStateTwo.insert();

        // insert device_type
        deviceType = new DeviceType(0, "Udoo Neo");
        deviceType.insert();

        deviceTypeTwo = new DeviceType(0, "test device type");
        deviceTypeTwo.insert();

        // insert command_lookups
        deviceCommand = new DeviceCommand("Test Command", deviceType.getId());
        deviceCommand.insert();

        deviceCommandTwo = new DeviceCommand("Test Command two", deviceTypeTwo.getId());
        deviceCommandTwo.insert();

        deviceCommandLookup = new DeviceCommandLookup(deviceCommand.getId(), securityState.getId(), securityStateTwo.getId(), deviceType.getId());
        deviceCommandLookup.insert();

        deviceCommandLookupTwo = new DeviceCommandLookup(deviceCommandTwo.getId(), securityState.getId(), securityStateTwo.getId(), deviceType.getId());
        deviceCommandLookupTwo.insert();

        // insert device
        device = new Device("Device 1", "this is a test device", deviceType, "0.0.0.0", 1, 1);
        device.insert();
    }
}
