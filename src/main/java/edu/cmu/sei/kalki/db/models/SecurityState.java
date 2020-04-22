package edu.cmu.sei.kalki.db.models;

import edu.cmu.sei.kalki.db.daos.SecurityStateDAO;

public class SecurityState extends Model  {
    private String name;

    public SecurityState() {
    }

    public SecurityState(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public SecurityState(String name) {
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int insert() {
        this.id = SecurityStateDAO.insertSecurityState(this);
        return this.id;
    }

    public int insertOrUpdate() {
        this.id = SecurityStateDAO.insertOrUpdateSecurityState(this);
        return this.id;
    }
}
