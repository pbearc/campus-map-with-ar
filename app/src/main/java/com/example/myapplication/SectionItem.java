package com.example.myapplication;

public class SectionItem {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ITEM = 1;

    private int type;
    private String text;
    private String location;

    public SectionItem(int type, String text, String location) {
        this.type = type;
        this.text = text;
        this.location = location;
    }

    public int getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public String getLocation(){
        return location;
    }
}
