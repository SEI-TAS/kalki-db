package edu.cmu.sei.kalki.db.daos;

import edu.cmu.sei.kalki.db.database.Postgres;
import edu.cmu.sei.kalki.db.models.AlertCondition;
import edu.cmu.sei.kalki.db.models.AlertContext;
import edu.cmu.sei.kalki.db.models.AlertTypeLookup;
import edu.cmu.sei.kalki.db.models.Device;
import org.postgresql.util.HStoreConverter;
import org.postgresql.util.PSQLException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class AlertContextDAO extends DAO
{
    /**
     * Extract an AlertContext from the result set of a database query.
     */
    public static AlertContext createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) return null;
        int id = rs.getInt("id");
        Integer deviceId = (Integer) rs.getObject("device_id");
        String deviceName = "";
        String alertTypeName = "";
        try {
            deviceName = (String) rs.getObject("device_name");
        } catch (PSQLException e) { logger.info("No device_name on ResultSet"); }
        try {
            alertTypeName = (String) rs.getObject("alert_type_name");
        } catch (PSQLException e) { logger.info("No alert_type_name on ResultSet"); }

        int alertTypeLookupId = rs.getInt("alert_type_lookup_id");
        String logicalOperator = rs.getString("logical_operator");

        return new AlertContext(id, deviceId, deviceName, logicalOperator, alertTypeLookupId, alertTypeName);
    }

    /**
     * Finds an AlertContext from the database with the given id
     *
     * @param id The id of the desired AlertContext
     * @return An AlertContext with desired id
     */
    public static AlertContext findAlertContext(int id) {
        String query = "SELECT ac.*, d.name AS device_name, at.name AS alert_type_name " +
                "FROM alert_context AS ac, device AS d, alert_type AS at, alert_type_lookup as atl " +
                "WHERE ac.id=? AND ac.device_id=d.id AND ac.alert_type_lookup_id=atl.id AND atl.alert_type_id=at.id";
        AlertContext context = (AlertContext) findObjectByIdAndQuery(id, query, AlertContextDAO.class);
        context.setConditions(AlertConditionDAO.findAlertConditionsForContext(context.getId()));
        return context;
    }

    /**
     * Finds all AlertContexts in the database
     *
     * @return a list of AlertContext
     */
    public static List<AlertContext> findAllAlertContexts() {
        String query = "SELECT ac.*, d.name AS device_name, at.name AS alert_type_name " +
                "FROM alert_context AS ac, device AS d, alert_type AS at, alert_type_lookup as atl " +
                "WHERE ac.device_id=d.id AND ac.alert_type_lookup_id=atl.id AND atl.alert_type_id=at.id";
        return (List<AlertContext>) findObjectsByQuery(query, AlertContextDAO.class);
    }

    /**
     * Finds most recent AlertContexts from the database for the given device_id
     *
     * @param deviceId an id of a device
     * @return a list of all most recent AlertContext in the database related to the given device
     */
    public static List<AlertContext> findAlertContextsByDevice(int deviceId) {
        String query = "SELECT DISTINCT ON (atl.id) alert_type_lookup_id, ac.id, ac.device_id, ac.logical_operator, d.name AS device_name, at.name AS alert_type_name " +
                "FROM alert_context AS ac, device AS d, alert_type AS at, alert_type_lookup AS atl " +
                "WHERE ac.device_id = ? AND ac.device_id=d.id AND ac.alert_type_lookup_id=atl.id AND atl.alert_type_id=at.id";
        List<AlertContext> contextList = (List<AlertContext>) findObjectsByIdAndQuery(deviceId, query, AlertContextDAO.class);
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
            PreparedStatement st = con.prepareStatement("INSERT INTO alert_context(device_id, alert_type_lookup_id, logical_operator) VALUES (?,?,?) RETURNING id")) {
            st.setObject(1, cont.getDeviceId());
            st.setInt(2, cont.getAlertTypeLookupId());
            st.setString(3, cont.getLogicalOperator());
            st.execute();

            int id = getLatestId(st);
            cont.setId(id);
            updateAlertCircumstance(cont);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error inserting AlertContext: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return cont;
    }

    /**
     * Update the alert_circumstance table for the given AlertContext
     * @param cont
     */
    public static void updateAlertCircumstance(AlertContext cont) {
        // remove alert context from alert circumstance
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("DELETE FROM alert_circumstance WHERE context_id = ?")) {
            st.setInt(1, cont.getId());
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error deleting AlertContexts from AlertCircumstance: " + e.getClass().getName() + ": " + e.getMessage());
        }

        // enter alert context and conditions into alert circumstance
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("INSERT INTO alert_circumstance(context_id, condition_id) VALUES (?,?)")) {
            // make sure alert conditions are in alert_condition table, then add row to alert_circumstance
            for(AlertCondition cond: cont.getConditions()) {
                cond.insertOrUpdate();
                st.setInt(1, cont.getId());
                st.setInt(2, cond.getId());
                st.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error inserting rows into alert_circumstance: " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Finds all default AlertContexts for the given device type id
     * @param typeId
     * @return
     */
    public static List<AlertContext> findAlertContextsForDeviceType(int typeId) {
        logger.info("Finding AlertContexts for DeviceType id: "+typeId);
        String query = "SELECT ac.*, at.name AS alert_type_name FROM alert_context AS ac, alert_type_lookup AS atl, alert_type AS at WHERE atl.device_type_id = ? AND atl.id = ac.alert_type_lookup_id AND atl.alert_type_id = at.id AND ac.device_id IS NULL";
        return (List<AlertContext>) findObjectsByIdAndQuery(typeId, query, AlertContextDAO.class);
    }

    /**
     * Insert row(s) into the AlertContext table based on the given Device's type
     *
     * @param id the Id of the device
     * @return 1 on success. -1 on error
     */
    public static Integer insertAlertContextForDevice(int id) {
        logger.info("Inserting alert contexts for device: " + id);
        Device d = DeviceDAO.findDevice(id);
        List<AlertContext> typeContexts = findAlertContextsForDeviceType(d.getType().getId());
        for(AlertContext c: typeContexts) {
            c.setDeviceId(d.getId());
            c.insert();

            if(c.getId() < 0)
                return -1;
        }

        return 1;
    }


    // TODO: i don't see a need for this function, so going to remove.

    /**
     * Insert row(s) into the AlertContext table for devices in type specified on the AlertTypeLookup
     *
     * @param alertTypeLookup The AlertType lookup associating alerts to a device type
     * @return id of new AlertContext on success. -1 on error
     */
//    public static Integer updateAlertContextsForDeviceType(AlertTypeLookup alertTypeLookup) {
//        logger.info("Inserting alert contexts for device type: " + alertTypeLookup.getDeviceTypeId());
//        List<Device> deviceList = DeviceDAO.findDevicesByType(alertTypeLookup.getDeviceTypeId());
//        if(deviceList != null) {
//            for (Device d : deviceList) {
//                AlertContext alertContext = new AlertContext(d.getId(), alertTypeLookup.getId());
//                insertAlertContext(alertContext);
//            }
//        }
//        return 1;
//    }

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
