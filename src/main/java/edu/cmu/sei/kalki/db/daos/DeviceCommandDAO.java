package edu.cmu.sei.kalki.db.daos;

import edu.cmu.sei.kalki.db.database.Postgres;
import edu.cmu.sei.kalki.db.models.DeviceCommand;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DeviceCommandDAO extends DAO
{
    /**
     * Extract a Command name from the result set of a database query.
     */
    public static DeviceCommand createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) return null;
        int id = rs.getInt("id");
        String name = rs.getString("name");
        int deviceTypeId = rs.getInt("device_type_id");
        return new DeviceCommand(id, name, deviceTypeId);
    }

    /**
     * Finds a command based on the given id
     */
    public static DeviceCommand findCommand(int id) {
        return (DeviceCommand) findObjectByIdAndTable(id,"command", DeviceCommandDAO.class);
    }

    /**
     * Finds all rows in the command table
     */
    public static List<DeviceCommand> findAllCommands() {
        return (List<DeviceCommand>) findObjects("command", DeviceCommandDAO.class);
    }

    /**
     * Finds the commands for the device from the policy rule log
     *
     * @param policyRuleId The policy rule id
     * @return commands A list of command names
     */
    public static List<DeviceCommand> findCommandsByPolicyRuleLog(int policyRuleId) {
        String query = "SELECT c.id, c.name, c.device_type_id FROM command_lookup AS cl, command AS c, policy_rule_log AS pi " +
                "WHERE pi.policy_rule_id = cl.policy_rule_id AND c.id = cl.command_id AND pi.id = ?";
        return (List<DeviceCommand>) findObjectsByIdAndQuery(policyRuleId, query, DeviceCommandDAO.class);
    }

    /**
     * insert a command into the db
     */
    public static Integer insertCommand(DeviceCommand command) {
        logger.info("Inserting command: " + command.getName());
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("INSERT INTO command(name, device_type_id) VALUES (?,?) RETURNING id")) {
            st.setString(1, command.getName());
            st.setInt(2, command.getDeviceTypeId());
            st.execute();
            return getLatestId(st);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error inserting Command: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return -1;
    }

    /**
     * Updates the command entry
     *
     * @input the command to update
     */
    public static Integer updateCommand(DeviceCommand command) {
        logger.info("Updating command; commandId: " +command.getId()+ " name: " +command.getName());
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("UPDATE command SET name = ?, device_type_id = ? WHERE id = ?")) {
            st.setString(1, command.getName());
            st.setInt(2, command.getDeviceTypeId());
            st.setInt(3, command.getId());
            st.executeUpdate();

            return command.getId();
        } catch (SQLException e) {
            logger.severe("Error updating Command: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * First, attempts to find the Command id in the database.
     * If successful, updates the existing Command with the given Device Commands's parameters Otherwise,
     * inserts the given Device Command as a Command Lookup.
     */
    public static Integer insertOrUpdateCommand(DeviceCommand command) {
        DeviceCommand dc = findCommand(command.getId());
        if (dc == null) {
            return insertCommand(command);
        } else {
            return updateCommand(command);
        }
    }

    /**
     * Deletes a Command by its id.
     *
     * @param id id of the Command Lookup to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static Boolean deleteCommand(int id) {
        logger.info(String.format("Deleting command with id = %d", id));
        return deleteById("command", id);
    }    
}
