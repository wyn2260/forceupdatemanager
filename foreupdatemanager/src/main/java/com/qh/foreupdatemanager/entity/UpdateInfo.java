package com.qh.foreupdatemanager.entity;

import java.util.List;

public class UpdateInfo {
    String version ;
    String downloadUri;
    Boolean isUpdate;
    List<String> upInfo;

    @Override
    public String toString() {
        return "UpdateInfo{" +
                "version='" + version + '\'' +
                ", downloadUri='" + downloadUri + '\'' +
                ", isUpdate=" + isUpdate +
                ", upInfo=" + upInfo +
                '}';
    }

    public List<String> getUpInfo() {
        return upInfo;
    }

    public void setUpInfo(List<String> upInfo) {
        this.upInfo = upInfo;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDownloadUri() {
        return downloadUri;
    }

    public void setDownloadUri(String downloadUri) {
        this.downloadUri = downloadUri;
    }

    public Boolean getUpdate() {
        return isUpdate;
    }

    public void setUpdate(Boolean update) {
        isUpdate = update;
    }

}
