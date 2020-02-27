package edu.cmu.sei.kalki.db.daos;

import edu.cmu.sei.kalki.db.database.Postgres;
import edu.cmu.sei.kalki.db.models.StateTransition;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
    
    public static StateTransition findStateTransition(int id) {
        ResultSet rs = findById(id, "state_transition");
        StateTransition stateTransition = null;
        try {
            stateTransition = createFromRs(rs);
        } catch (SQLException e) {
            logger.severe("Sql exception creating object");
            e.printStackTrace();
        }
        closeResources(rs);
        return stateTransition;
    }

    /**
     * Inserts the given StateTransition obj to the state_transition table
     * @param trans The obj to insert
     * @return Row's id on success. -1 otherwise
     */
    public static Integer insertStateTransition(StateTransition trans) {
        try(PreparedStatement insert = Postgres.prepareStatement("INSERT INTO state_transition(start_sec_state_id, finish_sec_state_id) VALUES(?,?)")) {
            insert.setInt(1, trans.getStartStateId());
            insert.setInt(2, trans.getFinishStateId());
            insert.executeUpdate();
            return getLatestId("state_transition");
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
        try(PreparedStatement update = Postgres.prepareStatement("UPDATE state_transition SET " +
                "start_sec_state_id = ? " +
                ", finish_sec_state_id = ? " +
                "WHERE id = ?")) {
            update.setInt(1, trans.getStartStateId());
            update.setInt(2, trans.getFinishStateId());
            update.setInt(3, trans.getId());
            update.executeUpdate();
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
