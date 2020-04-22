package edu.cmu.sei.kalki.db.database;

import edu.cmu.sei.kalki.db.daos.DataNodeDAO;
import edu.cmu.sei.kalki.db.models.DataNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DataNodeTest extends AUsesDatabase
{
    private static DataNode dataNode;

    @Test
    public void testFindDataNode() {
        assertEquals(dataNode.toString(), DataNodeDAO.findDataNode(dataNode.getId()).toString());
    }

    @Test
    public void testFindAllDataNodes() {
        assertEquals(1, DataNodeDAO.findAllDataNodes().size());
    }

    @Test
    public void testInsertOrUpdateDataNode() {
        assertEquals(1, DataNodeDAO.findAllDataNodes().size());

        dataNode.setName("New DataNode name");
        dataNode.setIpAddress("localhost");
        dataNode.insertOrUpdate();

        assertEquals(1, DataNodeDAO.findAllDataNodes().size());
        assertEquals(dataNode.toString(), DataNodeDAO.findDataNode(dataNode.getId()).toString());

        DataNode newDataNode = new DataNode("DataNode2", "192.0.0.1");
        int newId = newDataNode.insertOrUpdate();
        assertEquals(2, DataNodeDAO.findAllDataNodes().size());
        assertEquals(newDataNode.toString(), DataNodeDAO.findDataNode(newId).toString());
    }

    @Test
    public void testDeleteDataNode() {
        assertEquals(dataNode.toString(), DataNodeDAO.findDataNode(dataNode.getId()).toString());
        DataNodeDAO.deleteDataNode(dataNode.getId());
        assertEquals(null, DataNodeDAO.findDataNode(dataNode.getId()));
    }

    public void insertData() {
        // insert Group
        dataNode = new DataNode("Test DataNode", "127.0.0.1");
        dataNode.insert();
    }
}
