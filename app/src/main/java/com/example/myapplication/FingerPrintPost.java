package com.example.myapplication;

public class FingerPrintPost {
    private int mac1_rssi;
    private int mac2_rssi;
    private int mac3_rssi;
    private String point;

    public FingerPrintPost(String point2, int mac1_rssi2, int mac2_rssi2, int mac3_rssi2) {
        this.point = point2;
        this.mac1_rssi = mac1_rssi2;
        this.mac2_rssi = mac2_rssi2;
        this.mac3_rssi = mac3_rssi2;
    }

    public int getMac2_rssi() {
        return this.mac2_rssi;
    }

    public void setMac2_rssi(int mac2_rssi2) {
        this.mac2_rssi = mac2_rssi2;
    }

    public int getMac3_rssi() {
        return this.mac3_rssi;
    }

    public void setMac3_rssi(int mac3_rssi2) {
        this.mac3_rssi = mac3_rssi2;
    }

    public int getMac1_rssi() {
        return this.mac1_rssi;
    }

    public void setMac1_rssi(int mac1_rssi2) {
        this.mac1_rssi = mac1_rssi2;
    }

    public String getPoint() {
        return this.point;
    }

    public void setPoint(String point2) {
        this.point = point2;
    }
}
