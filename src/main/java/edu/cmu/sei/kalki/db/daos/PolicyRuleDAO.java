package edu.cmu.sei.kalki.db.daos;

import edu.cmu.sei.kalki.db.database.Postgres;
import edu.cmu.sei.kalki.db.models.PolicyRule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class PolicyRuleDAO extends DAO
{
    /**
     * Converts a ResultSet obj to a Policy
     */
    public static PolicyRule createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) return null;
        int id = rs.getInt("id");
        int stateTransitionId = rs.getInt("state_trans_id");
        int policyConditionId = rs.getInt("policy_cond_id");
        int deviceTypeId = rs.getInt("device_type_id");
        int samplingRate = rs.getInt("sampling_rate_factor");
        return new PolicyRule(id, stateTransitionId, policyConditionId, deviceTypeId, samplingRate);
    }

    /**
     * Returns a policy rule give its id.
     * @param id
     * @return
     */
    public static PolicyRule findPolicyRule(int id) {
        return (PolicyRule) findObjectByIdAndTable(id, "policy_rule", PolicyRuleDAO.class);
    }

    /**
     * Finds all PolicyRules in the database.
     *
     * @return a list of all PolicyRules in the database.
     */
    public static List<PolicyRule> findAllPolicyRules() {
        return (List<PolicyRule>) findObjectsByTable("policy_rule", PolicyRuleDAO.class);
    }

    /**
     * Finds the policy rule given the StateTransition PolicyCondition and DeviceType id's
     * @param stateTransitionId
     * @param policyConditionId
     * @param deviceTypeId
     * @return
     */
    public static PolicyRule findPolicyRule(int stateTransitionId, int policyConditionId, int deviceTypeId) {
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("SELECT * FROM policy_rule WHERE " +
                "state_trans_id = ? AND " +
                "policy_cond_id = ? AND " +
                "(device_type_id = ? OR device_type_id IS NULL)")) {
            st.setInt(1, stateTransitionId);
            st.setInt(2, policyConditionId);
            st.setInt(3, deviceTypeId);

            ResultSet rs = st.executeQuery();
            if(rs.next()) {
                return createFromRs(rs);
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
     * @param deviceTypeId
     * @return
     */
    public static List<PolicyRule> findPolicyRules(int deviceTypeId) {
        String query = "SELECT * FROM policy_rule WHERE device_type_id = ? OR device_type_id IS NULL";
        return (List<PolicyRule>) findObjectsByIdAndQuery(deviceTypeId, query, PolicyRuleDAO.class);
    }

    /**
     * Inserts the given PolicyRule obj to the policy table
     * @param policyRule The obj to insert
     * @return Row's id on success. -1 otherwise
     */
    public static Integer insertPolicyRule(PolicyRule policyRule) {
        try(Connection con = Postgres.getConnection();
        PreparedStatement st = con.prepareStatement("INSERT INTO policy_rule(state_trans_id, policy_cond_id, device_type_id, sampling_rate_factor) VALUES(?,?,?,?) RETURNING id")) {
            st.setInt(1, policyRule.getStateTransitionId());
            st.setInt(2, policyRule.getPolicyConditionId());
            st.setInt(3, policyRule.getDeviceTypeId());
            st.setInt(4, policyRule.getSamplingRateFactor());
            st.execute();
            return getLatestId(st);
        } catch (SQLException e) {
            logger.severe("Error inserting Policy: "+e.getClass().getName() +": "+e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * First, attempts to find the Policy Rule in the database.
     * If successful, updates the existing Policy Rule with the given Policy Rule's parameters Otherwise,
     * inserts the given Policy Rule.
     * @param policyRule Policy Rule to be inserted or updated.
     */
    public static Integer insertOrUpdatePolicyRule(PolicyRule policyRule) {
        PolicyRule pr = findPolicyRule(policyRule.getId());
        if (pr == null) {
            return insertPolicyRule(policyRule);
        } else {
            return updatePolicyRule(policyRule);
        }
    }

    /**
     * Updates the row in the policy table with the given id
     * @param policyRule
     * @return The id of the given policy on success. -1 otherwise
     */
    public static Integer updatePolicyRule(PolicyRule policyRule) {
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("UPDATE policy_rule SET " +
                "state_trans_id = ? " +
                ", policy_cond_id = ? " +
                ", device_type_id = ? " +
                ", sampling_rate_factor = ? " +
                "WHERE id = ?")) {
            st.setInt(1, policyRule.getStateTransitionId());
            st.setInt(2, policyRule.getPolicyConditionId());
            st.setInt(3, policyRule.getDeviceTypeId());
            st.setInt(4, policyRule.getSamplingRateFactor());
            st.setInt(5, policyRule.getId());
            st.executeUpdate();
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
