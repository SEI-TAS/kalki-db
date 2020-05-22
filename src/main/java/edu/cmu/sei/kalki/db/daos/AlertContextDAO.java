package edu.cmu.sei.kalki.db.daos;

import edu.cmu.sei.kalki.db.database.Postgres;
import edu.cmu.sei.kalki.db.models.AlertContext;
import edu.cmu.sei.kalki.db.models.AlertTypeLookup;
import edu.cmu.sei.kalki.db.models.Device;
import org.postgresql.util.HStoreConverter;

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
        int deviceId = rs.getInt("device_id");
        String deviceName = rs.getString("device_name");
        int alertTypeLookupId = rs.getInt("alert_type_lookup_id");
        String alertTypeName = rs.getString("alert_type_name");

        return new AlertContext(id, deviceId, deviceName, alertTypeLookupId, alertTypeName);
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
        return (AlertContext) findObjectByIdAndQuery(id, query, AlertContextDAO.class);
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
        String query = "SELECT DISTINCT ON (atl.id) alert_type_lookup_id, ac.id, ac.device_id, d.name AS device_name, at.name AS alert_type_name " +
                "FROM alert_context AS ac, device AS d, alert_type AS at, alert_type_lookup AS atl " +
                "WHERE ac.device_id = ? AND ac.device_id=d.id AND ac.alert_type_lookup_id=atl.id AND atl.alert_type_id=at.id";
        return (List<AlertContext>) findObjectsByIdAndQuery(deviceId, query, AlertContextDAO.class);
    }

    /**
     * Insert a row into the AlertContext table
     *
     * @param cond The AlertContext to be added
     * @return id of new AlertContext on success. -1 on error
     */
    public static Integer insertAlertContext(AlertContext cond) {
        logger.info("Inserting alert context for device: " + cond.getDeviceId());
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("INSERT INTO alert_context(device_id, alert_type_lookup_id) VALUES (?,?) RETURNING id")) {
            st.setInt(1, cond.getDeviceId());
            st.setInt(2, cond.getAlertTypeLookupId());
            st.execute();
            return getLatestId(st);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error inserting AlertContext: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return -1;
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
        List<AlertTypeLookup> atlList = AlertTypeLookupDAO.findAlertTypeLookupsByDeviceType(d.getType().getId());
        for(AlertTypeLookup atl: atlList){
            AlertContext ac = new AlertContext(id, atl.getId());
            ac.insert();
            if(ac.getId()<0) //insert failed
                return -1;
        }

        return 1;
    }

    /**
     * Insert row(s) into the AlertContext table for devices in type specified on the AlertTypeLookup
     *
     * @param alertTypeLookup The AlertType lookup associating alerts to a device type
     * @return id of new AlertContext on success. -1 on error
     */
    public static Integer updateAlertContextsForDeviceType(AlertTypeLookup alertTypeLookup) {
        logger.info("Inserting alert contexts for device type: " + alertTypeLookup.getDeviceTypeId());
        List<Device> deviceList = DeviceDAO.findDevicesByType(alertTypeLookup.getDeviceTypeId());
        if(deviceList != null) {
            for (Device d : deviceList) {
                AlertContext alertCondition = new AlertContext(d.getId(), alertTypeLookup.getId());
                insertAlertContext(alertCondition);
            }
        }
        return 1;
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
