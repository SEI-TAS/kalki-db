package edu.cmu.sei.kalki.db.daos;

import edu.cmu.sei.kalki.db.database.Postgres;
import edu.cmu.sei.kalki.db.models.Device;
import edu.cmu.sei.kalki.db.models.DeviceSecurityState;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DeviceSecurityStateDAO extends DAO
{

    /**
     * Finds the DeviceSecurityState with the supplied id
     *
     * @param the id for the DeviceSecurityState
     * @return the DeviceSecurityState, if it exists
     */
    public static DeviceSecurityState findDeviceSecurityState(int id) {
        DeviceSecurityState dss = null;
        logger.info("Finding device security state with id: " + id);
        try(PreparedStatement st = Postgres.prepareStatement("SELECT dss.id, dss.device_id, dss.timestamp, ss.name, ss.id AS state_id " +
                "FROM device_security_state dss, security_state ss " +
                "WHERE dss.id=? AND dss.state_id = ss.id " +
                "ORDER BY timestamp DESC " +
                "LIMIT 1")) {
            st.setInt(1, id);
            try( ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    dss = (DeviceSecurityState) createFromRs(DeviceSecurityState.class, rs);
                }
            }
        } catch (SQLException e) {
            logger.severe("SQL exception getting the device state: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return dss;
    }

    /**
     *
     * @return
     */
    public static List<DeviceSecurityState> findAllDeviceSecurityStates() {
        List<DeviceSecurityState> stateList = new ArrayList<>();
        try(PreparedStatement st = Postgres.prepareStatement("SELECT dss.id, dss.device_id, dss.timestamp, dss.state_id, ss.name FROM device_security_state AS dss, security_state AS ss WHERE dss.state_id=ss.id")) {
            try(ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    stateList.add((DeviceSecurityState) createFromRs(DeviceSecurityState.class, rs));
                }
            }
        } catch (Exception e){
            logger.severe("Error while trying to find all DeviceSecurityStates: "+e.getMessage());
        }
        return stateList;
    }

    /**
     * Finds the most recent DeviceSecurityState from the database for the given device
     *
     * @param deviceId the id of the device
     * @return the most recent DeviceSecurityState entered for a device
     */
    public static DeviceSecurityState findDeviceSecurityStateByDevice(int deviceId) {
        DeviceSecurityState ss = null;
        try(PreparedStatement st = Postgres.prepareStatement("SELECT dss.id, dss.device_id, dss.timestamp, ss.name, ss.id AS state_id " +
                "FROM device_security_state dss, security_state ss " +
                "WHERE dss.device_id=? AND dss.state_id = ss.id " +
                "ORDER BY timestamp DESC " +
                "LIMIT 1")) {
            st.setInt(1, deviceId);
            try(ResultSet rs = st.executeQuery()){
                if (rs.next()) {
                    ss = (DeviceSecurityState) createFromRs(DeviceSecurityState.class, rs);
                }
            }
        } catch (SQLException e) {
            logger.severe("SQL exception getting the device state: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return ss;
    }

    /**
     * Finds the next to last device security state for a device.
     * @param device
     * @return
     */
    public static int findPreviousDeviceSecurityStateId(Device device) {
        try(PreparedStatement st = Postgres.prepareStatement("SELECT dss.state_id AS state_id " +
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
        List<DeviceSecurityState> deviceStateList = new ArrayList<>();
        try(PreparedStatement st = Postgres.prepareStatement("SELECT dss.id, dss.device_id, dss.timestamp, ss.name, ss.id AS state_id " +
                "FROM device_security_state dss, security_state ss " +
                "WHERE dss.device_id=? AND dss.state_id = ss.id " +
                "ORDER BY timestamp DESC")) {
            st.setInt(1, deviceId);
            try(ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    deviceStateList.add((DeviceSecurityState) createFromRs(DeviceSecurityState.class, rs));
                }
            }
        } catch (SQLException e) {
            logger.severe("Sql exception getting all device states: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return deviceStateList;
    }

    /**
     * Saves given DeviceSecurityState to the database.
     *
     * @param deviceState DeviceSecurityState to be inserted.
     * @return The id of the new DeviceSecurityState if successful
     */
    public static Integer insertDeviceSecurityState(DeviceSecurityState deviceState) {
        logger.info("Inserting DeviceSecurityState");
        try(PreparedStatement insert = Postgres.prepareStatement
                ("INSERT INTO device_security_state(device_id, timestamp, state_id) " +
                        "values(?,?,?) " +
                        "RETURNING id")) {
            insert.setInt(1, deviceState.getDeviceId());
            insert.setTimestamp(2, deviceState.getTimestamp());
            insert.setInt(3, deviceState.getStateId());
            try(ResultSet rs = insert.executeQuery()) {
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
