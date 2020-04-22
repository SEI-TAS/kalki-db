package edu.cmu.sei.kalki.db.database;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.cmu.sei.kalki.db.daos.GroupDAO;
import edu.cmu.sei.kalki.db.models.Group;
import org.junit.jupiter.api.Test;

public class GroupTest extends AUsesDatabase {
    private static Group group;

    /*
        Test Group Actions
     */

    @Test
    public void testFindGroup() {
        assertEquals(group.toString(), GroupDAO.findGroup(group.getId()).toString());
    }

    @Test
    public void testFindAllGroups() {
        assertEquals(1, GroupDAO.findAllGroups().size());
    }

    @Test
    public void testInsertOrUpdateGroup() {
        assertEquals(1, GroupDAO.findAllGroups().size());

        group.setName("new group name");
        group.insertOrUpdate();

        assertEquals(1, GroupDAO.findAllGroups().size());
        assertEquals(group.toString(), GroupDAO.findGroup(group.getId()).toString());

        Group newGroup = new Group("Group2");
        int newId = newGroup.insertOrUpdate();
        assertEquals(2, GroupDAO.findAllGroups().size());
        assertEquals(newGroup.toString(), GroupDAO.findGroup(newId).toString());
    }

    @Test
    public void testDeleteGroup() {
        assertEquals(group.toString(), GroupDAO.findGroup(group.getId()).toString());
        GroupDAO.deleteGroup(group.getId());
        assertEquals(null, GroupDAO.findGroup(group.getId()));
    }

    public void insertData() {
        // insert Group
        group = new Group("Test Group");
        group.insert();
    }
}
