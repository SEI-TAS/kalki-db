package edu.cmu.sei.kalki.db.models;

import edu.cmu.sei.kalki.db.daos.AlertTypeDAO;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AlertType {
    private int id;
    private String name;
    private String description;
    private String source;

    private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

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

    /**
     * Extract an AlertType from the result set of a database query.
     * @param rs ResultSet from a AlertType query.
     * @return The AlertType that was found.
     */
    public static AlertType createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) return null;
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        String source = rs.getString("source");
        return new AlertType(id, name, description, source);
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

    public Integer insert() {
        this.id = AlertTypeDAO.insertAlertType(this);
        return this.id;
    }

    public Integer insertOrUpdate() {
        this.id = AlertTypeDAO.insertOrUpdateAlertType(this);
        return this.id;
    }

    public String toString() {
        try {
            return ow.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "Bad AlertType";
        }
    }
}
