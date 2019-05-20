package kalkidb.models;
import kalkidb.database.Postgres;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;

public class SecurityState{
    private int id;
    private String name;

    private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

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

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void insert(){
        this.id = Postgres.insertSecurityState(this);
    }

    public String toString() {
        try {
            return ow.writeValueAsString(this);
        }
        catch (JsonProcessingException e) {
            return "Bad SecurityState";
        }
    }
}