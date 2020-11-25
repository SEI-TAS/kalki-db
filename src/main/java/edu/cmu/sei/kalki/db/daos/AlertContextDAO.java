package edu.cmu.sei.kalki.db.daos;

import edu.cmu.sei.kalki.db.database.Postgres;
import edu.cmu.sei.kalki.db.models.AlertCondition;
import edu.cmu.sei.kalki.db.models.AlertContext;
import org.postgresql.util.PSQLException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class AlertContextDAO extends DAO
{
    /**
     * Extract an AlertContext from the result set of a database query.
     */
    public static AlertContext createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) return null;
        int id = rs.getInt("id");
        int alertTypeLookupId = rs.getInt("alert_type_lookup_id");
        String logicalOperator = rs.getString("logical_operator");

        Integer deviceTypeId = 0;
        try {
            deviceTypeId = (Integer) rs.getObject("device_type_id");
        } catch (PSQLException e) { logger.info("No device_type_id on ResultSet"); }
        String alertTypeName = "";
        try {
            alertTypeName = (String) rs.getObject("alert_type_name");
        } catch (PSQLException e) { logger.info("No alert_type_name on ResultSet"); }

        return new AlertContext(id, deviceTypeId, logicalOperator, alertTypeLookupId, alertTypeName);
    }

    /**
     * Finds an AlertContext from the database with the given id
     *
     * @param id The id of the desired AlertContext
     * @return An AlertContext with desired id
     */
    public static AlertContext findAlertContext(int id) {
        String query = "SELECT ac.*, dt.id AS device_type_id, dt.name AS device_type_name, at.name AS alert_type_name " +
                "FROM alert_context AS ac, device_type AS dt, alert_type AS at, alert_type_lookup as atl " +
                "WHERE ac.id=? AND atl.device_type_id=dt.id AND ac.alert_type_lookup_id=atl.id AND atl.alert_type_id=at.id";
        AlertContext context = (AlertContext) findObjectByIdAndQuery(id, query, AlertContextDAO.class);
        if(context != null) {
            context.setConditions(AlertConditionDAO.findAlertConditionsForContext(context.getId()));
        }
        return context;
    }

    /**
     * Finds all default AlertContexts for the given device type id, that are NOT device-specific.
     * @param typeId
     * @return
     */
    public static List<AlertContext> findAlertContextsForDeviceType(int typeId) {
        logger.info("Finding AlertContexts for DeviceType id: "+typeId);
        String query = "SELECT ac.*, at.name AS alert_type_name, dt.id AS device_type_id " +
                "FROM alert_context AS ac, alert_type_lookup AS atl, alert_type AS at, device_type AS dt " +
                "WHERE atl.device_type_id = ? AND atl.device_type_id=dt.id AND atl.id = ac.alert_type_lookup_id AND atl.alert_type_id = at.id " +
                "ORDER BY ac.id";
        List<AlertContext> contextList = (List<AlertContext>) findObjectsByIdAndQuery(typeId, query, AlertContextDAO.class);
        for(AlertContext context: contextList){
            List<AlertCondition> conditions = AlertConditionDAO.findAlertConditionsForContext(context.getId());
            context.setConditions(conditions);
        }
        return contextList;
    }

    /**
     * Finds all  AlertContexts for the given device alertTypeLookupId.
     * @param typeId
     * @return
     */
    public static List<AlertContext> findAlertContextsForAlertTypeLookup(int alertTypeLookupId) {
        logger.info("Finding AlertContexts for AlertTypeLookup id: "+alertTypeLookupId);
        String query = "SELECT ac.*, at.name AS alert_type_name, dt.id AS device_type_id " +
                "FROM alert_context AS ac, alert_type_lookup AS atl, alert_type AS at, device_type AS dt " +
                "WHERE ac.alert_type_lookup_id = ? AND atl.device_type_id=dt.id AND atl.id = ac.alert_type_lookup_id AND atl.alert_type_id = at.id " +
                "ORDER BY ac.id";
        List<AlertContext> contextList = (List<AlertContext>) findObjectsByIdAndQuery(alertTypeLookupId, query, AlertContextDAO.class);
        for(AlertContext context: contextList){
            List<AlertCondition> conditions = AlertConditionDAO.findAlertConditionsForContext(context.getId());
            context.setConditions(conditions);
        }
        return contextList;
    }

    /**
     * Insert a row into the AlertContext table
     *
     * @param cond The AlertContext to be added
     * @return id of new AlertContext on success. -1 on error
     */
    public static AlertContext insertAlertContext(AlertContext cont) {
        logger.info("Inserting alert context");
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("INSERT INTO alert_context(alert_type_lookup_id, logical_operator) VALUES (?,?) RETURNING id")) {
            st.setInt(1, cont.getAlertTypeLookupId());
            st.setString(2, cont.getLogicalOperator());
            st.execute();

            int id = getLatestId(st);
            cont.setId(id);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error inserting AlertContext: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return cont;
    }

    /**
     * Updates a row in the AlertContext table
     */
    public static AlertContext updateAlertContext(AlertContext cont) {
        logger.info("Updating alert context: "+cont.getId());
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("UPDATE alert_context " +
                    "SET alert_type_lookup_id = ?, logical_operator = ? " +
                    "WHERE id = ?")) {
            st.setInt(1, cont.getAlertTypeLookupId());
            st.setString(2, cont.getLogicalOperator());
            st.setInt(3, cont.getId());
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error updating AlertContext: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return cont;
    }

    /**
     * Inserts a row into AlertContext table if it does not exist.
     * Otherwise it will update the corresponding row
     */
    public static AlertContext insertOrUpdateAlertContext(AlertContext cond) {
        AlertContext c = findAlertContext(cond.getId());
        if(c == null)
            return insertAlertContext(cond);
        else
            return updateAlertContext(cond);
    }

    /**
     * Deletes an AlertContext by its id.
     *
     * @param id id of the AlertContext to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static Boolean deleteAlertContext(int id) {
        return deleteById("alert_context", id);
    }

}
