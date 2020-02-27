package edu.cmu.sei.kalki.db.daos;

import edu.cmu.sei.kalki.db.database.Postgres;
import edu.cmu.sei.kalki.db.models.DeviceCommand;

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
        ResultSet rs = findById(id, "command");
        DeviceCommand deviceCommand = null;
        try {
            deviceCommand = createFromRs(rs);
        } catch (SQLException e) {
            logger.severe("Sql exception creating object");
            e.printStackTrace();
        }
        closeResources(rs);
        return deviceCommand;
    }

    /**
     * Finds all rows in the command table
     */
    public static List<DeviceCommand> findAllCommands() {
        return (List<DeviceCommand>) findAll("command", DeviceCommandDAO.class);
    }

    /**
     * Finds the commands for the device from the policy rule log
     *
     * @param policyRuleId The policy rule id
     * @return commands A list of command names
     */
    public static List<DeviceCommand> findCommandsByPolicyRuleLog(int policyRuleId) {
        List<DeviceCommand> commands = new ArrayList<>();
        try(PreparedStatement st = Postgres.prepareStatement("SELECT c.id, c.name, c.device_type_id FROM command_lookup AS cl, command AS c, policy_rule_log AS pi " +
                "WHERE pi.policy_rule_id = cl.policy_rule_id AND c.id = cl.command_id AND pi.id = ?")) {
            st.setInt(1, policyRuleId);
            try(ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    commands.add(createFromRs(rs));
                }
            }
            return commands;
        } catch (SQLException e) {
            logger.severe("Sql exception getting all commands for device: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * insert a command into the db
     */
    public static Integer insertCommand(DeviceCommand command) {
        logger.info("Inserting command: " + command.getName());
        try(PreparedStatement insertCommand = Postgres.prepareStatement("INSERT INTO command(name, device_type_id) VALUES (?,?);")) {
            insertCommand.setString(1, command.getName());
            insertCommand.setInt(2, command.getDeviceTypeId());
            insertCommand.executeUpdate();
            return getLatestId("command");
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
        try(PreparedStatement updatecommand = Postgres.prepareStatement("UPDATE command SET name = ?, device_type_id = ? WHERE id = ?")) {
            updatecommand.setString(1, command.getName());
            updatecommand.setInt(2, command.getDeviceTypeId());
            updatecommand.setInt(3, command.getId());
            updatecommand.executeUpdate();

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
