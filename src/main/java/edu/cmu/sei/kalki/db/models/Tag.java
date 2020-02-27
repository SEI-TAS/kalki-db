package edu.cmu.sei.kalki.db.models;

import edu.cmu.sei.kalki.db.database.Postgres;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Tag {

    private int id;
    private String name;

    private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    public Tag() {

    }

    public Tag(String name) {
        this.name = name;
    }

    public Tag(int id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Extract a Tag from the result set of a database query.
     */
    public static Tag createFromRs(ResultSet rs) throws SQLException {
        if(rs == null) return null;
        int id = rs.getInt("id");
        String name = rs.getString("name");
        return new Tag(id, name);
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
        this.id = Postgres.insertTag(this);
        return this.id;
    }

    public Integer insertOrUpdate() {
        this.id = Postgres.insertOrUpdateTag(this);
        return this.id;
    }

    public String toString() {
        try {
            return ow.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "Bad Tag";
        }
    }
}
