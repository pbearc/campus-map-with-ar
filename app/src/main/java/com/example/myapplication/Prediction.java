package com.example.myapplication;

public class Prediction {
    private Mac mac;
    private String mac_address;
    private int rssi;

    public Prediction(String mac_address2, int rssi2, Mac mac2) {
        this.rssi = rssi2;
        this.mac = mac2;
        this.mac_address = mac_address2;
    }

    public int getRssi() {
        return this.rssi;
    }

    public String getMac_address() {
        return this.mac_address;
    }

    public Mac getMac() {
        return this.mac;
    }

    public void setRssi(int rssi2) {
        this.rssi = rssi2;
    }

    public void setMac_address(String mac_address2) {
        this.mac_address = mac_address2;
    }

    public void setMac(Mac mac2) {
        this.mac = mac2;
    }
}
