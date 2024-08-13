package com.example.myapplication;

public class PointOfInterest {
    private int id;
    private String name;
    private double latitude;
    private double longitude;
    private int z;
    private String identifier;

    public PointOfInterest(int id , String name, double latitude, double longitude, int z, String identifier){
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.z = z;
        this.identifier = identifier;
    }

    public int getZ() {
        return z;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public String getIdentifier() {
        return identifier;
    }
}
