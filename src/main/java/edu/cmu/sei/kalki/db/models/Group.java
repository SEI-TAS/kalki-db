package edu.cmu.sei.kalki.db.models;

import edu.cmu.sei.kalki.db.daos.GroupDAO;

public class Group  extends Model {

    private int id;
    private String name;

    public Group() {

    }

    public Group(String name) {
        this.name = name;
    }

    public Group(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer insert() {
        this.id = GroupDAO.insertGroup(this);
        return this.id;
    }

    public Integer insertOrUpdate() {
        this.id = GroupDAO.insertOrUpdateGroup(this);
        return this.id;
    }
}
