package edu.cmu.sei.kalki.db.daos;

import edu.cmu.sei.kalki.db.database.Postgres;
import edu.cmu.sei.kalki.db.models.SecurityState;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class SecurityStateDAO extends DAO
{
    /**
     * Take a ResultSet from a DB query and convert to the java object
     */
    public static SecurityState createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) return null;
        int id = rs.getInt("id");
        String name = rs.getString("name");
        return new SecurityState(id, name);
    }
    
    /**
     * Search the security_state table for a row with the given id
     *
     * @param id The id of the security state
     * @return the row from the table
     */
    public static SecurityState findSecurityState(int id) {
        return (SecurityState) findObjectByIdAndTable(id, "security_state", SecurityStateDAO.class);
    }

    /**
     * Finds by state name.
     * @param name
     * @return
     */
    public static SecurityState findByName(String name) {
        String query = "SELECT * FROM security_state WHERE name = ?";
        return (SecurityState) findObjectByStringAndQuery(name, query, SecurityStateDAO.class);
    }

    /**
     * Finds all SecurityStates in the database.
     *
     * @return a list of all SecurityStates in the database.
     */
    public static List<SecurityState> findAllSecurityStates() {
        return (List<SecurityState>) findObjectsByTable("security_state", SecurityStateDAO.class);
    }

    /**
     * Inserts the given SecurityState into the db
     *
     * @param the security state to enter
     * @return the id of the newly inserted SecurityState
     */
    public static Integer insertSecurityState(SecurityState state) {
        logger.info("Inserting SecurityState");
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement
                ("INSERT INTO security_state(name)" +
                        "values(?) RETURNING id")) {
            st.setString(1, state.getName());
            st.execute();
            return getLatestId(st);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error inserting SecurityState: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return -1;
    }

    /**
     * Update row in security_state corresponding to the parameter
     *
     * @param state The security state to update
     * @return The id of the updated row
     */
    public static Integer updateSecurityState(SecurityState state) {
        logger.info("Updating SecurityState with id=" + state.getId());
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement
                ("UPDATE security_state SET name = ?" +
                        "WHERE id=?")) {
            st.setString(1, state.getName());
            st.setInt(2, state.getId());
            st.executeUpdate();
            return state.getId();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Error updating Security: " + e.getClass().getName() + ": " + e.getMessage());
            return -1;
        }
    }

    /**
     * First, attempts to find the SecurityState in the database.
     * If successful, updates the existing SecurityState with the given SecurityState's parameters Otherwise,
     * inserts the given SecurityState.
     *
     * @param state SecurityState to be inserted or updated.
     */
    public static Integer insertOrUpdateSecurityState(SecurityState state) {
        SecurityState ss = findSecurityState(state.getId());
        if (ss == null) {
            return insertSecurityState(state);
        } else {
            return updateSecurityState(state);
        }
    }

    /**
     * Delete row from security_state with the given id
     *
     * @param id The id of the row to delete
     * @return True if successful
     */
    public static Boolean deleteSecurityState(int id) {
        return deleteById("security_state", id);
    }    
}
