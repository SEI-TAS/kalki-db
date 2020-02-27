package edu.cmu.sei.kalki.db.daos;

import edu.cmu.sei.kalki.db.database.Postgres;
import edu.cmu.sei.kalki.db.models.PolicyRuleLog;
import edu.cmu.sei.kalki.db.models.StateTransition;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PolicyRuleLogDAO extends DAO
{
    /**
     * Finds a row in the policy_rule_log table with the given id
     * @param id
     * @return
     */
    public static PolicyRuleLog findPolicyRuleLog(int id) {
        ResultSet rs = findById(id, "policy_rule_log");
        PolicyRuleLog policyRuleLog = (PolicyRuleLog) createFromRs(PolicyRuleLog.class, rs);
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
