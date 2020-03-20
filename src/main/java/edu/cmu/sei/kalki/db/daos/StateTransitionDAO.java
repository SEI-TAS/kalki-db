package edu.cmu.sei.kalki.db.daos;

import edu.cmu.sei.kalki.db.database.Postgres;
import edu.cmu.sei.kalki.db.models.StateTransition;

import javax.swing.plaf.nimbus.State;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class StateTransitionDAO extends DAO
{
    /**
     * Converts a ResultSet obj to a StateTransition
     */
    public static StateTransition createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) return null;
        int id = rs.getInt("id");
        int startSecStateId = rs.getInt("start_sec_state_id");
        int finishSecStateId = rs.getInt("finish_sec_state_id");
        return new StateTransition(id, startSecStateId, finishSecStateId);
    }

    /**
     * Finds a specific state transition.
     * @param id
     * @return
     */
    public static StateTransition findStateTransition(int id) {
        return (StateTransition) findObjectByIdAndTable(id, "state_transition", StateTransitionDAO.class);
    }

    /**
     * Find all state transitions.
     * @return
     */
    public static List<StateTransition> findAll() {
        return (List<StateTransition>) findObjectsByTable("state_transition", StateTransitionDAO.class);
    }

    /**
     * Inserts the given StateTransition obj to the state_transition table
     * @param trans The obj to insert
     * @return Row's id on success. -1 otherwise
     */
    public static Integer insertStateTransition(StateTransition trans) {
        try(Connection con = Postgres.getConnection();
        PreparedStatement st = con.prepareStatement("INSERT INTO state_transition(start_sec_state_id, finish_sec_state_id) VALUES(?,?) RETURNING id")) {
            st.setInt(1, trans.getStartStateId());
            st.setInt(2, trans.getFinishStateId());
            st.execute();
            return getLatestId(st);
        } catch (SQLException e) {
            logger.severe("Error inserting StateTransition: "+e.getClass().getName() +": "+e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Updates the row in the StateTransition table with the given id
     * @param trans
     * @return The id of the given transition on success. -1 otherwise
     */
    public static Integer updateStateTransition(StateTransition trans) {
        try(Connection con = Postgres.getConnection();
            PreparedStatement st = con.prepareStatement("UPDATE state_transition SET " +
                "start_sec_state_id = ? " +
                ", finish_sec_state_id = ? " +
                "WHERE id = ?")) {
            st.setInt(1, trans.getStartStateId());
            st.setInt(2, trans.getFinishStateId());
            st.setInt(3, trans.getId());
            st.executeUpdate();
            return trans.getId();
        } catch (SQLException e) {
            logger.severe("Error updating StateTransition: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /***
     * Delete a row in the state_transition table with the given id
     * @param id
     * @return True on success, false otherwise
     */
    public static boolean deleteStateTransition(int id) {
        return deleteById("state_transition", id);
    }
}
