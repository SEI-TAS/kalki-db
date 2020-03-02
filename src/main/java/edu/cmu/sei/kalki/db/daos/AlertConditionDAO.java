package edu.cmu.sei.kalki.db.daos;

import edu.cmu.sei.kalki.db.database.Postgres;
import edu.cmu.sei.kalki.db.models.AlertCondition;
import edu.cmu.sei.kalki.db.models.AlertTypeLookup;
import edu.cmu.sei.kalki.db.models.Device;
import org.postgresql.util.HStoreConverter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AlertConditionDAO extends DAO
{
    /**
     * Extract an AlertCondition from the result set of a database query.
     */
    public static AlertCondition createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) return null;
        int id = rs.getInt("id");
        int deviceId = rs.getInt("device_id");
        String deviceName = rs.getString("device_name");
        int alertTypeLookupId = rs.getInt("alert_type_lookup_id");
        String alertTypeName = rs.getString("alert_type_name");
        Map<String, String> variables = null;
        if (rs.getString("variables") != null) {
            variables = HStoreConverter.fromString(rs.getString("variables"));
        }
        return new AlertCondition(id, deviceId, deviceName, alertTypeLookupId, alertTypeName, variables);
    }

    /**
     * Finds an AlertCondition from the database with the given id
     *
     * @param id The id of the desired AlertCondition
     * @return An AlertCondition with desired id
     */
    public static AlertCondition findAlertCondition(int id) {
        String query = "SELECT ac.*, d.name AS device_name, at.name AS alert_type_name " +
                "FROM alert_condition AS ac, device AS d, alert_type AS at, alert_type_lookup as atl " +
                "WHERE ac.id=? AND ac.device_id=d.id AND ac.alert_type_lookup_id=atl.id AND atl.alert_type_id=at.id";
        return (AlertCondition) findObjectByIdAndQuery(id, query, AlertConditionDAO.class);
    }

    /**
     * Finds all AlertConditions in the database
     *
     * @return a list of AlertCondition
     */
    public static List<AlertCondition> findAllAlertConditions() {
        String query = "SELECT ac.*, d.name AS device_name, at.name AS alert_type_name " +
                "FROM alert_condition AS ac, device AS d, alert_type AS at, alert_type_lookup as atl " +
                "WHERE ac.device_id=d.id AND ac.alert_type_lookup_id=atl.id AND atl.alert_type_id=at.id";
        return (List<AlertCondition>) findObjectsByQuery(query, AlertConditionDAO.class);
    }

    /**
     * Finds most recent AlertConditions from the database for the given device_id
     *
     * @param deviceId an id of a device
     * @return a list of all most recent AlertConditions in the database related to the given device
     */
    public static List<AlertCondition> findAlertConditionsByDevice(int deviceId) {
        String query = "SELECT DISTINCT ON (atl.id) alert_type_lookup_id, ac.id, ac.device_id, d.name AS device_name, at.name AS alert_type_name, ac.variables " +
                "FROM alert_condition AS ac, device AS d, alert_type AS at, alert_type_lookup AS atl " +
                "WHERE ac.device_id = ? AND ac.device_id=d.id AND ac.alert_type_lookup_id=atl.id AND atl.alert_type_id=at.id";
        return (List<AlertCondition>) findObjectByIdAndQuery(deviceId, query, AlertConditionDAO.class);
    }

    /**
     * Insert a row into the AlertCondition table
     *
     * @param cond The AlertCondition to be added
     * @return id of new AlertCondition on success. -1 on error
     */
    public static Integer insertAlertCondition(AlertCondition cond) {
        logger.info("Inserting alert condition for device: " + cond.getDeviceId());
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("INSERT INTO alert_condition(variables, device_id, alert_type_lookup_id) VALUES (?,?,?) RETURNING id")) {
            st.setObject(1, cond.getVariables());
            st.setInt(2, cond.getDeviceId());
            st.setInt(3, cond.getAlertTypeLookupId());
            st.execute();
            return getLatestId(st);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error inserting AlertCondition: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return -1;
    }

    /**
     * Insert row(s) into the AlertCondition table based on the given Device's type
     *
     * @param id the Id of the device
     * @return 1 on success. -1 on error
     */
    public static Integer insertAlertConditionForDevice(int id) {
        logger.info("Inserting alert conditions for device: " + id);

        Device d = DeviceDAO.findDevice(id);
        List<AlertTypeLookup> atlList = AlertTypeLookupDAO.findAlertTypeLookupsByDeviceType(d.getType().getId());
        for(AlertTypeLookup atl: atlList){
            AlertCondition ac = new AlertCondition(id, atl.getId(), atl.getVariables());
            ac.insertOrUpdate();
            if(ac.getId()<0) //insert failed
                return -1;
        }

        return 1;
    }

    /**
     * Insert row(s) into the AlertCondition table for devices in type specified on the AlertTypeLookup
     *
     * @param cond The AlertCondition to be added
     * @return id of new AlertCondition on success. -1 on error
     */
    public static Integer updateAlertConditionsForDeviceType(AlertTypeLookup alertTypeLookup) {
        logger.info("Inserting alert conditions for device type: " + alertTypeLookup.getDeviceTypeId());
        List<Device> deviceList = DeviceDAO.findDevicesByType(alertTypeLookup.getDeviceTypeId());
        if(deviceList != null) {
            for (Device d : deviceList) {
                AlertCondition alertCondition = new AlertCondition(d.getId(), alertTypeLookup.getId(), alertTypeLookup.getVariables());
                insertAlertCondition(alertCondition);
            }
        }
        return 1;
    }

    /**
     * Deletes an AlertCondition by its id.
     *
     * @param id id of the AlertCondition to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static Boolean deleteAlertCondition(int id) {
        return deleteById("alert_condition", id);
    }

}
