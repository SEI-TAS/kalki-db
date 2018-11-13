package kalkidb.models;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;
import kalkidb.database.Postgres;

public class UmboxImage {

    private int id;
    private String name;
    private String path;
    private final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    public UmboxImage() {

    }

    public UmboxImage(String name, String path){
        this.name = name;
        this.path = path;
    }

    public UmboxImage(int id, String name, String path) {
        this.id = id;
        this.name = name;
        this.path = path;
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

    public String toString() {
//        return String.format("{ id: \"%d\", name: \"%s\", path: \"%s\" }", id, name, path);
        try {
            return ow.writeValueAsString(this);
        }
        catch (JsonProcessingException e) {
            return "Bad umbox image";
        }
    }

    public void insert() {
        Postgres.insertUmboxImage(this).thenApplyAsync(id -> {
            this.id = id;
            return id;
        });
    }
}