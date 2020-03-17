package edu.cmu.sei.kalki.db.daos;

import edu.cmu.sei.kalki.db.database.Postgres;
import edu.cmu.sei.kalki.db.models.PolicyCondition;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PolicyConditionDAO extends DAO
{
    /**
     * Converts a ResultSet obj to a PolicyCondition
     */
    public static PolicyCondition createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) { return null; }
        int id = rs.getInt("id");
        int threshold = rs.getInt("threshold");
        return new PolicyCondition(id, threshold, null);
    }
    
    /**
     * Find a PolicyCondition and its associated AlertType ids
     * @param id
     * @return A PolicyCondition obj. Null otherwise
     */
    public static PolicyCondition findPolicyCondition(int id) {
        try {
            PolicyCondition policyCondition = (PolicyCondition) findObjectByIdAndTable(id, "policy_condition", PolicyConditionDAO.class);
            if(policyCondition != null) {
                List<Integer> alertTypeIds = new ArrayList<>();
                try (Connection con = Postgres.getConnection();
                     PreparedStatement st = con.prepareStatement("SELECT * FROM policy_condition_alert WHERE policy_cond_id = ?")) {
                    st.setInt(1, policyCondition.getId());
                    try (ResultSet rs = st.executeQuery()) {
                        while (rs.next()) {
                            alertTypeIds.add(rs.getInt("alert_type_id"));
                        }
                    }
                }
                policyCondition.setAlertTypeIds(alertTypeIds);
            }

            return policyCondition;
        } catch (SQLException e) {
            logger.severe("Error finding PolicyCondition: "+e.getClass().getName() +": "+e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Inserts a row into the policy_condition table and a row for each alert_type_id in policy_condition_alert
     * @param policyCondition
     * @return
     */
    public static Integer insertPolicyCondition(PolicyCondition policyCondition) {
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("INSERT INTO policy_condition(threshold) VALUES(?) RETURNING id")) {
            st.setInt(1, policyCondition.getThreshold());
            st.execute();
            policyCondition.setId(getLatestId(st));

            if(policyCondition.getAlertTypeIds() != null){
                for(int i=0; i<policyCondition.getAlertTypeIds().size(); i++) {
                    int id = policyCondition.getAlertTypeIds().get(i);
                    try(PreparedStatement insert2 = con.prepareStatement("INSERT INTO policy_condition_alert(policy_cond_id, alert_type_id) VALUES(?,?)")) {
                        insert2.setInt(1, policyCondition.getId());
                        insert2.setInt(2, id);
                        insert2.executeUpdate();
                    }
                }
            }

            return policyCondition.getId();
        } catch (SQLException e) {
            logger.severe("Error inserting PolicyCondition: "+e.getClass().getName() +": "+e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Updates a row in policy_condition and related rows in policy_condition_alert
     * @param policyCondition The policy condition to update
     * @return the condition's id on succes; -1 on failure
     */
    public static Integer updatePolicyCondition(PolicyCondition policyCondition) {
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("UPDATE policy_condition SET threshold = ? WHERE id = ?")) {
            // Update PolicyCondition table
            st.setInt(1, policyCondition.getThreshold());
            st.setInt(2, policyCondition.getId());
            st.executeUpdate();

            // Update PolicyConditionAlert table
            if(!deletePolicyConditionAlertRows(policyCondition.getId()))
                return -1;

            for(Integer alertId: policyCondition.getAlertTypeIds()) {
                try(PreparedStatement insert = con.prepareStatement("INSERT INTO policy_condition_alert(policy_cond_id, alert_type_id) VALUES(?,?)")) {
                    insert.setInt(1, policyCondition.getId());
                    insert.setInt(2, alertId);
                    insert.executeUpdate();
                }
            }

            return policyCondition.getId();
        } catch (SQLException e) {
            logger.severe("Error updating PolicyCondition: "+e.getClass().getName() +": "+e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Helper function to remove all rows from policy_condition_alert for the given PolicyCondition id
     * @param policyConditionId
     * @return
     */
    private static boolean deletePolicyConditionAlertRows(int policyConditionId){
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("DELETE FROM policy_condition_alert WHERE policy_cond_id = ?")) {
            st.setInt(1, policyConditionId);
            st.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.severe("Error deleting PolicyConditionAlert rows: "+e.getClass().getName() +": "+e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Deletes a row in the policy_condition table with the given id
     * @param id
     * @return
     */
    public static Boolean deletePolicyCondition(int id) {
        return deleteById("policy_condition", id);
    }
    
}
