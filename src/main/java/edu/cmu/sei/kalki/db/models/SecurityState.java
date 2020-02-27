package edu.cmu.sei.kalki.db.models;

import edu.cmu.sei.kalki.db.daos.SecurityStateDAO;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SecurityState {
    private int id;
    private String name;

    private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    public SecurityState() {
    }

    public SecurityState(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public SecurityState(String name) {
        this.name = name;
    }

    /**
     * Take a ResultSet from a DB query and convert to the java object
     */
    public static SecurityState createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) return null;
        int id = rs.getInt("id");
        String name = rs.getString("name");
        return new SecurityState(id, name);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public Integer insert() {
        this.id = SecurityStateDAO.insertSecurityState(this);
        return this.id;
    }

    public Integer insertOrUpdate() {
        this.id = SecurityStateDAO.insertOrUpdateSecurityState(this);
        return this.id;
    }

    public String toString() {
        try {
            return ow.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "Bad SecurityState";
        }
    }
}
