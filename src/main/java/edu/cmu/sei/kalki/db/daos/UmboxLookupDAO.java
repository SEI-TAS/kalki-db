package edu.cmu.sei.kalki.db.daos;

import edu.cmu.sei.kalki.db.database.Postgres;
import edu.cmu.sei.kalki.db.models.UmboxLookup;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UmboxLookupDAO extends DAO
{
    /**
     * Extract a UmboxLookup from the result set of a database query.
     */
    public static UmboxLookup createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) return null;
        int id = rs.getInt("id");
        int policyRuleId = rs.getInt("policy_rule_id");
        int umboxImageId = rs.getInt("umbox_image_id");
        int dagOrder = rs.getInt("dag_order");
        return new UmboxLookup(id, policyRuleId, umboxImageId, dagOrder);
    }

    /**
     * Finds a UmboxLookup from the database by its id.
     *
     * @param id id of the UmboxLookup to find.
     * @return the UmboxLookup if it exists in the database, else null.
     */
    public static UmboxLookup findUmboxLookup(int id) {
        logger.info("Finding umboxLookup with id = " + id);
        try(PreparedStatement st = Postgres.prepareStatement("SELECT * FROM umbox_lookup WHERE id = ?")) {
            st.setInt(1, id);
            try(ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return createFromRs(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Exception finding umbox lookup by ID: " + e.getClass().getName() + ": " + e.getMessage());
        }

        return null;
    }

    /**
     * Finds all umbox lookups based on the given device id
     */
    public static List<UmboxLookup> findUmboxLookupsByDevice(int deviceId) {
        List<UmboxLookup> lookupList = new ArrayList<>();
        try(PreparedStatement st = Postgres.prepareStatement("SELECT ul.* FROM umbox_lookup ul, device d, policy_rule p " +
                "WHERE ul.policy_rule_id = p.id AND p.device_type_id = d.type_id AND d.id = ?;")) {
            st.setInt(1, deviceId);
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
     * Finds all umboxLookup entries
     */
    public static List<UmboxLookup> findAllUmboxLookups() {
        return (List<UmboxLookup>) findAll("umbox_lookup", UmboxLookupDAO.class);
    }

    /**
     * Adds the desired UmboxLookup to the database
     */
    public static Integer insertUmboxLookup(UmboxLookup ul) {
        logger.info("Adding umbox lookup: ");
        try(PreparedStatement st = Postgres.prepareStatement("INSERT INTO umbox_lookup (policy_rule_id, umbox_image_id, dag_order) VALUES (?,?,?)")) {
            st.setInt(1, ul.getPolicyRuleId());
            st.setInt(2, ul.getUmboxImageId());
            st.setInt(3, ul.getDagOrder());
            st.executeUpdate();
            return getLatestId("umbox_lookup");
        }
        catch (SQLException e){
            e.printStackTrace();
            logger.severe("SQL exception adding umbox lookup: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return -1;
    }

    /**
     * Edit desired UmboxLookup
     */
    public static Integer updateUmboxLookup(UmboxLookup ul) {
        logger.info(String.format("Updating UmboxLookup with id = %d with values: %s", ul.getId(), ul));
        try(PreparedStatement update = Postgres.prepareStatement("UPDATE umbox_lookup " +
                "SET policy_" +
                "rule_id = ?, umbox_image_id = ?, dag_order = ?" +
                "WHERE id = ?")) {
            update.setInt(1, ul.getPolicyRuleId());
            update.setInt(2, ul.getUmboxImageId());
            update.setInt(3, ul.getDagOrder());
            update.setInt(4, ul.getId());
            update.executeUpdate();

            return ul.getId();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error updating UmboxLookup: " + e.getClass().toString() + ": " + e.getMessage());
        }
        return -1;
    }

    /**
     * First, attempts to find the UmboxLookup in the database.
     * If successful, updates the existing UmboxLookup with the given parameters Otherwise,
     * inserts the given UmboxLookup.
     *
     * @param ul UmboxLookup to be inserted or updated.
     */
    public static Integer insertOrUpdateUmboxLookup(UmboxLookup ul) {
        UmboxLookup foundUl = findUmboxLookup(ul.getId());

        if (foundUl == null) {
            return insertUmboxLookup(ul);
        } else {
            return updateUmboxLookup(ul);
        }
    }

    /**
     * Deletes a UmboxLookup by its id.
     */
    public static Boolean deleteUmboxLookup(int id) {
        return deleteById("umbox_lookup", id);
    }

    
}
