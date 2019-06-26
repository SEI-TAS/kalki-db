package edu.cmu.sei.ttg.kalki.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.Before;

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
    private static DeviceType deviceType;
    private static DeviceType deviceTypeTwo;
    private static DeviceCommand deviceCommand;
    private static DeviceCommandLookup deviceCommandLookup;

    @Before
    public void resetDB() {
        Postgres.resetDatabase();
        insertData();
    }

    /*
        Command Lookup Action Tests
     */

    @Test
    public void testFindAllCommandLookups() {
        assertEquals(Postgres.findAllCommandLookups().size(), 2);
    }

    @Test
    public void testFindCommandLookup() {
        assertEquals(Postgres.findCommandLookup(deviceCommandLookup.getId()).toString(),
                deviceCommandLookup.toString());
    }

    @Test
    public void testInsertOrUpdateCommandLookup() {
        assertEquals(2, Postgres.findAllCommandLookups().size());

        deviceCommandLookup.setDeviceTypeId(deviceTypeTwo.getId());
        deviceCommandLookup.insertOrUpdate();

        assertEquals(deviceCommandLookup.getDeviceTypeId(),
                Postgres.findCommandLookup(deviceCommandLookup.getId()).getDeviceTypeId());
        assertEquals(2, Postgres.findAllCommandLookups().size());

        DeviceCommandLookup newLookup =
                new DeviceCommandLookup(deviceType.getId(), securityState.getId(), deviceCommand.getId());

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

    private static void insertData() {
        // insert security state(s)
        securityState = new SecurityState("Normal");
        securityState.insert();

        // insert device_type
        deviceType = new DeviceType(0, "Udoo Neo");
        deviceType.insert();

        deviceTypeTwo = new DeviceType(0, "test device type");
        deviceTypeTwo.insert();

        // insert command_lookups
        deviceCommand = new DeviceCommand("Test Command");
        deviceCommand.insert();

        deviceCommandLookup = new DeviceCommandLookup(deviceType.getId(), securityState.getId(), deviceCommand.getId());
        deviceCommandLookup.insert();
    }
}