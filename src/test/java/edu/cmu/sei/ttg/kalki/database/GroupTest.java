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

public class GroupTest extends AUsesDatabase {
    private static Group group;

    @Before
    public void resetDB() {
        Postgres.resetDatabase();
        insertData();
    }

    /*
        Test Group Actions
     */

    @Test
    public void testFindGroup() {
        assertEquals(group.toString(), Postgres.findGroup(group.getId()).toString());
    }

    @Test
    public void testFindAllGroups() {
        assertEquals(1, Postgres.findAllGroups().size());
    }

    @Test
    public void testInsertOrUpdateGroup() {
        assertEquals(1, Postgres.findAllGroups().size());

        group.setName("new group name");
        group.insertOrUpdate();

        assertEquals(1, Postgres.findAllGroups().size());
        assertEquals(group.toString(), Postgres.findGroup(group.getId()).toString());

        Group newGroup = new Group("Group2");
        int newId = newGroup.insertOrUpdate();
        assertEquals(2, Postgres.findAllGroups().size());
        assertEquals(newGroup.toString(), Postgres.findGroup(newId).toString());
    }

    @Test
    public void testDeleteGroup() {
        assertEquals(group.toString(), Postgres.findGroup(group.getId()).toString());
        Postgres.deleteGroup(group.getId());
        assertEquals(null, Postgres.findGroup(group.getId()));
    }

    private static void insertData() {
        // insert Group
        group = new Group("Test Group");
        group.insert();
    }
}