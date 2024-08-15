package com.example.myapplication;

public class FingerPrintPost {
    private String point;
    private int mac1_rssi;
    private int mac2_rssi;
    private int mac3_rssi;

    public FingerPrintPost(String point, int mac1_rssi, int mac2_rssi, int mac3_rssi){
        this.point = point;
        this.mac1_rssi = mac1_rssi;
        this.mac2_rssi = mac2_rssi;
        this.mac3_rssi = mac3_rssi;
    }

    public int getMac2_rssi() {
        return mac2_rssi;
    }

    public void setMac2_rssi(int mac2_rssi) {
        this.mac2_rssi = mac2_rssi;
    }

    public int getMac3_rssi() {
        return mac3_rssi;
    }

    public void setMac3_rssi(int mac3_rssi) {
        this.mac3_rssi = mac3_rssi;
    }

    public int getMac1_rssi() {
        return mac1_rssi;
    }

    public void setMac1_rssi(int mac1_rssi) {
        this.mac1_rssi = mac1_rssi;
    }

    public String getPoint() {
        return point;
    }

    public void setPoint(String point) {
        this.point = point;
    }
}
