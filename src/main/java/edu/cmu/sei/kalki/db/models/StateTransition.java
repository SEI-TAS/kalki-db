package edu.cmu.sei.kalki.db.models;

import edu.cmu.sei.kalki.db.daos.StateTransitionDAO;

public class StateTransition extends Model  {
    private int id;
    private int startStateId;
    private int finishStateId;

    public StateTransition() { }

    public StateTransition(int startStateId, int finishStateId) {
        this.startStateId = startStateId;
        this.finishStateId = finishStateId;
    }

    public StateTransition(int id, int startStateId, int finishStateId) {
        this(startStateId, finishStateId);
        this.id = id;
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
        int id = StateTransitionDAO.insertStateTransition(this);
        if(id>0)
            this.id = id;
    }

    public void update() {
        StateTransitionDAO.updateStateTransition(this);
    }
}
