package edu.cmu.sei.kalki.db.daos;

import edu.cmu.sei.kalki.db.database.Postgres;
import edu.cmu.sei.kalki.db.models.AlertCondition;

import java.sql.*;
import java.util.List;

public class AlertConditionDAO extends DAO {

    /**
     * Extract an AlertCondition from the result set of a database query
     */
    public static AlertCondition createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) return null;

        ResultSetMetaData metaData = rs.getMetaData();
        int numColumns = metaData.getColumnCount();
        StringBuilder str = new StringBuilder();
        for(int i=1; i<=numColumns; i++) {
            str.append(metaData.getColumnName(i)+": "+rs.getObject(i).toString()+"; ");
        }
        logger.info("RESULTSET: "+str.toString());

        int id = rs.getInt("id");
        int deviceId = rs.getInt("device_id");
        int attributeId = rs.getInt("attribute_id");
        String attributeName = rs.getString("attribute_name");
        int numStatuses = rs.getInt("num_statuses");
        String compOperator = rs.getString("comparison_operator");
        String calculation = rs.getString("calculation");
        int thresholdId = rs.getInt("threshold_id");
        String thresholdValue = rs.getString("threshold_value");

        return new AlertCondition(id, deviceId, attributeId, attributeName, numStatuses, compOperator, calculation, thresholdId, thresholdValue);
    }

    /**
     * Finds an AlertCondition from the database with the given id
     */
    public static AlertCondition findAlertCondition(int id) {
        String query = "SELECT ac.*, ds.name AS attribute_name " +
                "FROM alert_condition AS ac, device_sensor as ds " +
                "WHERE ac.attribute_id = ds.id AND ac.id = ?";
        return (AlertCondition) findObjectByIdAndQuery(id, query, AlertConditionDAO.class);
    }

    /**
     * Finds all AlertConditions for a specific AlertContext
     */
    public static List<AlertCondition> findAlertConditionsForContext(int contextId) {
        String query = "SELECT ac.*, ds.name AS attribute_name " +
                "FROM alert_condition AS ac, device_sensor as ds, alert_circumstance AS circ " +
                "WHERE circ.context_id = ? AND circ.condition_id = ac.id AND ac.attribute_id = ds.id";
        return (List<AlertCondition>) findObjectsByIdAndQuery(contextId, query, AlertConditionDAO.class);
    }

    /**
     * Insert a row into the AlertCondition table
     */
    public static AlertCondition insertAlertCondition(AlertCondition cond) {
        logger.info("Inserting alert condition for device: "+ cond.getDeviceId());
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("INSERT INTO alert_condition(device_id, attribute_id, num_statuses, comparison_operator, calculation, threshold_id, threshold_value) VALUES(?,?,?,?,?,?,?) RETURNING  id")) {
            st.setInt(1, cond.getDeviceId());
            st.setInt(2, cond.getAttributeId());
            st.setInt(3, cond.getNumStatues());
            st.setString(4, cond.getCompOperator());
            st.setString(5, cond.getCalculation());
            st.setObject(6, cond.getThresholdId());
            st.setString(7, cond.getThresholdValue());
            st.execute();
            int id = getLatestId(st);
            cond.setId(id);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error inserting AlertCondition: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return cond;
    }

    /**
     * Updates a row in the AlertCondition table
     */
    public static AlertCondition updateAlertCondition(AlertCondition cond) {
        logger.info("Updating alert condition: "+cond.getId());
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("UPDATE alert_condition " +
                    "SET device_id = ?, attribute_id = ?, num_statuses = ?, comparison_operator = ?, calculation = ?, threshold_id = ?, threshold_value = ? " +
                    "WHERE id = ?")) {
            st.setInt(1, cond.getDeviceId());
            st.setInt(2, cond.getAttributeId());
            st.setInt(3, cond.getNumStatues());
            st.setString(4, cond.getCompOperator());
            st.setString(5, cond.getCalculation());
            st.setObject(6, cond.getAttributeId());
            st.setString(7, cond.getThresholdValue());
            st.setInt(8, cond.getId());
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error updating AlertCondition: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return cond;
    }

    /**
     * Inserts a row into AlertCondition table if it does not exist.
     * Otherwise it will update the corresponding row
     */
    public static AlertCondition insertOrUpdateAlertCondition(AlertCondition cond) {
        AlertCondition c = findAlertCondition(cond.getId());
        if(c == null)
            return insertAlertCondition(cond);
        else
            return updateAlertCondition(cond);
    }
}
