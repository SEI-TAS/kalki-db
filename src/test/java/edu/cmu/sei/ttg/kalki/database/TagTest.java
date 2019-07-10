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
        assertEquals(tag.toString(), Postgres.findTag(tag.getId()).toString());
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
        assertEquals(tag.toString(), Postgres.findTag(tag.getId()).toString());

        Tag newTag = new Tag("Tag3");
        int newId = newTag.insertOrUpdate();
        assertEquals(3, Postgres.findAllTags().size());
        assertEquals(newTag.toString(), Postgres.findTag(newId).toString());
    }

    @Test
    public void testDeleteTag() {
        assertEquals(tag.toString(), Postgres.findTag(tag.getId()).toString());
        Postgres.deleteTag(tag.getId());
        assertEquals(null, Postgres.findTag(tag.getId()));
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