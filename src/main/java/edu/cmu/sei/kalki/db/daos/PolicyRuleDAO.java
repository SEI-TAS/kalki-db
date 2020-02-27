package edu.cmu.sei.kalki.db.daos;

import edu.cmu.sei.kalki.db.database.Postgres;
import edu.cmu.sei.kalki.db.models.PolicyRule;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PolicyRuleDAO extends DAO
{
    public static PolicyRule findPolicyRule(int id) {
        ResultSet rs = findById(id, "policy_rule");
        PolicyRule policyRule = (PolicyRule) createFromRs(PolicyRule.class, rs);
        closeResources(rs);
        return policyRule;
    }

    /**
     * Finds the policy rule given the StateTransition PolicyCondition and DeviceType id's
     * @param stateTransId
     * @param policyCondId
     * @param devTypeId
     * @return
     */
    public static PolicyRule findPolicyRule(int stateTransId, int policyCondId, int devTypeId) {
        try(PreparedStatement query = Postgres.prepareStatement("SELECT * FROM policy_rule WHERE " +
                "state_trans_id = ? AND " +
                "policy_cond_id = ? AND " +
                "device_type_id = ?")) {
            query.setInt(1, stateTransId);
            query.setInt(2, policyCondId);
            query.setInt(3, devTypeId);

            ResultSet rs = query.executeQuery();
            if(rs.next()) {
                return (PolicyRule) createFromRs(PolicyRule.class, rs);
            }
        } catch (SQLException e) {
            logger.severe("Error finding Policy Rule: "+e.getClass().getName() +": "+e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Finds all policy rules given the DeviceType ids
     * @param securityStateId
     * @param devTypeId
     * @return
     */
    public static List<PolicyRule> findPolicyRules(int devTypeId) {
        try(PreparedStatement query = Postgres.prepareStatement("SELECT * " +
                "FROM policy_rule WHERE " +
                "device_type_id = ?")) {
            query.setInt(1, devTypeId);

            try(ResultSet rs = query.executeQuery()) {
                List<PolicyRule> rules = new ArrayList<>();
                while (rs.next()) {
                    rules.add((PolicyRule) createFromRs(PolicyRule.class, rs));
                }
                return rules;
            }
        } catch (SQLException e) {
            logger.severe("Error finding Policy Rule: "+e.getClass().getName() +": "+e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Inserts the given PolicyRule obj to the policy table
     * @param policyRule The obj to insert
     * @return Row's id on success. -1 otherwise
     */
    public static Integer insertPolicyRule(PolicyRule policyRule) {
        try(PreparedStatement insert = Postgres.prepareStatement("INSERT INTO policy_rule(state_trans_id, policy_cond_id, device_type_id, sampling_rate) VALUES(?,?,?,?)")) {
            insert.setInt(1, policyRule.getStateTransId());
            insert.setInt(2, policyRule.getPolicyCondId());
            insert.setInt(3, policyRule.getDevTypeId());
            insert.setInt(4, policyRule.getSamplingRate());
            insert.executeUpdate();
            return getLatestId("policy_rule");
        } catch (SQLException e) {
            logger.severe("Error inserting Policy: "+e.getClass().getName() +": "+e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Updates the row in the policy table with the given id
     * @param policyRule
     * @return The id of the given policy on success. -1 otherwise
     */
    public static Integer updatePolicyRule(PolicyRule policyRule) {
        try(PreparedStatement update = Postgres.prepareStatement("UPDATE policy_rule SET " +
                "state_trans_id = ? " +
                ", policy_cond_id = ? " +
                ", device_type_id = ? " +
                ", sampling_rate = ? " +
                "WHERE id = ?")) {
            update.setInt(1, policyRule.getStateTransId());
            update.setInt(2, policyRule.getPolicyCondId());
            update.setInt(3, policyRule.getDevTypeId());
            update.setInt(4, policyRule.getSamplingRate());
            update.setInt(5, policyRule.getId());
            update.executeUpdate();
            return policyRule.getId();
        } catch (SQLException e) {
            logger.severe("Error updating Policy: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /***
     * Delete a row in the policy rule table with the given id
     * @param policyRuleId
     * @return True on success, false otherwise
     */
    public static boolean deletePolicyRule(int policyRuleId) {
        return deleteById("policy_rule", policyRuleId);
    }
    
}
