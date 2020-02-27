package edu.cmu.sei.kalki.db.daos;

import edu.cmu.sei.kalki.db.database.Postgres;
import edu.cmu.sei.kalki.db.models.PolicyCondition;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PolicyConditionDAO extends DAO
{
    /**
     * Find a PolicyCondition and it's associated AlertType id's
     * @param id
     * @return A PolicyCondition obj. Null otherwise
     */
    public static PolicyCondition findPolicyCondition(int id) {
        try {
            ResultSet pcrs = findById(id,"policy_condition");
            PolicyCondition policyCondition = (PolicyCondition) createFromRs(PolicyCondition.class, pcrs);
            closeResources(pcrs);
            if(policyCondition == null) { return null; }

            List<Integer> alertTypeIds = new ArrayList<>();
            try(PreparedStatement query = Postgres.prepareStatement("SELECT * FROM policy_condition_alert WHERE policy_cond_id = ?")) {
                query.setInt(1, policyCondition.getId());
                try(ResultSet rs = query.executeQuery()) {
                    while (rs.next()) {
                        alertTypeIds.add(rs.getInt("alert_type_id"));
                    }
                }
            }
            policyCondition.setAlertTypeIds(alertTypeIds);

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
        try(PreparedStatement insert = Postgres.prepareStatement("INSERT INTO policy_condition(threshold) VALUES(?)")) {
            insert.setInt(1, policyCondition.getThreshold());
            insert.executeUpdate();
            policyCondition.setId(getLatestId("policy_condition"));

            if(policyCondition.getAlertTypeIds() != null){
                for(int i=0; i<policyCondition.getAlertTypeIds().size(); i++) {
                    int id = policyCondition.getAlertTypeIds().get(i);
                    try(PreparedStatement insert2 = Postgres.prepareStatement("INSERT INTO policy_condition_alert(policy_cond_id, alert_type_id) VALUES(?,?)")) {
                        insert2.setInt(1, policyCondition.getId());
                        insert2.setInt(2, id);
                        insert2.executeQuery();
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
        try(PreparedStatement update = Postgres.prepareStatement("UPDATE policy_condition SET threshold = ? WHERE id = ?")) {
            // Update PolicyCondition table
            update.setInt(1, policyCondition.getThreshold());
            update.setInt(2, policyCondition.getId());
            update.executeUpdate();

            // Update PolicyConditionAlert table
            if(!deletePolicyConditionAlertRows(policyCondition.getId()))
                return -1;

            for(Integer alertId: policyCondition.getAlertTypeIds()) {
                try(PreparedStatement insert = Postgres.prepareStatement("INSERT INTO policy_condition_alert(policy_cond_id, alert_type_id) VALUES(?,?)")) {
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
     * Helper function to remove all rows from policy_condition_alert for the givein PolicyCondition id
     * @param policyConditionId
     * @return
     */
    private static boolean deletePolicyConditionAlertRows(int policyConditionId){
        try(PreparedStatement delete = Postgres.prepareStatement("DELETE FROM policy_condition_alert WHERE policy_cond_id = ?")) {
            delete.setInt(1, policyConditionId);
            delete.executeUpdate();
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
