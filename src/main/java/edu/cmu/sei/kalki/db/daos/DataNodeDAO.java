package edu.cmu.sei.kalki.db.daos;

import edu.cmu.sei.kalki.db.database.Postgres;
import edu.cmu.sei.kalki.db.models.DataNode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DataNodeDAO extends DAO
{
    /**
     * Extract a DataNode from the result set of a database query.
     */
    public static DataNode createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) return null;
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String ipAddress = rs.getString("ip_address");
        return new DataNode(id, name, ipAddress);
    }

    /**
     * Finds a DataNode from the database by its id.
     *
     * @param id id of the DataNode to find.
     * @return the DataNode if it exists in the database, else null.
     */
    public static DataNode findDataNode(int id) {
        return (DataNode) findObjectByIdAndTable(id, "data_node", DataNodeDAO.class);
    }

    /**
     * Finds all DataNodes in the database.
     *
     * @return a list of all DataNodes in the database.
     */
    public static List<DataNode> findAllDataNodes() {
        return (List<DataNode>) findObjectsByTable("data_node", DataNodeDAO.class);
    }

    /**
     * Saves given Data Node to the database.
     *
     * @param dataNode Data Node to be inserted.
     * @return auto incremented id
     */
    public static Integer insertDataNode(DataNode dataNode) {
        logger.info("Inserting data node: " + dataNode.getName());
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement
                    ("INSERT INTO data_node(name, ip_address)" +
                            "values(?, ?) RETURNING id")) {
            st.setString(1, dataNode.getName());
            st.setString(2, dataNode.getIpAddress());
            st.execute();
            return getLatestId(st);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error inserting DataNode: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return -1;
    }

    /**
     * Updates Data Node with given id to have the parameters of the given DataNode.
     *
     * @param dataNode data node holding new parameters to be saved in the database.
     */
    public static Integer updateDataNode(DataNode dataNode) {
        logger.info("Updating DataNode with id=" + dataNode.getId());
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement
                    ("UPDATE data_node SET name = ?, ip_address = ? " +
                            "WHERE id=?")) {
            st.setString(1, dataNode.getName());
            st.setString(2, dataNode.getIpAddress());
            st.setInt(3, dataNode.getId());
            st.executeUpdate();
            return dataNode.getId();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error updating Data Node: " + e.getClass().getName() + ": " + e.getMessage());
            return -1;
        }
    }

    /**
     * First, attempts to find the DataNode in the database.
     * If successful, updates the existing DataNode with the given DataNode's parameters. Otherwise,
     * inserts the given DataNode.
     *
     * @param dataNode DataNode to be inserted or updated.
     */
    public static Integer insertOrUpdateDataNode(DataNode dataNode) {
        DataNode existingDataNode = findDataNode(dataNode.getId());
        if (existingDataNode == null) {
            return insertDataNode(dataNode);
        } else {
            return updateDataNode(dataNode);
        }
    }

    /**
     * Deletes a DataNode by its id.
     *
     * @param id id of the DataNode to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static Boolean deleteDataNode(int id) {
        return deleteById("data_node", id);
    }
}
