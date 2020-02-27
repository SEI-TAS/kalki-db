package edu.cmu.sei.kalki.db.models;

import edu.cmu.sei.kalki.db.database.Postgres;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StateTransition {
    private int id;
    private int startStateId;
    private int finishStateId;

    private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    public StateTransition() { }

    public StateTransition(int startStateId, int finishStateId) {
        this.startStateId = startStateId;
        this.finishStateId = finishStateId;
    }

    public StateTransition(int id, int startStateId, int finishStateId) {
        this(startStateId, finishStateId);
        this.id = id;
    }

    /**
     * Converts a ResultSet obj to a Policy
     */
    public static StateTransition createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) return null;
        int id = rs.getInt("id");
        int startSecStateId = rs.getInt("start_sec_state_id");
        int finishSecStateId = rs.getInt("finish_sec_state_id");
        return new StateTransition(id, startSecStateId, finishSecStateId);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStartStateId() {
        return startStateId;
    }

    public void setStartStateId(int startStateId) {
        this.startStateId = startStateId;
    }

    public int getFinishStateId() {
        return finishStateId;
    }

    public void setFinishStateId(int finishStateId) {
        this.finishStateId = finishStateId;
    }

    public void insert(){
        int id = Postgres.insertStateTransition(this);
        if(id>0)
            this.id = id;
    }

    public String toString() {
        try {
            return ow.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "Bad policy";
        }
    }
}
