package edu.cmu.sei.kalki.db.daos;

import edu.cmu.sei.kalki.db.database.Postgres;
import edu.cmu.sei.kalki.db.models.AlertTypeLookup;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AlertTypeLookupDAO extends DAO
{

    /**
     * Returns the row from alert_type_lookup with the given id
     * @param id of the row
     */
    public static AlertTypeLookup findAlertTypeLookup(int id) {
        ResultSet rs = findById(id, "alert_type_lookup");
        AlertTypeLookup alertTypeLookup = (AlertTypeLookup) createFromRs(AlertTypeLookup.class, rs);
        closeResources(rs);
        return alertTypeLookup;
    }

    /**
     * Returns all rows from alert_type_lookup for the given device_type
     * @param typeId The device_type id
     */
    public static List<AlertTypeLookup> findAlertTypeLookupsByDeviceType(int typeId){
        List<AlertTypeLookup> atlList = new ArrayList<>();
        try(PreparedStatement st = Postgres.prepareStatement("Select * from alert_type_lookup WHERE device_type_id=?")) {
            st.setInt(1, typeId);
            try(ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    atlList.add((AlertTypeLookup) createFromRs(AlertTypeLookup.class, rs));
                }
            }
        } catch (SQLException e) {
            logger.severe("Sql exception getting all alert_type_lookups for the device type: "+typeId);
            e.printStackTrace();
        }
        return atlList;
    }

    /**
     * Returns all rows from the alert_type_lookup table
     * @return A list of AlertTypeLookups
     */
    public static List<AlertTypeLookup> findAllAlertTypeLookups() {
        return (List<AlertTypeLookup>) findAll("alert_type_lookup", AlertTypeLookup.class);
    }

    /**
     * Inserts the given AlertTypeLookup into the alert_type_lookup table
     * @param atl
     * @return The id of the new AlertTypeLookup. -1 on failure
     */
    public static int insertAlertTypeLookup(AlertTypeLookup atl){
        logger.info("Inserting AlertTypeLookup: " + atl.toString());
        try(PreparedStatement insertAtl = Postgres.prepareStatement("INSERT INTO alert_type_lookup(alert_type_id, device_type_id, variables) VALUES (?,?,?)")) {
            insertAtl.setInt(1, atl.getAlertTypeId());
            insertAtl.setInt(2, atl.getDeviceTypeId());
            insertAtl.setObject(3, atl.getVariables());
            insertAtl.executeUpdate();
            return getLatestId("alert_type_lookup");
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error inserting AlertTypeLookup: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return -1;
    }

    /**
     * Updates the row for the given AlertTypeLookup
     * @param atl The object with new values for the row
     * @return The id of the AlertTypeLookup. -1 on failure
     */
    public static int updateAlertTypeLookup(AlertTypeLookup atl){
        logger.info("Updating AlertTypeLookup; atlId: " +atl.getId());
        try(PreparedStatement updateAtl = Postgres.prepareStatement("UPDATE alert_type_lookup SET alert_type_id = ?, device_type_id = ?, variables = ? WHERE id = ?")) {
            updateAtl.setInt(1, atl.getAlertTypeId());
            updateAtl.setInt(2, atl.getDeviceTypeId());
            updateAtl.setObject(3, atl.getVariables());
            updateAtl.setInt(4, atl.getId());
            updateAtl.executeUpdate();

            return atl.getId();
        } catch (SQLException e) {
            logger.severe("Error updating AlertTypeLookup: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Checks if row exists in alert_type_lookup for the given AlertTypeLookup
     * If there is now row, insert it. Otherwise update the row
     * @param alertTypeLookup
     * @return
     */
    public static int insertOrUpdateAlertTypeLookup(AlertTypeLookup alertTypeLookup) {
        AlertTypeLookup atl = findAlertTypeLookup(alertTypeLookup.getId());
        if(atl == null){
            return insertAlertTypeLookup(alertTypeLookup);
        } else {
            return updateAlertTypeLookup(alertTypeLookup);
        }

    }
    /**
     * Deletes an AlertTypeLookup by its id.
     *
     * @param id id of the AlertTypeLookup to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static Boolean deleteAlertTypeLookup(int id) {
        logger.info(String.format("Deleting alert_type_lookup with id = %d", id));
        return deleteById("alert_type_lookup", id);
    }
    
}