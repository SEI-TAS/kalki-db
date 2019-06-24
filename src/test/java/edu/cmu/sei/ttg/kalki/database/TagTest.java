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

public class TagTest extends AUsesDatabase {
    private static Tag tag;

    @Before
    public void resetDB() {
        Postgres.resetDatabase();
        insertData();
    }

    /*
        Test Tag Actions
     */

    @Test
    public void testFindTag() {
        assertEquals(tag.toString(), Postgres.findTag(tag.getId()).toString());
    }

    @Test
    public void testFindAllTags() {
        assertEquals(1, Postgres.findAllTags().size());
    }

    @Test
    public void testInsertOrUpdateTag() {
        assertEquals(1, Postgres.findAllTags().size());

        tag.setName("new tag name");
        tag.insertOrUpdate();

        assertEquals(1, Postgres.findAllTags().size());
        assertEquals(tag.toString(), Postgres.findTag(tag.getId()).toString());

        Tag newTag = new Tag("Tag2");
        int newId = newTag.insertOrUpdate();
        assertEquals(2, Postgres.findAllTags().size());
        assertEquals(newTag.toString(), Postgres.findTag(newId).toString());
    }

    @Test
    public void testDeleteTag() {
        assertEquals(tag.toString(), Postgres.findTag(tag.getId()).toString());
        Postgres.deleteTag(tag.getId());
        assertEquals(null, Postgres.findTag(tag.getId()));
    }

    private static void insertData() {
        // insert Tag
        tag = new Tag("Tag1");
        tag.insert();
    }
}