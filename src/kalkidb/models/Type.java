package kalkidb.models;
import kalkidb.database.Postgres;

public class Type {

    private int id;
    private String name;
    private byte[] policyFile;
    private String policyFileName;

    public Type() {

    }

    public Type(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Type(String name, byte[] policyFile, String policyFileName) {
        this.name = name;
        this.policyFile = policyFile;
        this.policyFileName = policyFileName;
    }

    public Type(int id, String name, byte[] policyFile, String policyFileName) {
        this.id = id;
        this.name = name;
        this.policyFile = policyFile;
        this.policyFileName = policyFileName;
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

    public byte[] getPolicyFile() {
        return policyFile;
    }

    public void setPolicyFile(byte[] policyFile) {
        this.policyFile = policyFile;
    }

    public String getPolicyFileName() {
        return policyFileName;
    }

    public void setPolicyFileName(String policyFileName) {
        this.policyFileName = policyFileName;
    }

    public void insert(){
        Postgres.insertType(this).thenApplyAsync(id -> {
            this.id = id;
            return id;
        });
    }
}