package com.example.myapplication;

public enum CardinalDirection {
    NORTH,
    SOUTH,
    EAST,
    WEST;

    public static CardinalDirection normalizeDegree(double degree){
        double normalized = (degree + 360) % 360;
        if (normalized >= 315 || normalized <= 45){
            return CardinalDirection.NORTH;
        }else if (normalized >= 45 && normalized <= 135){
            return CardinalDirection.EAST;
        }else if (normalized >= 135 && normalized <= 225){
            return CardinalDirection.SOUTH;
        }else{
            return CardinalDirection.WEST;
        }
    }
}
