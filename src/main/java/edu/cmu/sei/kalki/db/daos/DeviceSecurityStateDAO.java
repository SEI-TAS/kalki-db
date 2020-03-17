package edu.cmu.sei.kalki.db.daos;

import edu.cmu.sei.kalki.db.database.Postgres;
import edu.cmu.sei.kalki.db.models.Device;
import edu.cmu.sei.kalki.db.models.DeviceSecurityState;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class DeviceSecurityStateDAO extends DAO
{
    /**
     * Extract a DeviceSecurityState from the result set of a database query.
     */
    public static DeviceSecurityState createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) return null;
        int id = rs.getInt("id");
        int deviceId = rs.getInt("device_id");
        int stateId = rs.getInt("state_id");
        Timestamp timestamp = rs.getTimestamp("timestamp");
        String name = rs.getString("name");
        return new DeviceSecurityState(id, deviceId, stateId, timestamp, name);
    }
    
    /**
     * Finds the DeviceSecurityState with the supplied id
     *
     * @param the id for the DeviceSecurityState
     * @return the DeviceSecurityState, if it exists
     */
    public static DeviceSecurityState findDeviceSecurityState(int id) {
        String query = "SELECT dss.id, dss.device_id, dss.timestamp, ss.name, ss.id AS state_id " +
                "FROM device_security_state dss, security_state ss " +
                "WHERE dss.id=? AND dss.state_id = ss.id " +
                "ORDER BY timestamp DESC " +
                "LIMIT 1";
        return (DeviceSecurityState) findObjectByIdAndQuery(id, query, DeviceSecurityStateDAO.class);
    }

    /**
     * Returns all device security states in the DB.
     * @return
     */
    public static List<DeviceSecurityState> findAllDeviceSecurityStates() {
        String query = "SELECT dss.id, dss.device_id, dss.timestamp, dss.state_id, ss.name FROM device_security_state AS dss, security_state AS ss WHERE dss.state_id=ss.id";
        return (List<DeviceSecurityState>) findObjectsByQuery(query, DeviceSecurityStateDAO.class);
    }

    /**
     * Finds the most recent DeviceSecurityState from the database for the given device
     *
     * @param deviceId the id of the device
     * @return the most recent DeviceSecurityState entered for a device
     */
    public static DeviceSecurityState findDeviceSecurityStateByDevice(int deviceId) {
        String query = "SELECT dss.id, dss.device_id, dss.timestamp, ss.name, ss.id AS state_id " +
                "FROM device_security_state dss, security_state ss " +
                "WHERE dss.device_id=? AND dss.state_id = ss.id " +
                "ORDER BY timestamp DESC " +
                "LIMIT 1";
        return (DeviceSecurityState) findObjectByIdAndQuery(deviceId, query, DeviceSecurityStateDAO.class);
    }

    /**
     * Finds the next to last device security state for a device.
     * @param device
     * @return
     */
    public static int findPreviousDeviceSecurityStateId(Device device) {
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("SELECT dss.state_id AS state_id " +
                "FROM device_security_state dss " +
                "WHERE dss.device_id=? AND dss.id < ? " +
                "ORDER BY dss.id DESC " +
                "LIMIT 1")) {
            st.setInt(1, device.getId());
            st.setInt(2, device.getCurrentState().getId());
            try(ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("state_id");
                } else {
                    logger.info("Only 1 device security state entered for device with id: " + device.getId());
                    return -1;
                }
            }
        } catch (SQLException e) {
            logger.severe("Error finding previous device security state for device: "+device.getId());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Finds all DeviceSecurityState from the database for the given device.
     *
     * @param deviceId the id of the device.
     * @return a list of all DeviceSecurityState in the database where the device_id field is equal to deviceId.
     */
    public static List<DeviceSecurityState> findDeviceSecurityStates(int deviceId) {
        String query = "SELECT dss.id, dss.device_id, dss.timestamp, ss.name, ss.id AS state_id " +
                "FROM device_security_state dss, security_state ss " +
                "WHERE dss.device_id=? AND dss.state_id = ss.id " +
                "ORDER BY timestamp DESC";
        return (List<DeviceSecurityState>) findObjectsByIdAndQuery(deviceId, query, DeviceSecurityStateDAO.class);
    }

    /**
     * Saves given DeviceSecurityState to the database.
     *
     * @param deviceState DeviceSecurityState to be inserted.
     * @return The id of the new DeviceSecurityState if successful
     */
    public static Integer insertDeviceSecurityState(DeviceSecurityState deviceState) {
        logger.info("Inserting DeviceSecurityState");
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement
                ("INSERT INTO device_security_state(device_id, timestamp, state_id) " +
                        "values(?,?,?) " +
                        "RETURNING id")) {
            st.setInt(1, deviceState.getDeviceId());
            st.setTimestamp(2, deviceState.getTimestamp());
            st.setInt(3, deviceState.getStateId());
            try(ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error inserting DeviceState: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return -1;
    }

    /**
     * Deletes a DeviceSecurityState by its id.
     *
     * @param id id of the DeviceSecurityState to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static Boolean deleteDeviceSecurityState(int id) {
        return deleteById("device_security_state", id);
    }
    
}
