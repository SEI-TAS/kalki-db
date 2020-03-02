package edu.cmu.sei.kalki.db.daos;

import edu.cmu.sei.kalki.db.database.Postgres;
import edu.cmu.sei.kalki.db.models.Alert;
import edu.cmu.sei.kalki.db.models.Device;
import edu.cmu.sei.kalki.db.models.DeviceSecurityState;
import edu.cmu.sei.kalki.db.models.SecurityState;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class DeviceDAO extends DAO
{
    /**
     * Extract a Device from the result set of a database query.
     */
    public static Device createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) return null;
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        int typeId = rs.getInt("type_id");
        int groupId = rs.getInt("group_id");
        String ip = rs.getString("ip_address");
        int statusHistorySize = rs.getInt("status_history_size");
        int samplingRate = rs.getInt("sampling_rate");
        int defaultSamplingRate = rs.getInt("default_sampling_rate");
        return new Device(id, name, description, typeId, groupId, ip, statusHistorySize, samplingRate, defaultSamplingRate);
    }

    /**
     * Finds a Device from the database by its id.
     *
     * @param id id of the Device to find.
     * @return the Device if it exists in the database, else null.
     */
    public static Device findDevice(int id) {
        Device device = (Device) findObjectByIdAndTable(id, "device", DeviceDAO.class);
        if(device != null) {
            List<Integer> tagIds = TagDAO.findTagIds(device.getId());
            device.setTagIds(tagIds);

            DeviceSecurityState ss = DeviceSecurityStateDAO.findDeviceSecurityStateByDevice(device.getId());
            device.setCurrentState(ss);
        }
        return device;
    }

    /**
     * Finds all Devices in the database.
     *
     * @return a list of all Devices in the database.
     */
    public static List<Device> findAllDevices() {
        List<Device> devices = new ArrayList<>();
        try {
            ResultSet rs = findAllFromTable("device");
            while (rs.next()) {
                Device d = createFromRs( rs);
                List<Integer> tagIds = TagDAO.findTagIds(d.getId());
                d.setTagIds(tagIds);
                DeviceSecurityState ss = DeviceSecurityStateDAO.findDeviceSecurityStateByDevice(d.getId());
                d.setCurrentState(ss);

                devices.add(d);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error getting all Devices: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return devices;
    }

    /**
     * Find devices in a given group
     *
     * @return a list of Devices with the given groupId
     * @parameter groupId the id of the group
     */
    public static List<Device> findDevicesByGroup(int groupId) {
        List<Device> devices = new ArrayList<>();
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("SELECT * FROM device WHERE group_id = ?")) {
            st.setInt(1, groupId);
            try(ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    Device d = createFromRs( rs);
                    d.setCurrentState(DeviceSecurityStateDAO.findDeviceSecurityStateByDevice(d.getId()));
                    devices.add(d);
                }
            }
        } catch (SQLException e) {
            logger.severe("Sql exception getting all devices for group: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return devices;
    }

    /**
     * Get a device given a device security state id.
     * @param dssId
     * @return
     */
    public static Device findDeviceByDeviceSecurityState(int dssId) {
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("SELECT device_id FROM device_security_state WHERE id = ?")) {
            st.setInt(1, dssId);

            try(ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("device_id");
                    Device d = findDevice(id);
                    d.setCurrentState(DeviceSecurityStateDAO.findDeviceSecurityStateByDevice(d.getId()));
                    return d;
                }
            }
        } catch (SQLException e) {
            logger.severe("Error while finding the device with device security state: "+dssId+".");
            logger.severe(e.getMessage());
        }
        return null;
    }

    /**
     * Finds the Device related to the given Alert id
     *
     * @param the Alert
     * @return the Device associated with the alert
     */
    public static Device findDeviceByAlert(Alert alert) {
        if (alert.getDeviceId() != null) {
            return findDevice(alert.getDeviceId());
        } else {
            logger.severe("Error: alert has no associated DeviceStatus OR UmboxInstance!");
            return null;
        }
    }

    /**
     * Finds the Device related to the given DeviceType id
     *
     * @param int id of the type
     * @return the Devices associated with the type
     */
    public static List<Device> findDevicesByType(int id) {
        List<Device> deviceList = new ArrayList<>();
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("SELECT * FROM device WHERE type_id = ?")) {
            st.setInt(1, id);
            try(ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    Device d = createFromRs( rs);
                    d.setCurrentState(DeviceSecurityStateDAO.findDeviceSecurityStateByDevice(d.getId()));
                    deviceList.add(d);
                }
            }
            return deviceList;
        } catch (SQLException e) {
            logger.severe("Sql exception getting the device for the alert: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Saves given Device to the database.
     *
     * @param device Device to be inserted.
     * @return auto incremented id
     */
    public static Device insertDevice(Device device) {
        logger.info("Inserting device: " + device);
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement
                ("INSERT INTO device(description, name, type_id, group_id, ip_address," +
                        "status_history_size, sampling_rate, default_sampling_rate) values(?,?,?,?,?,?,?,?) RETURNING id")) {
            st.setString(1, device.getDescription());
            st.setString(2, device.getName());
            st.setInt(3, device.getType().getId());
            if (device.getGroup() != null) {
                st.setInt(4, device.getGroup().getId());
            } else {
                st.setObject(4, null);
            }

            st.setString(5, device.getIp());
            st.setInt(6, device.getStatusHistorySize());
            st.setInt(7, device.getSamplingRate());
            st.setInt(8, device.getDefaultSamplingRate());
            st.execute();
            int serialNum = getLatestId(st);
            device.setId(serialNum);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error inserting Device: " + e.getClass().getName() + ": " + e.getMessage());
        }

        DeviceSecurityState currentState = device.getCurrentState();

        if(currentState == null) {
            // Give the device a normal security state if it is not specified
            SecurityState securityState = SecurityStateDAO.findByName("Normal");
            if (securityState != null) {
                DeviceSecurityState normalDeviceState = new DeviceSecurityState(device.getId(), securityState.getId(), securityState.getName());
                normalDeviceState.insert();
                device.setCurrentState(normalDeviceState);
            } else {
                throw new NoSuchElementException("No normal security state has been added to the database");
            }

            // Insert tags into device_tag
            List<Integer> tagIds = device.getTagIds();
            if (tagIds != null) {
                for (int tagId : tagIds) {
                    Postgres.executeCommand(String.format("INSERT INTO device_tag(device_id, tag_id) values (%d,%d)", device.getId(), tagId));
                }
            }
        }

        return device;
    }

    /**
     * First, attempts to find the Device in the database.
     * If successful, updates the existing Device with the given Device's parameters Otherwise,
     * inserts the given Device.
     *
     * @param device Device to be inserted or updated.
     */
    public static Device insertOrUpdateDevice(Device device) {
        Device d = findDevice(device.getId());
        if (d == null) {
            return insertDevice(device);
        } else {
            return updateDevice(device);
        }
    }

    /**
     * Updates Device with given id to have the parameters of the given Device.
     *
     * @param device Device holding new parameters to be saved in the database.
     * @return the id of the updated device
     */
    public static Device updateDevice(Device device) {
        logger.info(String.format("Updating Device with id = %d with values: %s", device.getId(), device));

        // Delete existing tags
        Postgres.executeCommand(String.format("DELETE FROM device_tag WHERE device_id = %d", device.getId()));

        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("UPDATE device " +
                "SET name = ?, description = ?, type_id = ?, group_id = ?, ip_address = ?, status_history_size = ?, sampling_rate = ? " +
                "WHERE id = ?")) {
            st.setString(1, device.getName());
            st.setString(2, device.getDescription());
            st.setInt(3, device.getType().getId());
            if (device.getGroup() != null)
                st.setInt(4, device.getGroup().getId());
            else
                st.setObject(4, null);
            st.setString(5, device.getIp());
            st.setInt(6, device.getStatusHistorySize());
            st.setInt(7, device.getSamplingRate());

            st.setInt(8, device.getId());
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error updating Device: " + e.getClass().getName() + ": " + e.getMessage());
        }

        // Insert tags into device_tag
        List<Integer> tagIds = device.getTagIds();
        if (tagIds != null) {
            for (int tagId : tagIds) {
                Postgres.executeCommand(String.format("INSERT INTO device_tag(device_id, tag_id) values (%d,%d)", device.getId(), tagId));
            }
        }
        return device;
    }

    /**
     * Deletes a Device by its id.
     *
     * @param id id of the Device to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static Boolean deleteDevice(int id) {
        logger.info(String.format("Deleting device with id = %d", id));
        return deleteById("device", id);
    }

    /**
     * inserts a state reset alert for the given device id
     */
    public static void resetSecurityState(int deviceId) {
        logger.info("Inserting a state reset alert for device id: " +deviceId);
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("SELECT name, id FROM alert_type WHERE name = ?;")) {
            st.setString(1, "state-reset");
            try(ResultSet rs = st.executeQuery()) {

                if (rs.next()) {
                    String name = rs.getString("name");
                    int alertTypeId = rs.getInt("id");

                    Alert alert = new Alert(deviceId, name, alertTypeId, "State reset");
                    alert.insert();
                }
            }
        } catch (SQLException e) {
            logger.severe("Sql exception inserting state reset alert: " + e.getClass().getName() + ": " + e.getMessage());
        }
    }
    
}
