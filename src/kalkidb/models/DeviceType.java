package kalkidb.models;
import kalkidb.database.Postgres;
import java.util.concurrent.CompletionStage;

public class DeviceType {

    private int id;
    private String name;
    private byte[] policyFile;
    private String policyFileName;

    public DeviceType() {

    }

    public DeviceType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public DeviceType(String name, byte[] policyFile, String policyFileName) {
        this.name = name;
        this.policyFile = policyFile;
        this.policyFileName = policyFileName;
    }

    public DeviceType(int id, String name, byte[] policyFile, String policyFileName) {
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

    public CompletionStage<Integer> insert(){
        return Postgres.insertDeviceType(this).thenApplyAsync(id -> {
            this.id = id;
            return id;
        });
    }
}