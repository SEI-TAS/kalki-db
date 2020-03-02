package edu.cmu.sei.kalki.db.daos;

import edu.cmu.sei.kalki.db.database.Postgres;
import edu.cmu.sei.kalki.db.models.AlertType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AlertTypeDAO extends DAO
{
    /**
     * Extract an AlertType from the result set of a database query.
     * @param rs ResultSet from a AlertType query.
     * @return The AlertType that was found.
     */
    public static AlertType createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) return null;
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        String source = rs.getString("source");
        return new AlertType(id, name, description, source);
    }

    /**
     * Finds an AlertType from the dataase with the given id
     */
    public static AlertType findAlertType(int id) {
        return (AlertType) findObjectByIdAndTable(id, "alert_type", AlertTypeDAO.class);
    }

    /**
     * Finds all AlertTypes from the database for the given type_id
     *
     * @param deviceTypeId an id of a DeviceType
     * @return a list of all AlertTypes in the database for the given DeviceType
     */
    public static List<AlertType> findAlertTypesByDeviceType(int deviceTypeId) {
        List<AlertType> alertTypeList = new ArrayList<>();
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("SELECT alert_type.id, alert_type.name, alert_type.description, alert_type.source " +
                "FROM alert_type, alert_type_lookup AS atl " +
                "WHERE alert_type.id = atl.alert_type_id AND atl.device_type_id = ?;")) {
            st.setInt(1, deviceTypeId);
            try(ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    alertTypeList.add(createFromRs(rs));
                }
            }
        } catch (SQLException e) {
            logger.severe("Sql exception getting all alert types: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return alertTypeList;
    }

    /**
     * Finds all AlertTypes in the database
     *
     * @return a list of AlertTypes
     */
    public static List<AlertType> findAllAlertTypes() {
        return (List<AlertType>) findObjectsByTable("alert_type", AlertTypeDAO.class);
    }

    /**
     * Insert a row into the AlertType table
     *
     * @param type The AlertType to be added
     * @return id of new AlertType on success. -1 on error
     */
    public static Integer insertAlertType(AlertType type) {
        logger.info("Inserting alert type: " + type.getName());
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("INSERT INTO alert_type(name, description, source) VALUES (?,?,?) RETURNING id")) {
            st.setString(1, type.getName());
            st.setString(2, type.getDescription());
            st.setString(3, type.getSource());
            st.execute();
            return getLatestId(st);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error inserting AlertType: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return -1;
    }

    /**
     * Updates provided AlertType
     *
     * @param type AlertType holding new values to be saved in the database.
     * @return the id of the updated Alert on success. -1 on failure
     */
    public static Integer updateAlertType(AlertType type) {
        logger.info(String.format("Updating AlertType with id = %d with values: %s", type.getId(), type));
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("UPDATE alert_type " +
                "SET name = ?, description = ?, source = ?" +
                "WHERE id = ?")) {
            st.setString(1, type.getName());
            st.setString(2, type.getDescription());
            st.setString(3, type.getSource());
            st.setInt(4, type.getId());
            st.executeUpdate();

            return type.getId();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error updating AlertType: " + e.getClass().toString() + ": " + e.getMessage());
        }
        return -1;
    }

    /**
     * First, attempts to find the AlertType in the database.
     * If successful, updates the existing AlertType with the given AlertType's parameters Otherwise,
     * inserts the given AlertType.
     *
     * @param type AlertType to be inserted or updated.
     */
    public static Integer insertOrUpdateAlertType(AlertType type) {
        AlertType a = findAlertType(type.getId());
        if (a == null) {
            return insertAlertType(type);
        } else {
            return updateAlertType(type);
        }
    }

    /**
     * Deletes an AlertType by its id.
     *
     * @param id id of the AlertType to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static Boolean deleteAlertType(int id) {
        return deleteById("alert_table", id);
    }
}
