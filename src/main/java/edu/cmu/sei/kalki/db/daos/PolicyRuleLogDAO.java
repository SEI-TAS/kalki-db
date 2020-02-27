package edu.cmu.sei.kalki.db.daos;

import edu.cmu.sei.kalki.db.database.Postgres;
import edu.cmu.sei.kalki.db.models.PolicyRuleLog;
import edu.cmu.sei.kalki.db.models.StateTransition;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class PolicyRuleLogDAO extends DAO
{
    /**
     * Converts a ResultSet from a query on policy rule log to a java PolicyRuleLog
     */
    public static PolicyRuleLog createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) { return null; }
        int id = rs.getInt("id");
        int policyRuleId = rs.getInt("policy_rule_id");
        int deviceId = rs.getInt("device_id");
        Timestamp timestamp = rs.getTimestamp("timestamp");
        return new PolicyRuleLog(id, policyRuleId, deviceId, timestamp);
    }
    
    /**
     * Finds a row in the policy_rule_log table with the given id
     * @param id
     * @return
     */
    public static PolicyRuleLog findPolicyRuleLog(int id) {
        ResultSet rs = findById(id, "policy_rule_log");
        PolicyRuleLog policyRuleLog = null;
        try {
            policyRuleLog = createFromRs(rs);
        } catch (SQLException e) {
            logger.severe("Sql exception creating object");
            e.printStackTrace();
        }
        closeResources(rs);
        return policyRuleLog;
    }

    /**
     * Inserts a PolicyRuleLog to the policy_rule_log table
     * @param policyRuleLog
     * @return
     */
    public static Integer insertPolicyRuleLog(PolicyRuleLog policyRuleLog) {
        try(PreparedStatement insert = Postgres.prepareStatement("INSERT INTO policy_rule_log(policy_rule_id, device_id, timestamp) VALUES(?,?,?)")) {
            insert.setInt(1, policyRuleLog.getPolicyRuleId());
            insert.setInt(2, policyRuleLog.getDeviceId());
            insert.setTimestamp(3, policyRuleLog.getTimestamp());
            insert.executeUpdate();
            return getLatestId("policy_rule_log");
        } catch (SQLException e) {
            logger.severe("Error inserting StateTransition: "+e.getClass().getName() +": "+e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Deletes a row in the policy_rule_log table with the given id
     * @param id
     * @return
     */
    public static boolean deletePolicyRuleLog(int id) {
        return deleteById("policy_rule_log", id);
    }
}
