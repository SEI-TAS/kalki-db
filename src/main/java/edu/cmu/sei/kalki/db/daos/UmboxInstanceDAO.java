package edu.cmu.sei.kalki.db.daos;

import edu.cmu.sei.kalki.db.database.Postgres;
import edu.cmu.sei.kalki.db.models.UmboxInstance;
import edu.cmu.sei.kalki.db.models.UmboxLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class UmboxInstanceDAO extends DAO
{
    /**
     * Extract a UmboxInstance from the result set of a database query.
     */
    public static UmboxInstance createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) return null;
        int id = rs.getInt("id");
        String alerterId = rs.getString("alerter_id");
        int imageId = rs.getInt("umbox_image_id");
        int deviceId = rs.getInt("device_id");
        Timestamp startedAt = rs.getTimestamp("started_at");
        return new UmboxInstance(id, alerterId, imageId, deviceId, startedAt);
    }

    /**
     * Find a umbox instance by its alerter id
     *
     * @param alerterId The ID of desired UmboxInstance
     * @return The desired UmboxInstance on success or null on failure
     */
    public static UmboxInstance findUmboxInstance(String alerterId) {
        String query = "SELECT * FROM umbox_instance WHERE alerter_id = ?";
        return (UmboxInstance) findObjectByStringAndQuery(alerterId, query, UmboxInstanceDAO.class);
    }

    /**
     * Finds all UmboxInstances from the database for the given device.
     *
     * @param deviceId the id of the device.
     * @return a list of all UmboxInstaces in the database where the device_id field is equal to deviceId.
     */
    public static List<UmboxInstance> findUmboxInstances(int deviceId) {
        String query = "SELECT * FROM umbox_instance WHERE device_id = ?";
        return (List<UmboxInstance>) findObjectsByIdAndQuery(deviceId, query, UmboxInstanceDAO.class);
    }

    /**
     * Adds the desired UmboxInstance to the database
     *
     * @param u UmboxInstance to add
     * @return
     */
    public static Integer insertUmboxInstance(UmboxInstance u) {
        logger.info("Adding umbox instance: " + u);
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("INSERT INTO umbox_instance (alerter_id, umbox_image_id, device_id, started_at) VALUES (?,?,?,?) RETURNING id")) {
            st.setString(1, u.getAlerterId());
            st.setInt(2, u.getUmboxImageId());
            st.setInt(3, u.getDeviceId());
            st.setTimestamp(4, u.getStartedAt());
            st.execute();
            return getLatestId(st);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("SQL exception adding umbox instance: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return -1;
    }

    /**
     * Edit desired UmboxInstance
     *
     * @param u The instance to be updated
     * @return
     */
    public static Integer updateUmboxInstance(UmboxInstance u) {
        logger.info("Editing umbox intance: " + u);
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("UPDATE umbox_instance " +
                "SET alerter_id = ?, umbox_image_id = ?, device_id = ?, started_at = ?" +
                "WHERE id = ?")) {
            st.setString(1, u.getAlerterId());
            st.setInt(2, u.getUmboxImageId());
            st.setInt(3, u.getDeviceId());
            st.setTimestamp(4, u.getStartedAt());
            st.setInt(5, u.getId());
            st.executeUpdate();
            return u.getId();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("SQL exception editing umbox iamge: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return -1;
    }

    /**
     * Deletes a UmboxInstance by its id.
     *
     * @param id id of the UmboxInstance to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static Boolean deleteUmboxInstance(int id) {
        return deleteById("umbox_instance", id);
    }
    
}
