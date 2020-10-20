package edu.cmu.sei.kalki.db.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

import edu.cmu.sei.kalki.db.database.Postgres;
import edu.cmu.sei.kalki.db.models.DeviceSensor;
import edu.cmu.sei.kalki.db.models.DeviceType;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DeviceSensorDAO extends DAO
{

    /**
     * Extract a DeviceSensor from the result set of a database query
     */
    public static DeviceSensor createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) return null;

        int id = rs.getInt("id");
        String name = rs.getString("name");
        int typeId = rs.getInt("type_id");

        return new DeviceSensor(id, name, typeId);
    }

    /**
     * Finds a list of DeviceSensors associated with the given DeviceType's id
     * @param typeId The id of the device type
     * @return A list of DeviceSensors
     */
    public static List<DeviceSensor> findSensorsForDeviceType(int typeId) {
        String query = "SELECT * from device_sensor WHERE type_id = ?";
        return (List<DeviceSensor>) findObjectsByIdAndQuery(typeId, query, DeviceSensorDAO.class);
    }

    /**
     * Finds a row in the DeviceSensorTable
     */
    public static DeviceSensor findDeviceSensor(int id) {
       return (DeviceSensor) findObjectByIdAndTable(id, "device_sensor", DeviceSensorDAO.class);
    }

    /**
     * Inserts a row into the DeviceSensor table
     */
    public static Integer insertDeviceSensor(DeviceSensor sensor) {
        logger.info("Inserting DeviceSensor: "+sensor.getName());
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("INSERT INTO device_sensor(name, type_id) VALUES (?,?) RETURNING id")) {
            st.setString(1, sensor.getName());
            st.setInt(2, sensor.getTypeId());
            st.execute();
            return getLatestId(st);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error inserting DeviceSensor: "+e.getClass().getName()+": "+e.getMessage());
        }
        return -1;
    }

    /**
     * Inserts DeviceSensors for a DeviceType
     * @param type The DeviceType with associated DeviceSensors
     * @return
     */
    public static DeviceType insertDeviceSensorForDeviceType(DeviceType type) {
        logger.info("Inserting DeviceSensors for deviceType: " + type.getName());
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement
                    ("INSERT INTO device_sensor(name, type_id)" +
                            "values(?,?) RETURNING id")) {
            for(DeviceSensor sensor: type.getSensors()) {
                st.setString(1, sensor.getName());
                st.setInt(2, type.getId());
                st.execute();
                int id = getLatestId(st);
                sensor.setId(id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error inserting DeviceSensors: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return type;
    }

    /**
     * Updates DeviceSensors for a DeviceType
     * @param type The DeviceType with associated DeviceSensors
     * @return
     */
    public static DeviceType updateDeviceSensorForDeviceType(DeviceType type) {
        logger.info("Updating DeviceSensors for deviceType: " + type.getName());

        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("DELETE FROM device_sensor WHERE type_id=?")){
            st.setInt(1, type.getId());
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error deleting DeviceSensors: " + e.getClass().getName() + ": " + e.getMessage());
        }

        return insertDeviceSensorForDeviceType(type);
    }


    /**
     * Deletes a DeviceSensor by its id.
     *
     * @param id id of the DeviceSensor to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static Boolean deleteDeviceSensor(int id) {
        return deleteById("device_sensor", id);
    }

}
