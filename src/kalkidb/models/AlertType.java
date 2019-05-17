package kalkidb.models;
import kalkidb.database.Postgres;

public class AlertType {
    private int id;
    private String name;
    private String description;
    private String source;

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

    public void insert(){
        Postgres.insertAlertType(this).thenApplyAsync(id->{
            this.id = id;
            return id;
        });
    }
}