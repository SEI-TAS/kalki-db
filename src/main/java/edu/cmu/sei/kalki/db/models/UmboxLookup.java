package edu.cmu.sei.kalki.db.models;

import edu.cmu.sei.kalki.db.daos.UmboxLookupDAO;

public class UmboxLookup extends Model  {
    private int securityStateId;
    private int deviceTypeId;
    private int umboxImageId;
    private int dagOrder;

    public UmboxLookup() {
    }

    public UmboxLookup(int securityStateId, int deviceTypeId, int umboxImageId, int dagOrder) {
        this.securityStateId = securityStateId;
        this.deviceTypeId = deviceTypeId;
        this.umboxImageId = umboxImageId;
        this.dagOrder = dagOrder;
    }

    public UmboxLookup(int id, int securityStateId, int deviceTypeId, int umboxImageId, int dagOrder) {
        this(securityStateId, deviceTypeId, umboxImageId, dagOrder);
        this.id = id;
    }

    public Integer getSecurityStateId() {
        return securityStateId;
    }

    public void setSecurityStateId(Integer securityStateId) {
        this.securityStateId = securityStateId;
    }

    public Integer getUmboxImageId() {
        return umboxImageId;
    }

    public void setUmboxImageId(Integer umboxImageId) {
        this.umboxImageId = umboxImageId;
    }

    public Integer getDagOrder() {
        return dagOrder;
    }

    public void setDagOrder(Integer dagOrder) {
        this.dagOrder = dagOrder;
    }

    public int insert() {
        this.id = UmboxLookupDAO.insertUmboxLookup(this);
        return this.id;
    }

    public int insertOrUpdate() {
        this.id = UmboxLookupDAO.insertOrUpdateUmboxLookup(this);
        return this.id;
    }

    public int getDeviceTypeId() {
        return deviceTypeId;
    }

    public void setDeviceTypeId(int deviceTypeId) {
        this.deviceTypeId = deviceTypeId;
    }
}
