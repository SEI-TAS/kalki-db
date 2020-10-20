package edu.cmu.sei.kalki.db.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
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
     * Updates DeviceSensor with given id to have the parameters of the given DeviceSensor.
     * @param type DeviceSensor holding new parameters to be saved in the database.
     */
    public static DeviceSensor updateDeviceSensor(DeviceSensor sensor) {
        logger.info("Updating DeviceSensor with id=" + sensor.getId());
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement
                    ("UPDATE device_sensor SET name = ? " +
                            "WHERE id=?")) {
            st.setString(1, sensor.getName());
            st.setInt(2, sensor.getId());
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error updating DeviceSensor: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return sensor;
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

        // Remove deleted sensors.
        List<DeviceSensor> sensorsToDelete = new ArrayList<>();
        List<DeviceSensor> oldSensors = findSensorsForDeviceType(type.getId());
        for (DeviceSensor oldSensor : oldSensors) {
            boolean oldSensorFound = false;
            for (DeviceSensor currentSensor : type.getSensors()) {
                if(oldSensor.getId() == currentSensor.getId()) {
                    oldSensorFound = true;
                    break;
                }
            }
            if(!oldSensorFound) {
                sensorsToDelete.add(oldSensor);
            }
        }

        // Actually delete all sensors that were not sent back with the request.
        if(!sensorsToDelete.isEmpty()) {
            StringBuilder inClauseBuilder = new StringBuilder();
            for(int i=0; i<sensorsToDelete.size(); i++) {
                inClauseBuilder.append("?,");
            }
            String inClause = inClauseBuilder.substring(0, inClauseBuilder.length()-1);

            try (Connection con = Postgres.getConnection();
                 PreparedStatement st = con.prepareStatement("DELETE FROM device_sensor WHERE id IN (" + inClause + ")")) {
                for(int i=0; i<sensorsToDelete.size(); i++) {
                    st.setInt(i+1, sensorsToDelete.get(i).getId());
                }
                st.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                logger.severe("Error deleting DeviceSensors: " + e.getClass().getName() + ": " + e.getMessage());
            }
        }

        // Create a temp devicetype object with only the new sensors to be inserted.
        DeviceType typeWithNewSensors = new DeviceType();
        typeWithNewSensors.setId(type.getId());
        typeWithNewSensors.setName(type.getName());
        List<DeviceSensor> newSensors = new ArrayList<>();
        typeWithNewSensors.setSensors(newSensors);
        for(DeviceSensor sensor : type.getSensors()) {
            if(sensor.getId() == 0) {
                newSensors.add(sensor);
            }
        }
        insertDeviceSensorForDeviceType(typeWithNewSensors);

        // Return the updated object will all sensors with proper ids.
        return DeviceTypeDAO.findDeviceType(type.getId());
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
