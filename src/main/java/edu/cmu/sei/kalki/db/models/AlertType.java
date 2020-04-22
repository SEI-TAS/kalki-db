package edu.cmu.sei.kalki.db.models;

import edu.cmu.sei.kalki.db.daos.AlertTypeDAO;

public class AlertType extends Model  {
    private String name;
    private String description;
    private String source;

    public AlertType() {
    }

    public AlertType(String name, String description, String source) {
        this.name = name;
        this.description = description;
        this.source = source;
    }

    public AlertType(int id, String name, String description, String source) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.source = source;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int insert() {
        this.id = AlertTypeDAO.insertAlertType(this);
        return this.id;
    }

    public int insertOrUpdate() {
        this.id = AlertTypeDAO.insertOrUpdateAlertType(this);
        return this.id;
    }
}
