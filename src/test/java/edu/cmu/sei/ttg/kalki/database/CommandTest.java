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

public class CommandTest extends AUsesDatabase {
    private static SecurityState securityState;
    private static DeviceType deviceType;
    private static DeviceType deviceTypeTwo;
    private static DeviceCommand deviceCommand;
    private static DeviceCommand deviceCommandLookup;

    @Before
    public void resetDB() {
        Postgres.resetDatabase();
        insertData();
    }

    /*
        Command Action Tests
     */

    /* finish after tables are updated

    @Test
    public void testFindAllCommands() {
        assertEquals(Postgres.findAllCommands().size(), 2);
    }

    @Test
    public void testFindCommandLookup() {
        assertEquals(Postgres.findCommandLookup(deviceCommandLookup.getLookupId()).toString(),
                deviceCommandLookup.toString());
    }

    @Test
    public void testFindAllCommandLookups() {
        ArrayList<DeviceCommand> foundLookups = new ArrayList<DeviceCommand>(Postgres.findAllCommandLookups());

        assertEquals(2, foundLookups.size());   //1 from setupDatabase and 1 from insert data
        assertEquals(deviceCommandLookup.toString(), foundLookups.get(1).toString());
    }

    @Test
    public void testFindCommandsByDevice() {

    }

    @Test
    public void testInsertCommand() {

    }

    @Test
    public void testInsertOrUpdateCommandLookup() {

    }

    @Test
    public void testDeleteCommandLookup() {

    }
    */

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

//        deviceCommandLookup = new DeviceCommand(deviceType.getId(), securityState.getId(), deviceCommand.getId());
//        deviceCommandLookup.insertCommandLookup();
    }
}