package edu.cmu.sei.kalki.db.daos;

import edu.cmu.sei.kalki.db.database.Postgres;
import edu.cmu.sei.kalki.db.models.DeviceType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DeviceTypeDAO extends DAO
{
    /**
     * Extract a DeviceType from the result set of a database query.
     */
    public static DeviceType createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) return null;
        int id = rs.getInt("id");
        String name = rs.getString("name");
        byte[] policyFile = rs.getBytes("policy_file");
        String policyFileName = rs.getString("policy_file_name");
        return new DeviceType(id, name, policyFile, policyFileName);
    }

    /**
     * Finds a DeviceType from the database by its id.
     *
     * @param id id of the DeviceType to find.
     * @return the DeviceType if it exists in the database, else null.
     */
    public static DeviceType findDeviceType(int id) {
        return (DeviceType) findObjectByIdAndTable(id, "device_type", DeviceTypeDAO.class);
    }

    /**
     * Finds all DeviceTypes in the database.
     *
     * @return a list of all DeviceTypes in the database.
     */
    public static List<DeviceType> findAllDeviceTypes() {
        return (List<DeviceType>) findObjectsByTable("device_type", DeviceTypeDAO.class);
    }

    /**
     * Saves given DeviceType to the database.
     *
     * @param type DeviceType to be inserted.
     * @return auto incremented id
     */
    public static Integer insertDeviceType(DeviceType type) {
        logger.info("Inserting DeviceType: " + type.getName());
        try(Connection con = Postgres.getConnection();
        PreparedStatement st = con.prepareStatement
                ("INSERT INTO device_type(name, policy_file, policy_file_name)" +
                        "values(?,?,?) RETURNING id")) {
            st.setString(1, type.getName());
            st.setBytes(2, type.getPolicyFile());
            st.setString(3, type.getPolicyFileName());
            st.execute();
            return getLatestId(st);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error inserting DeviceType: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return -1;
    }

    /**
     * Updates DeviceType with given id to have the parameters of the given DeviceType.
     *
     * @param type DeviceType holding new parameters to be saved in the database.
     */
    public static Integer updateDeviceType(DeviceType type) {
        logger.info("Updating DeviceType with id=" + type.getId());
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement
                ("UPDATE device_type SET name = ?, policy_file = ?, policy_file_name = ?" +
                        "WHERE id=?")) {
            st.setString(1, type.getName());
            st.setBytes(2, type.getPolicyFile());
            st.setString(3, type.getPolicyFileName());
            st.setInt(4, type.getId());
            st.executeUpdate();
            return type.getId();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error updating DeviceType: " + e.getClass().getName() + ": " + e.getMessage());
            return -1;
        }
    }

    /**
     * First, attempts to find the DeviceType in the database.
     * If successful, updates the existing DeviceType with the given DeviceType's parameters Otherwise,
     * inserts the given DeviceType.
     *
     * @param type DeviceType to be inserted or updated.
     */
    public static Integer insertOrUpdateDeviceType(DeviceType type) {
        DeviceType dt = findDeviceType(type.getId());
        if (dt == null) {
            return insertDeviceType(type);
        } else {
            return updateDeviceType(type);
        }
    }

    /**
     * Deletes a DeviceType by its id.
     *
     * @param id id of the DeviceType to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static Boolean deleteDeviceType(int id) {
        return deleteById("device_type", id);
    }
    
}
