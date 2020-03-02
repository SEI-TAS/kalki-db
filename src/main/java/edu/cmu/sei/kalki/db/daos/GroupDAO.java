package edu.cmu.sei.kalki.db.daos;

import edu.cmu.sei.kalki.db.database.Postgres;
import edu.cmu.sei.kalki.db.models.Group;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class GroupDAO extends DAO
{
    /**
     * Extract a Group from the result set of a database query.
     */
    public static Group createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) return null;
        int id = rs.getInt("id");
        String name = rs.getString("name");
        return new Group(id, name);
    }

    /**
     * Finds a Group from the database by its id.
     *
     * @param id id of the Group to find.
     * @return the Group if it exists in the database, else null.
     */
    public static Group findGroup(int id) {
        return (Group) findObjectByIdAndTable(id, "device_group", GroupDAO.class);
    }

    /**
     * Finds all Groups in the database.
     *
     * @return a list of all Groups in the database.
     */
    public static List<Group> findAllGroups() {
        return (List<Group>) findObjectsByTable("device_group", GroupDAO.class);
    }

    /**
     * Saves given Device Group to the database.
     *
     * @param group Device Group to be inserted.
     * @return auto incremented id
     */
    public static Integer insertGroup(Group group) {
        logger.info("Inserting group: " + group.getName());
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement
                ("INSERT INTO device_group(name)" +
                        "values(?) RETURNING id")) {
            st.setString(1, group.getName());
            st.execute();
            return getLatestId(st);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error inserting Group: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return -1;
    }

    /**
     * Updates Group with given id to have the parameters of the given Group.
     *
     * @param group group holding new parameters to be saved in the database.
     */
    public static Integer updateGroup(Group group) {
        logger.info("Updating Group with id=" + group.getId());
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement
                ("UPDATE device_group SET name = ?" +
                        "WHERE id=?")) {
            st.setString(1, group.getName());
            st.setInt(2, group.getId());
            st.executeUpdate();
            return group.getId();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error updating Group: " + e.getClass().getName() + ": " + e.getMessage());
            return -1;
        }
    }

    /**
     * First, attempts to find the Group in the database.
     * If successful, updates the existing Group with the given Groups's parameters. Otherwise,
     * inserts the given Group.
     *
     * @param group Group to be inserted or updated.
     */
    public static Integer insertOrUpdateGroup(Group group) {
        Group g = findGroup(group.getId());
        if (g == null) {
            return insertGroup(group);
        } else {
            return updateGroup(group);
        }
    }

    /**
     * Deletes a Group by its id.
     *
     * @param id id of the Group to delete.
     * @return true if the deletion succeeded, false otherwise.
     */
    public static Boolean deleteGroup(int id) {
        return deleteById("device_group", id);
    }
    
}
