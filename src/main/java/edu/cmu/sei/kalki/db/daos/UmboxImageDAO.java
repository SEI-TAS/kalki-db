package edu.cmu.sei.kalki.db.daos;

import edu.cmu.sei.kalki.db.database.Postgres;
import edu.cmu.sei.kalki.db.models.UmboxImage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UmboxImageDAO extends DAO
{
    /**
     * Find a UmboxImage based on its id
     *
     * @param id ID of the desired UmboxImage
     * @return The desired UmboxImage on success or null on failure
     */
    public static UmboxImage findUmboxImage(int id) {
        ResultSet rs = findById(id, "umbox_image");
        UmboxImage umboxImage = (UmboxImage) createFromRs(UmboxImage.class, rs);
        closeResources(rs);
        return umboxImage;
    }

    /**
     * Finds the UmboxImages relating to the device type and the security state
     *
     * @param devTypeId id of the device type
     * @param secStateId id of the security state
     * @return A list of UmboxImages for the given device type id and state id
     */
    public static List<UmboxImage> findUmboxImagesByDeviceTypeAndSecState(int devTypeId, int secStateId) {
        try(PreparedStatement st = Postgres.prepareStatement("SELECT ui.id, ui.name, ui.file_name, ul.dag_order " +
                "FROM umbox_image ui, umbox_lookup ul, policy_rule pl, state_transition st " +
                "WHERE pl.device_type_id = ? AND st.finish_sec_state_id = ? " +
                "AND ul.umbox_image_id = ui.id AND ul.policy_rule_id = pl.id AND pl.state_trans_id = st.id")) {
            st.setInt(1, devTypeId);
            st.setInt(2, secStateId);
            List<UmboxImage> umboxImageList = new ArrayList<>();
            try(ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    umboxImageList.add((UmboxImage) createFromRs(UmboxImage.class, rs));
                }
            }
            return umboxImageList;
        } catch (SQLException e) {
            logger.severe("Sql exception getting all UmboxImages: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Finds all UmboxImages in the database.
     *
     * @return a list of all UmboxImages in the database.
     */
    public static List<UmboxImage> findAllUmboxImages() {
        return (List<UmboxImage>) findAll("umbox_image", UmboxImage.class);
    }

    /**
     * Inserts given UmboxImage into the database
     *
     * @param u the UmboxImage to be inserted
     * @return The id of the inserted UmboxImage on success or -1 on failure
     */
    public static Integer insertUmboxImage(UmboxImage u) {
        logger.info("Adding umbox image: " + u);
        try(PreparedStatement st = Postgres.prepareStatement("INSERT INTO umbox_image (name, file_name) VALUES (?, ?)")) {
            st.setString(1, u.getName());
            st.setString(2, u.getFileName());
            st.executeUpdate();
            return getLatestId("umbox_image");
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("SQL exception adding umbox iamge: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return -1;
    }

    /**
     * Updates given UmboxImage in the database
     *
     * @param u the UmboxImage to be updated
     * @return The ID of the updated UmboxImage or -1 on failure
     */
    public static Integer updateUmboxImage(UmboxImage u) {
        logger.info("Editing umbox image: " + u);
        try(PreparedStatement st = Postgres.prepareStatement("UPDATE umbox_image " +
                "SET name = ?, file_name = ? " +
                "WHERE id = ?")) {
            st.setString(1, u.getName());
            st.setString(2, u.getFileName());
            st.setInt(3, u.getId());
            st.executeUpdate();
            return u.getId();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("SQL exception editing umbox iamge: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return -1;
    }

    /**
     * First, attempts to find the UmboxImage in the database.
     * If successful, updates the existing UmboxImage with the given UmboxImage's parameters Otherwise,
     * inserts the given UmboxImage.
     *
     * @param image UmboxImage to be inserted or updated.
     */
    public static Integer insertOrUpdateUmboxImage(UmboxImage image) {
        UmboxImage ui = findUmboxImage(image.getId());
        if (ui == null) {
            return insertUmboxImage(image);
        } else {
            return updateUmboxImage(image);
        }
    }

    /**
     * Deletes a UmboxImage by its id.
     *
     * @param id id of the UmboxImage to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static Boolean deleteUmboxImage(int id) {
        return deleteById("umbox_image", id);
    }
    
}
