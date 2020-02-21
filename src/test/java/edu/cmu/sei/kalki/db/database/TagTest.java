package edu.cmu.sei.kalki.db.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import edu.cmu.sei.kalki.db.models.Device;
import edu.cmu.sei.kalki.db.models.DeviceType;
import edu.cmu.sei.kalki.db.models.Group;
import edu.cmu.sei.kalki.db.models.Tag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import edu.cmu.sei.kalki.db.models.*;

public class TagTest extends AUsesDatabase {
    private static Tag tag;
    private static Tag tagTwo;
    private static DeviceType deviceType;
    private static Group group;
    private static Device device;

    /*
        Test Tag Actions
     */

    @Test
    public void testFindTag() {
        Assertions.assertEquals(tag.toString(), Postgres.findTag(tag.getId()).toString());
    }

    @Test
    public void testFindAllTags() {
        assertEquals(2, Postgres.findAllTags().size());
    }

    @Test
    public void testFindTagsByDevice() {
        ArrayList<Tag> foundTags = new ArrayList<Tag>(Postgres.findTagsByDevice(device.getId()));

        assertEquals(1, foundTags.size());
        assertEquals(tag.toString(), foundTags.get(0).toString());
    }

    @Test
    public void testInsertOrUpdateTag() {
        assertEquals(2, Postgres.findAllTags().size());

        tag.setName("new tag name");
        tag.insertOrUpdate();

        assertEquals(2, Postgres.findAllTags().size());
        Assertions.assertEquals(tag.toString(), Postgres.findTag(tag.getId()).toString());

        Tag newTag = new Tag("Tag3");
        int newId = newTag.insertOrUpdate();
        assertEquals(3, Postgres.findAllTags().size());
        Assertions.assertEquals(newTag.toString(), Postgres.findTag(newId).toString());
    }

    @Test
    public void testDeleteTag() {
        Assertions.assertEquals(tag.toString(), Postgres.findTag(tag.getId()).toString());
        Postgres.deleteTag(tag.getId());
        Assertions.assertEquals(null, Postgres.findTag(tag.getId()));
    }

    public void insertData() {
        // insert Tag
        tag = new Tag("Tag1");
        tag.insert();

        tagTwo = new Tag("Tagw");
        tagTwo.insert();

        ArrayList<Integer> tagIds = new ArrayList<Integer>();
        tagIds.add(tag.getId());

        // insert device_type
        deviceType = new DeviceType(0, "Udoo Neo");
        deviceType.insert();

        // insert Group
        group = new Group("Test Group");
        group.insert();

        // insert device
        device = new Device("Device 1", "this is a test device", deviceType, "0.0.0.0", 1, 1);
        device.setTagIds(tagIds);
        device.insert();
    }
}
