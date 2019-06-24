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

public class UmboxImageTest extends AUsesDatabase {
    private static UmboxImage umboxImage;

    @Before
    public void resetDB() {
        Postgres.resetDatabase();
        insertData();
    }

    /*
        Test Umbox Image Actions
     */

    @Test
    public void testFindUmboxImage() {
        assertEquals(umboxImage.toString(), Postgres.findUmboxImage(umboxImage.getId()).toString());
    }

    @Test
    public void testFindAllUmboxImages() {
        assertEquals(2, Postgres.findAllUmboxImages().size()); //one added in setupDatabase and one in insertData
    }

    @Test
    public void testInsertOrUpdateUmboxImage() {
        assertEquals(2, Postgres.findAllUmboxImages().size());

        umboxImage.setName("changed name");
        umboxImage.insertOrUpdate();

        assertEquals(2, Postgres.findAllUmboxImages().size());
        assertEquals(umboxImage.toString(), Postgres.findUmboxImage(umboxImage.getId()).toString());

        UmboxImage newImage = new UmboxImage("image2", "path/to/new/image");
        int newId = newImage.insertOrUpdate();
        assertEquals(3, Postgres.findAllUmboxImages().size());
        assertEquals(newImage.toString(), Postgres.findUmboxImage(newId).toString());
    }
//      waiting until I separate out the classes so I don't have to do so many deletes
//    @Test
//    public void testDeleteUmboxImage() {
//        assertEquals(umboxImage.toString(), Postgres.findUmboxImage(umboxImage.getId()).toString());
//        Postgres.deleteUmboxImage(umboxImage.getId());
//        assertEquals(null, Postgres.findUmboxImage(umboxImage.getId()));
//    }

    private static void insertData() {
        // insert umbox_image
        umboxImage = new UmboxImage("UmboxImage", "path/to/image");
        umboxImage.insert();
    }
}
