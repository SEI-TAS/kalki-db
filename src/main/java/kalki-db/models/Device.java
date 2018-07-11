package kalkidb.models;

import kalkidb.database.Postgres;


public class Device {

    public String id;
    public String name;
    public String type;
    public String groupId;
    public String ip;
    public int historySize;
    public int samplingRate;
    public String policyFile;
    public String description;

    public Device(String id, String description, String name, String type, String groupId, String ip,
                  int historySize, int samplingRate, String policyFile){
        this.id = id;
        this.description = description;
        this.name = name;
        this.type = type;
        this.groupId = groupId;
        this.historySize = historySize;
        this.samplingRate = samplingRate;
        this.ip = ip;
        this.policyFile = policyFile;
    }

    public void insert(){
        Postgres.insertDevice(this);
    }

}
