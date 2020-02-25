package edu.cmu.sei.kalki.db.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import edu.cmu.sei.kalki.db.models.Group;
import org.junit.jupiter.api.Test;

import edu.cmu.sei.kalki.db.models.*;

public class GroupTest extends AUsesDatabase {
    private static Group group;

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

    public void insertData() {
        // insert Group
        group = new Group("Test Group");
        group.insert();
    }
}