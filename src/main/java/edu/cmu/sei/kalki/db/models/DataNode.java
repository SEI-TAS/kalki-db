package edu.cmu.sei.kalki.db.models;

import edu.cmu.sei.kalki.db.daos.DataNodeDAO;

public class DataNode extends Model
{
    private int id;
    private String name;
    private String ipAddress;

    public DataNode() {
    }

    public DataNode(int id, String name, String ipAddress) {
        this.id = id;
        this.name = name;
        this.ipAddress = ipAddress;
    }

    public DataNode(String name, String ipAddress) {
        this.name = name;
        this.ipAddress = ipAddress;
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

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Integer insert() {
        this.id = DataNodeDAO.insertDataNode(this);
        return this.id;
    }

    public Integer insertOrUpdate() {
        this.id = DataNodeDAO.insertOrUpdateDataNode(this);
        return this.id;
    }
}
