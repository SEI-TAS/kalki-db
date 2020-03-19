package edu.cmu.sei.kalki.db.models;

import edu.cmu.sei.kalki.db.daos.TagDAO;

public class Tag extends Model  {

    private int id;
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
        this.id = TagDAO.insertTag(this);
        return this.id;
    }

    public Integer insertOrUpdate() {
        this.id = TagDAO.insertOrUpdateTag(this);
        return this.id;
    }
}
