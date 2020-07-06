package edu.cmu.sei.kalki.db.daos;

import edu.cmu.sei.kalki.db.database.Postgres;
import edu.cmu.sei.kalki.db.models.DeviceCommandLookup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
        return (DeviceCommandLookup) findObjectByIdAndTable(id, "command_lookup", DeviceCommandLookupDAO.class);
    }

    /**
     * Finds all command lookups based on the given device id
     */
    public static List<DeviceCommandLookup> findCommandLookupsByDevice(int deviceId) {
        String query = "SELECT cl.* FROM command_lookup cl, device d, command c WHERE d.id = ? AND c.device_type_id = d.type_id AND c.id=cl.command_id";
        return (List<DeviceCommandLookup>) findObjectsByIdAndQuery(deviceId, query, DeviceCommandLookupDAO.class);
    }

    /**
     * Finds all rows in the command lookup table
     */
    public static List<DeviceCommandLookup> findAllCommandLookups() {
        return (List<DeviceCommandLookup>) findObjectsByTable("command_lookup", DeviceCommandLookupDAO.class);
    }

    /**
     * inserts into command lookup to relate a state and command
     *
     * @return -1 on failure, 1 on success
     */
    public static int insertCommandLookup(DeviceCommandLookup commandLookup) {
        logger.info("Inserting command lookup; commandId: "+ commandLookup.getCommandId());
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("INSERT INTO command_lookup(command_id, policy_rule_id) VALUES (?,?) RETURNING id")) {
            st.setInt(1, commandLookup.getCommandId());
            st.setInt(2, commandLookup.getPolicyRuleId());
            st.execute();

            return getLatestId(st);
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
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("UPDATE command_lookup SET command_id = ?, policy_rule_id = ? WHERE id = ?")) {
            st.setInt(1, commandLookup.getCommandId());
            st.setInt(2, commandLookup.getPolicyRuleId());
            st.setInt(3, commandLookup.getId());
            st.executeUpdate();

            return commandLookup.getId();
        } catch (SQLException e) {
            logger.severe("Error inserting Command: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Deletes all Command Lookups by a policy rule id.
     *
     * @param id id of the Policy Rule to delete Command Lookups by.
     * @return true if the deletions succeeded, false otherwise.
     */
    public static Boolean deleteCommandLookupsByPolicyRule(int id) {
        logger.info(String.format("Deleting command lookups associated with policy rule with id = %d", id));
        try (Connection con = Postgres.getConnection();
             PreparedStatement st = con.prepareStatement("DELETE FROM command_lookup WHERE policy_rule_id=?")) {
            st.setInt(1, id);
            st.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.severe("Error deleting id: " + id + " from table: command_lookup. " + e.getMessage());
            e.printStackTrace();
        }
        return false;
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
