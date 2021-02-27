package com.bongiovanni.smartretail.models;

import org.altbeacon.beacon.Beacon;

import java.util.Date;

public class MyBeacon {

    private String beaconType = null, beaconAddress, beaconName;
    private int hashCode, manufacturer, txPower, rssi;
    private double distance;
    private long lastSeen;

    private com.bongiovanni.smartretail.models.IbeaconData ibeaconData;

    public MyBeacon(Beacon beacon) {
        this.hashCode = beacon.hashCode();
        this.beaconAddress = beacon.getBluetoothAddress();
        this.beaconName = beacon.getBluetoothName();
        this.manufacturer = beacon.getManufacturer();
        update(beacon);

        // This is an iBeacon
        this.beaconType = "iBeacon";
        this.ibeaconData = new IbeaconData(beacon.getId1().toString(), beacon.getId2().toString(), beacon.getId3().toString());

    }

    public void update(Beacon beacon){
        this.txPower = beacon.getTxPower();
        this.rssi = beacon.getRssi();
        this.distance = beacon.getDistance();
        this.lastSeen = new Date().getTime();
    }

    public int hashCode() {
        return hashCode;
    }

    public String getHashCode() {
        return String.valueOf(hashCode);
    }

    public String getBeaconType() {
        return beaconType;
    }

    public String getBeaconAddress() {
        return beaconAddress;
    }

    public int getManufacturer() {
        return manufacturer;
    }

    public int getTxPower() {
        return txPower;
    }

    public int getRssi() {
        return rssi;
    }

    public String getDistance() {
        return String.format("%.2f", distance);
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public String getBeaconName() {
        return beaconName;
    }

    public IbeaconData getIbeaconData() {
        return ibeaconData;
    }

}
