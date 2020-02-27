package edu.cmu.sei.kalki.db.daos;

import edu.cmu.sei.kalki.db.database.Postgres;
import edu.cmu.sei.kalki.db.models.Tag;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TagDAO extends DAO
{
    /**
     * Search the tag table for a row with the given id
     *
     * @param id The id of the tag
     * @return the row from the table
     */
    public static Tag findTag(int id) {
        ResultSet rs = findById(id, "tag");
        Tag tag = (Tag) createFromRs(Tag.class, rs);
        closeResources(rs);
        return tag;
    }

    /**
     * Find the respective tags for given device id
     *
     * @param deviceId The device id the tags are for
     * @return A list of tags or null
     */
    public static List<Tag> findTagsByDevice(int deviceId) {
        try(PreparedStatement st = Postgres.prepareStatement("SELECT tag.* FROM tag, device_tag " +
                "WHERE tag.id = device_tag.tag_id AND device_tag.device_id = ?")) {
            List<Tag> tags = new ArrayList<>();
            st.setInt(1, deviceId);
            try(ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    tags.add((Tag) createFromRs(Tag.class, rs));
                }
            }
            return tags;
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error finding tags by device_id: " + deviceId + ": " + e.getMessage());

        }
        return null;
    }

    /**
     * Find the respective tag ids for given device id
     *
     * @param deviceId The device id the tags are for
     * @return A list of tag ids or null
     */
    public static List<Integer> findTagIds(int deviceId) {
        try(PreparedStatement st = Postgres.prepareStatement("SELECT * FROM device_tag WHERE device_id = ?")) {
            st.setInt(1, deviceId);
            List<Integer> tagIds = new ArrayList<>();
            try(ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    tagIds.add(rs.getInt(2));
                }
            }
            return tagIds;
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error finding tags by device_id: " + deviceId + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Finds all Tags in the database.
     *
     * @return a list of all Tags in the database.
     */
    public static List<Tag> findAllTags() {
        return (List<Tag>) findAll("tag", Tag.class);
    }

    /**
     * Saves given Tag to the database.
     *
     * @param tag Tag to be inserted.
     * @return auto incremented id
     */
    public static Integer insertTag(Tag tag) {
        logger.info("Inserting Tag: " + tag.getId());
        try(PreparedStatement update = Postgres.prepareStatement
                ("INSERT INTO tag(name)" +
                        "values(?)")) {
            update.setString(1, tag.getName());
            update.executeUpdate();
            return getLatestId("tag");
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error inserting Tag: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return -1;
    }

    /**
     * Updates Tag with given id to have the parameters of the given Tag.
     *
     * @param tag Tag holding new parameters to be saved in the database.
     */
    public static Integer updateTag(Tag tag) {
        logger.info("Updating Tag with id=" + tag.getId());
        try(PreparedStatement update = Postgres.prepareStatement
                ("UPDATE tag SET name = ?" +
                        "WHERE id=?")) {
            update.setString(1, tag.getName());
            update.setInt(2, tag.getId());
            update.executeUpdate();
            return tag.getId();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error updating Tag: " + e.getClass().getName() + ": " + e.getMessage());
            return -1;
        }
    }


    /**
     * First, attempts to find the Tag in the database.
     * If successful, updates the existing Tag with the given Tag's parameters Otherwise,
     * inserts the given Tag.
     *
     * @param tag Tag to be inserted or updated.
     */
    public static Integer insertOrUpdateTag(Tag tag) {
        Tag t = findTag(tag.getId());
        if (t == null) {
            return insertTag(tag);
        } else {
            return updateTag(tag);
        }
    }

    /**
     * Deletes a Tag by its id.
     *
     * @param id id of the Tag to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static Boolean deleteTag(int id) {
        logger.info(String.format("Deleting Tag with id = %d", id));
        return deleteById("tag", id);
    }    
}
