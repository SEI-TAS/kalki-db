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
        int stateTransId = rs.getInt("state_trans_id");
        int policyCondId = rs.getInt("policy_cond_id");
        int devTypeId = rs.getInt("device_type_id");
        int samplingRate = rs.getInt("sampling_rate_factor");
        return new PolicyRule(id, stateTransId, policyCondId, devTypeId, samplingRate);
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
     * @param stateTransId
     * @param policyCondId
     * @param devTypeId
     * @return
     */
    public static PolicyRule findPolicyRule(int stateTransId, int policyCondId, int devTypeId) {
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("SELECT * FROM policy_rule WHERE " +
                "state_trans_id = ? AND " +
                "policy_cond_id = ? AND " +
                "(device_type_id = ? OR device_type_id IS NULL)")) {
            st.setInt(1, stateTransId);
            st.setInt(2, policyCondId);
            st.setInt(3, devTypeId);

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
     * @param devTypeId
     * @return
     */
    public static List<PolicyRule> findPolicyRules(int devTypeId) {
        String query = "SELECT * FROM policy_rule WHERE device_type_id = ? OR device_type_id IS NULL";
        return (List<PolicyRule>) findObjectsByIdAndQuery(devTypeId, query, PolicyRuleDAO.class);
    }

    /**
     * Inserts the given PolicyRule obj to the policy table
     * @param policyRule The obj to insert
     * @return Row's id on success. -1 otherwise
     */
    public static Integer insertPolicyRule(PolicyRule policyRule) {
        try(Connection con = Postgres.getConnection();
        PreparedStatement st = con.prepareStatement("INSERT INTO policy_rule(state_trans_id, policy_cond_id, device_type_id, sampling_rate_factor) VALUES(?,?,?,?) RETURNING id")) {
            st.setInt(1, policyRule.getStateTransId());
            st.setInt(2, policyRule.getPolicyCondId());
            st.setInt(3, policyRule.getDevTypeId());
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
            st.setInt(1, policyRule.getStateTransId());
            st.setInt(2, policyRule.getPolicyCondId());
            st.setInt(3, policyRule.getDevTypeId());
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
