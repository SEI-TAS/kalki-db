package edu.cmu.sei.kalki.db.daos;

import edu.cmu.sei.kalki.db.database.Postgres;
import edu.cmu.sei.kalki.db.models.Device;
import edu.cmu.sei.kalki.db.models.DeviceStatus;
import org.postgresql.util.HStoreConverter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceStatusDAO extends DAO
{
    /**
     * Extract a DeviceStatus from the result set of a database query.
     */
    public static DeviceStatus createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) return null;
        int deviceId = rs.getInt("device_id");
        Map<String, String> attributes = HStoreConverter.fromString(rs.getString("attributes"));
        Timestamp timestamp = rs.getTimestamp("timestamp");
        int statusId = rs.getInt("id");
        return new DeviceStatus(deviceId, attributes, timestamp, statusId);
    }
    
    /**
     * Finds a DeviceStatus from the database by its id.
     *
     * @param id id of the DeviceStatus to find.
     * @return the DeviceStatus if it exists in the database, else null.
     */
    public static DeviceStatus findDeviceStatus(int id) {
        ResultSet rs = findById(id, "device_status");
        DeviceStatus deviceStatus = null;
        try {
            deviceStatus = createFromRs(rs);
        } catch (SQLException e) {
            logger.severe("Sql exception creating object");
            e.printStackTrace();
        }
        closeResources(rs);
        return deviceStatus;
    }

    /**
     * Finds all DeviceStatuses from the database for the given device.
     *
     * @param deviceId the id of the device.
     * @return a list of all DeviceStatuses in the database where the device_id field is equal to deviceId.
     */
    public static List<DeviceStatus> findDeviceStatuses(int deviceId) {
        try(PreparedStatement st = Postgres.prepareStatement("SELECT * FROM device_status WHERE device_id = ?")) {
            st.setInt(1, deviceId);
            try(ResultSet rs = st.executeQuery()) {
                List<DeviceStatus> deviceHistories = new ArrayList<>();
                while (rs.next()) {
                    deviceHistories.add(createFromRs(rs));
                }
                return deviceHistories;
            }
        } catch (SQLException e) {
            logger.severe("Sql exception getting all device statuses: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Finds the last N DeviceStatuses for the given device
     *
     * @param deviceId the id of the device
     * @param N        the number of statuses to retrieve
     * @return a list of N device statuses
     */
    public static List<DeviceStatus> findNDeviceStatuses(int deviceId, int N) {
        logger.info("Finding last " + N + "device statuses for device: " + deviceId);
        try(PreparedStatement st = Postgres.prepareStatement("SELECT * FROM device_status WHERE device_id = ? ORDER BY id DESC LIMIT ?")) {
            st.setInt(1, deviceId);
            st.setInt(2, N);
            try(ResultSet rs = st.executeQuery()) {
                List<DeviceStatus> deviceHistories = new ArrayList<>();
                while (rs.next()) {
                    deviceHistories.add(createFromRs(rs));
                }
                return deviceHistories;
            }
        } catch (SQLException e) {
            logger.severe("Sql exception getting all device statuses: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Finds numStatuses worth of device statuses where their id < startingId
     * @param deviceId
     * @param numStatuses
     * @param startingId
     * @return list of device statuses
     */
    public static List<DeviceStatus> findSubsetNDeviceStatuses(int deviceId, int numStatuses, int startingId) {
        logger.info("Finding "+numStatuses+" previous statuses from id: "+startingId);
        try(PreparedStatement st = Postgres.prepareStatement("SELECT * FROM device_status WHERE id < ? AND device_id = ? ORDER BY id DESC LIMIT ?")) {
            st.setInt(1, startingId);
            st.setInt(2, deviceId);
            st.setInt(3, numStatuses);
            try(ResultSet rs = st.executeQuery()) {
                List<DeviceStatus> deviceStatusList = new ArrayList<>();
                while (rs.next()) {
                    deviceStatusList.add(createFromRs(rs));
                }
                return deviceStatusList;
            }
        } catch(SQLException e) {
            logger.severe("SQL Exception getting subset of device statuses: "+e.getClass().getName()+": "+e.getMessage());
        }
        return null;
    }

    /**
     * Finds the last N DeviceStatuses for the given device
     *
     * @param deviceId the id of the device
     * @param startingTime The timestamp to start
     * @param period The amount of time back to search
     * @param timeUnit the unit of time to use (minute(s), hour(s), day(s))
     * @return a list of N device statuses
     */
    public static List<DeviceStatus> findDeviceStatusesOverTime(int deviceId, Timestamp startingTime, int period, String timeUnit) {
        String interval = period + " " + timeUnit;
        try(PreparedStatement st = Postgres.prepareStatement("SELECT * FROM device_status WHERE device_id = ? AND timestamp between (?::timestamp - (?::interval)) and ?::timestamp")) {
            st.setInt(1, deviceId);
            st.setTimestamp(2, startingTime);
            st.setString(3, interval);
            st.setTimestamp(4, startingTime);
            logger.info("Parameter count: " + st.getParameterMetaData().getParameterCount());
            try(ResultSet rs = st.executeQuery()) {
                List<DeviceStatus> deviceHistories = new ArrayList<>();
                while (rs.next()) {
                    deviceHistories.add(createFromRs(rs));
                }
                return deviceHistories;
            }
        } catch (SQLException e) {
            logger.severe("Sql exception getting all device statuses: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns a list of device statuses for devices with the given type id. One device status per device
     *
     * @param typeId The typeid for the requested devices
     * @return A map pairing a device with its most recent DeviceStatus
     */

    public static Map<Device, DeviceStatus> findDeviceStatusesByType(int typeId) {
        Map<Device, DeviceStatus> deviceStatusMap = new HashMap<>();
        List<Device> devices = DeviceDAO.findDevicesByType(typeId);
        for (Device device : devices) {
            List<DeviceStatus> statuses = findNDeviceStatuses(device.getId(), 1);
            for(DeviceStatus deviceStatus : statuses) {
                deviceStatusMap.put(device, deviceStatus);
            }
        }
        return deviceStatusMap;
    }

    /**
     * Returns a list of device statuses for devices with the given group id. One device status per device
     *
     * @param groupId The typeid for the requested devices
     * @return A map pairing a device with its most recent DeviceStatus
     */

    public static Map<Device, DeviceStatus> findDeviceStatusesByGroup(int groupId) {
        Map<Device, DeviceStatus> deviceStatusMap = new HashMap<>();
        List<Device> devices = DeviceDAO.findDevicesByGroup(groupId);
        for (Device device : devices) {
            List<DeviceStatus> statuses = findNDeviceStatuses(device.getId(), 1);
            for(DeviceStatus deviceStatus : statuses) {
                deviceStatusMap.put(device, deviceStatus);
            }
        }
        return deviceStatusMap;
    }

    /**
     * Finds all DeviceStatuses in the database.
     *
     * @return a list of all DeviceStatuses in the database.
     */
    public static List<DeviceStatus> findAllDeviceStatuses() {
        return (List<DeviceStatus>) findAll("device_status", DeviceStatusDAO.class);
    }

    /**
     * Saves given DeviceStatus to the database.
     *
     * @param deviceStatus DeviceStatus to be inserted.
     * @return auto incremented id
     */
    public static Integer insertDeviceStatus(DeviceStatus deviceStatus) {
        logger.info("Inserting device_status: " + deviceStatus.toString());
        try(PreparedStatement update = Postgres.prepareStatement
                ("INSERT INTO device_status(device_id, timestamp, attributes) values(?,?,?)")) {
            update.setInt(1, deviceStatus.getDeviceId());
            update.setTimestamp(2, deviceStatus.getTimestamp());
            update.setObject(3, deviceStatus.getAttributes());
            update.executeUpdate();

            return getLatestId("device_status");
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error inserting DeviceStatus: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return -1;
    }

    /**
     * First, attempts to find the DeviceStatus in the database.
     * If successful, updates the existing DeviceStatus with the given DeviceStatus's parameters Otherwise,
     * inserts the given DeviceStatus.
     *
     * @param deviceStatus DeviceStatus to be inserted or updated.
     */
    public static Integer insertOrUpdateDeviceStatus(DeviceStatus deviceStatus) {
        DeviceStatus ds = findDeviceStatus(deviceStatus.getId());
        if (ds == null) {
            return insertDeviceStatus(deviceStatus);
        } else {
            return updateDeviceStatus(deviceStatus);
        }
    }

    /**
     * Updates DeviceStatus with given id to have the parameters of the given DeviceStatus.
     *
     * @param deviceStatus DeviceStatus holding new parameters to be saved in the database.
     */
    public static Integer updateDeviceStatus(DeviceStatus deviceStatus) {
        logger.info("Updating DeviceStatus with id=" + deviceStatus.getId());
        try(PreparedStatement update = Postgres.prepareStatement
                ("UPDATE device_status SET device_id = ?, attributes = ?, timestamp = ? " +
                        "WHERE id=?")) {
            update.setInt(1, deviceStatus.getDeviceId());
            update.setObject(2, deviceStatus.getAttributes());
            update.setTimestamp(3, deviceStatus.getTimestamp());
            update.setInt(4, deviceStatus.getId());
            update.executeUpdate();
            return deviceStatus.getId();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error updating DeviceStatus: " + e.getClass().getName() + ": " + e.getMessage());
            return -1;
        }
    }

    /**
     * Deletes an DeviceStatus by its id.
     *
     * @param id id of the DeviceStatus to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static Boolean deleteDeviceStatus(int id) {
        return deleteById("device_status", id);
    }
    
}
