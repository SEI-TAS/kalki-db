package edu.cmu.sei.kalki.db.daos;

import edu.cmu.sei.kalki.db.database.Postgres;
import edu.cmu.sei.kalki.db.models.DeviceCommandLookup;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DeviceCommandLookupDAO extends DAO
{
    /**
     * Extract a Command from the result set of a database query.
     */
    public static DeviceCommandLookup createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) { return null; }
        int id = rs.getInt("id");
        int commandId = rs.getInt("command_id");
        int policyRuleId = rs.getInt("policy_rule_id");
        return new DeviceCommandLookup(id, commandId, policyRuleId);
    }

    /**
     * Finds a command lookup based on the given id
     */
    public static DeviceCommandLookup findCommandLookup(int id) {
        logger.info("Finding command lookup with id = " + id);
        ResultSet rs = findById(id, "command_lookup");
        DeviceCommandLookup deviceCommandLookup = null;
        try {
            deviceCommandLookup = createFromRs(rs);
        } catch (SQLException e) {
            logger.severe("Sql exception creating object");
            e.printStackTrace();
        }
        closeResources(rs);
        return deviceCommandLookup;
    }

    /**
     * Finds all command lookups based on the given device id
     */
    public static List<DeviceCommandLookup> findCommandLookupsByDevice(int deviceId) {
        List<DeviceCommandLookup> lookupList = new ArrayList<>();
        try(PreparedStatement st = Postgres.prepareStatement("SELECT cl.* FROM command_lookup cl, device d, command c " +
                "WHERE d.id = ? AND c.device_type_id = d.type_id AND c.id=cl.command_id")) {
            st.setInt(1,deviceId);
            try(ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    lookupList.add(createFromRs(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Exception finding umbox lookup: " + e.getClass().getName() + ": " + e.getMessage());
        }

        return lookupList;
    }

    /**
     * Finds all rows in the command lookup table
     */
    public static List<DeviceCommandLookup> findAllCommandLookups() {
        return (List<DeviceCommandLookup>) findAll("command_lookup", DeviceCommandLookupDAO.class);
    }

    /**
     * inserts into command lookup to relate a state and command
     *
     * @return -1 on failure, 1 on success
     */
    public static int insertCommandLookup(DeviceCommandLookup commandLookup) {
        logger.info("Inserting command lookup; commandId: "+ commandLookup.getCommandId());
        try(PreparedStatement insertCommandLookup =
                    Postgres.prepareStatement("INSERT INTO command_lookup(command_id, policy_rule_id) VALUES (?,?)")) {
            insertCommandLookup.setInt(1, commandLookup.getCommandId());
            insertCommandLookup.setInt(2, commandLookup.getPolicyRuleId());
            insertCommandLookup.executeUpdate();

            return getLatestId("command_lookup");
        } catch (SQLException e) {
            logger.severe("Error inserting CommandLookup: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * First, attempts to find the Command Lookup id in the database.
     * If successful, updates the existing Command Lookup with the given Device Commands's parameters Otherwise,
     * inserts the given Device Command as a Command Lookup.
     */
    public static Integer insertOrUpdateCommandLookup(DeviceCommandLookup commandLookup) {
        DeviceCommandLookup cl = findCommandLookup(commandLookup.getId());
        if (cl == null) {
            return insertCommandLookup(commandLookup);
        } else {
            return updateCommandLookup(commandLookup);
        }
    }

    /**
     * Updates the command lookup entry
     *
     * @input the command lookup id to update
     * @input the device command to update
     */

    public static Integer updateCommandLookup(DeviceCommandLookup commandLookup) {
        logger.info("Updating command lookup; commandId: " +commandLookup.getCommandId());
        try(PreparedStatement updatecommand = Postgres.prepareStatement("UPDATE command_lookup SET command_id = ?, policy_rule_id = ? WHERE id = ?")) {
            updatecommand.setInt(1, commandLookup.getCommandId());
            updatecommand.setInt(2, commandLookup.getPolicyRuleId());
            updatecommand.setInt(3, commandLookup.getId());
            updatecommand.executeUpdate();

            return commandLookup.getId();
        } catch (SQLException e) {
            logger.severe("Error inserting Command: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Deletes a Command Lookup by its id.
     *
     * @param id id of the Command Lookup to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static Boolean deleteCommandLookup(int id) {
        logger.info(String.format("Deleting command lookup with id = %d", id));
        return deleteById("command_lookup", id);
    }
    
}
