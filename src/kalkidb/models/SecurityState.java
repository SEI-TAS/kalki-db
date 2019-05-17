package kalkidb.models;
import kalkidb.database.Postgres;

public class SecurityState{
    private int id;
    private String name;

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
}