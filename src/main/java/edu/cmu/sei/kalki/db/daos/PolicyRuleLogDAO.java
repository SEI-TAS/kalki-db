package edu.cmu.sei.kalki.db.daos;

import edu.cmu.sei.kalki.db.database.Postgres;
import edu.cmu.sei.kalki.db.models.PolicyRuleLog;
import edu.cmu.sei.kalki.db.models.StateTransition;

import java.sql.Connection;
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
        return (PolicyRuleLog) findObjectByIdAndTable(id, "policy_rule_log", PolicyRuleLogDAO.class);
    }

    /**
     * Inserts a PolicyRuleLog to the policy_rule_log table
     * @param policyRuleLog
     * @return
     */
    public static Integer insertPolicyRuleLog(PolicyRuleLog policyRuleLog) {
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("INSERT INTO policy_rule_log(policy_rule_id, device_id, timestamp) VALUES(?,?,?) RETURNING id")) {
            st.setInt(1, policyRuleLog.getPolicyRuleId());
            st.setInt(2, policyRuleLog.getDeviceId());
            st.setTimestamp(3, policyRuleLog.getTimestamp());
            st.execute();
            return getLatestId(st);
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
