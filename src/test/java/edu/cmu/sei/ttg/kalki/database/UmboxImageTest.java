package edu.cmu.sei.ttg.kalki.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

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

    /*
        Test Umbox Image Actions
     */

    @Test
    public void testFindUmboxImage() {
        assertEquals(umboxImage.toString(), Postgres.findUmboxImage(umboxImage.getId()).toString());
    }

    @Test
    public void testFindAllUmboxImages() {
        assertEquals(1, Postgres.findAllUmboxImages().size());
    }

    @Test
    public void testInsertOrUpdateUmboxImage() {
        assertEquals(1, Postgres.findAllUmboxImages().size());

        umboxImage.setName("changed name");
        umboxImage.insertOrUpdate();

        assertEquals(1, Postgres.findAllUmboxImages().size());
        assertEquals(umboxImage.toString(), Postgres.findUmboxImage(umboxImage.getId()).toString());

        UmboxImage newImage = new UmboxImage("image2", "path/to/new/image");
        int newId = newImage.insertOrUpdate();
        assertEquals(2, Postgres.findAllUmboxImages().size());
        assertEquals(newImage.toString(), Postgres.findUmboxImage(newId).toString());
    }

    @Test
    public void testDeleteUmboxImage() {
        assertEquals(umboxImage.toString(), Postgres.findUmboxImage(umboxImage.getId()).toString());
        Postgres.deleteUmboxImage(umboxImage.getId());
        assertEquals(null, Postgres.findUmboxImage(umboxImage.getId()));
    }

    public void insertData() {
        // insert umbox_image
        umboxImage = new UmboxImage("UmboxImage", "path/to/image");
        umboxImage.insert();
    }
}
