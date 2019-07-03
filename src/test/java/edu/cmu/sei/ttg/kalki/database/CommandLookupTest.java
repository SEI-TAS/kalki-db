package edu.cmu.sei.ttg.kalki.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

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
    private static DeviceCommandLookup deviceCommandLookup;

    /*
        Command Lookup Action Tests
     */

    @Test
    public void testFindAllCommandLookups() {
        assertEquals(1, Postgres.findAllCommandLookups().size());
    }

    @Test
    public void testFindCommandLookup() {
        assertEquals(deviceCommandLookup.toString(),
                Postgres.findCommandLookup(deviceCommandLookup.getId()).toString());
    }

    @Test
    public void testFindCommandLookupByCommand() {
        assertEquals(deviceCommandLookup.toString(),
                Postgres.findCommandLookupByCommand(deviceCommand.getId()).toString());
    }

    @Test
    public void testInsertOrUpdateCommandLookup() {
        assertEquals(1, Postgres.findAllCommandLookups().size());

        deviceCommandLookup.setStateId(securityStateTwo.getId());
        deviceCommandLookup.insertOrUpdate();

        assertEquals(deviceCommandLookup.getStateId(),
                Postgres.findCommandLookup(deviceCommandLookup.getId()).getStateId());
        assertEquals(1, Postgres.findAllCommandLookups().size());

        DeviceCommandLookup newLookup =
                new DeviceCommandLookup(securityState.getId(), deviceCommand.getId());

        int newId = newLookup.insertOrUpdate();

        assertEquals(2, Postgres.findAllCommandLookups().size());
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

        deviceCommandLookup = new DeviceCommandLookup(securityState.getId(), deviceCommand.getId());
        deviceCommandLookup.insert();
    }
}