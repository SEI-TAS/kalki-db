package edu.cmu.sei.kalki.db.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import edu.cmu.sei.kalki.db.models.UmboxImage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.cmu.sei.ttg.kalki.models.*;

public class UmboxImageTest extends AUsesDatabase {
    private static UmboxImage umboxImage;

    /*
        Test Umbox Image Actions
     */

    @Test
    public void testFindUmboxImage() {
        Assertions.assertEquals(umboxImage.toString(), Postgres.findUmboxImage(umboxImage.getId()).toString());
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
        Assertions.assertEquals(umboxImage.toString(), Postgres.findUmboxImage(umboxImage.getId()).toString());

        UmboxImage newImage = new UmboxImage("image2", "path/to/new/image");
        int newId = newImage.insertOrUpdate();
        assertEquals(2, Postgres.findAllUmboxImages().size());
        Assertions.assertEquals(newImage.toString(), Postgres.findUmboxImage(newId).toString());
    }

    @Test
    public void testDeleteUmboxImage() {
        Assertions.assertEquals(umboxImage.toString(), Postgres.findUmboxImage(umboxImage.getId()).toString());
        Postgres.deleteUmboxImage(umboxImage.getId());
        Assertions.assertEquals(null, Postgres.findUmboxImage(umboxImage.getId()));
    }

    public void insertData() {
        // insert umbox_image
        umboxImage = new UmboxImage("UmboxImage", "path/to/image");
        umboxImage.insert();
    }
}
