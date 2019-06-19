package edu.cmu.sei.ttg.kalki.models;
import edu.cmu.sei.ttg.kalki.database.Postgres;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;

public class Tag {

    private int id;
    private String name;

    private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    public Tag() {

    }

    public Tag(String name){
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

    public Integer insert(){
        this.id = Postgres.insertTag(this);
        return this.id;
    }

    public Integer insertOrUpdate() { return Postgres.insertOrUpdateTag(this); }

    public String toString() {
        try {
            return ow.writeValueAsString(this);
        }
        catch (JsonProcessingException e) {
            return "Bad Tag";
        }
    }
}
