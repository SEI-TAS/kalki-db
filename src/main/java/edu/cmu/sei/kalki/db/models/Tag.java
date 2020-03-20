package edu.cmu.sei.kalki.db.models;

import edu.cmu.sei.kalki.db.daos.TagDAO;

public class Tag extends Model  {
    private String name;

    public Tag() {

    }

    public Tag(String name) {
        this.name = name;
    }

    public Tag(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int insert() {
        this.id = TagDAO.insertTag(this);
        return this.id;
    }

    public int insertOrUpdate() {
        this.id = TagDAO.insertOrUpdateTag(this);
        return this.id;
    }
}
