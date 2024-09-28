package com.example.myapplication;

public class PointOfInterest {
    private int id;
    private String identifier;
    private double latitude;
    private double longitude;
    private String name;
    private int z;

    public PointOfInterest(int id2, String name2, double latitude2, double longitude2, int z2, String identifier2) {
        this.id = id2;
        this.name = name2;
        this.latitude = latitude2;
        this.longitude = longitude2;
        this.z = z2;
        this.identifier = identifier2;
    }

    public int getZ() {
        return this.z;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public String getName() {
        return this.name;
    }

    public int getId() {
        return this.id;
    }

    public String getIdentifier() {
        return this.identifier;
    }
}
