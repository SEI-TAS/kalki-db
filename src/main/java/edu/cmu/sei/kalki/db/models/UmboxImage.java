package edu.cmu.sei.kalki.db.models;

import edu.cmu.sei.kalki.db.daos.UmboxImageDAO;

public class UmboxImage extends Model  {
    private String name;
    private String fileName;
    private Integer dagOrder;

    public UmboxImage() {
    }

    public UmboxImage(String name, String fileName){
        this.name = name;
        this.fileName = fileName;
    }

    public UmboxImage(int id, String name, String fileName){
        this.id = id;
        this.name = name;
        this.fileName = fileName;
        this.dagOrder = null;
    }

    public UmboxImage(int id, String name, String fileName, Integer dagOrder) {
        this.id = id;
        this.name = name;
        this.fileName = fileName;
        this.dagOrder = dagOrder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getDagOrder() {
        return dagOrder;
    }

    public void setDagOrder(Integer dagOrder) {
        this.dagOrder = dagOrder;
    }

    public int insert() {
        this.id = UmboxImageDAO.insertUmboxImage(this);
        return this.id;
    }

    public int insertOrUpdate() {
        this.id = UmboxImageDAO.insertOrUpdateUmboxImage(this);
        return this.id;
    }
}
