package edu.cmu.sei.ttg.kalki.models;

import edu.cmu.sei.ttg.kalki.database.Postgres;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;

public class Group {

    private int id;
    private String name;

    private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

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
        this.id = Postgres.insertGroup(this);
        return this.id;
    }

    public Integer insertOrUpdate() {
        this.id = Postgres.insertOrUpdateGroup(this);
        return this.id;
    }

    public String toString() {
        try {
            return ow.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "Bad Group";
        }
    }
}
