package edu.cmu.sei.ttg.kalki.models;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;
import edu.cmu.sei.ttg.kalki.database.Postgres;

public class UmboxImage {

    private int id;
    private String name;
    private String path;
    private Integer dagOrder;
    private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    public UmboxImage() {

    }

    public UmboxImage(String name, String path){
        this.name = name;
        this.path = path;
    }

    public UmboxImage(int id, String name, String path){
        this.id = id;
        this.name = name;
        this.path = path;
        this.dagOrder = null;
    }

    public UmboxImage(int id, String name, String path, Integer dagOrder) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.dagOrder = dagOrder;
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Integer getDagOrder() {
        return dagOrder;
    }

    public void setDagOrder(Integer dagOrder) {
        this.dagOrder = dagOrder;
    }

    public String toString() {
        try {
            return ow.writeValueAsString(this);
        }
        catch (JsonProcessingException e) {
            return "Bad umbox image";
        }
    }

    public Integer insert() {
        this.id = Postgres.insertUmboxImage(this);
        return this.id;
    }

    public Integer insertOrUpdate() { return Postgres.insertOrUpdateUmboxImage(this); }
}
