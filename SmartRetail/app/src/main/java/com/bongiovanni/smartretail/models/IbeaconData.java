package com.bongiovanni.smartretail.models;

public class IbeaconData {
    private String uuid, major, minor;

    public IbeaconData(String uuid, String major, String minor) {
        this.uuid = uuid;
        this.major = major;
        this.minor = minor;
    }

    public String getUuid() {
        return uuid;
    }

    public String getMajor() {
        return major;
    }

    public String getMinor() {
        return minor;
    }
}
