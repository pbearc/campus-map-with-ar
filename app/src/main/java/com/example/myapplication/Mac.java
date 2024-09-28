package com.example.myapplication;

public class Mac {
    private String address;
    private Double latitude;
    private Double longitude;
    private int z;

    public Mac(String address2, Double latitude2, Double longitude2, int z2) {
        this.address = address2;
        this.latitude = latitude2;
        this.longitude = longitude2;
        this.z = z2;
    }

    public String getAddress() {
        return this.address;
    }

    public int getZ() {
        return this.z;
    }

    public Double getLatitude() {
        return this.latitude;
    }

    public Double getLongitude() {
        return this.longitude;
    }
}