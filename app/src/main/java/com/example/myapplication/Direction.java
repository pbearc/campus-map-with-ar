package com.example.myapplication;

public enum Direction {
    FRONT(315, 45),
    RIGHT(45, 135),
    BACK(135, 225),
    LEFT(225, 315),
    UP(null, null),
    DOWN(null, null),
    COMPLETE(null, null);

    private static final int LB_DEGREE = 0;
    private static final int UB_DEGREE = 360;
    private Integer ubDegree;
    private Integer lbDegree;

    Direction(Integer lbDegree, Integer ubDegree){
        this.ubDegree = ubDegree;
        this.lbDegree = lbDegree;
    }

    @Override
    public String toString() {
        switch (this) {
            case FRONT:
                return "Move Forward";
            case RIGHT:
                return "Turn Right";
            case BACK:
                return "Move Backward";
            case LEFT:
                return "Turn Left";
            case UP:
                return "To Upper Floor";
            case DOWN:
                return "To Lower Floor";
            case COMPLETE:
                return "Complete";
            default:
                return "Unknown Direction";
        }
    }

    public int getUbDegree() {
        return ubDegree;
    }

    public int getLbDegree() {
        return lbDegree;
    }

    public static Direction getDirectionX(double forwardAzimuth, double relativeOrientation){
        double deltaDegree = (forwardAzimuth - relativeOrientation + 360) % 360;
        for(Direction direction : Direction.values()){
            Integer lb = direction.getLbDegree();
            Integer ub = direction.getUbDegree();
            if (lb > ub) {
                if ((deltaDegree >= lb && deltaDegree <= Direction.UB_DEGREE) || (deltaDegree >= Direction.LB_DEGREE && deltaDegree <= ub)) return direction;
            } else {
                if (deltaDegree >= lb && deltaDegree <= ub) return direction;
            }
        }
        return null;
    }

    public static Direction getDirectionY(int zDiff){
        return zDiff > 0 ? Direction.DOWN : Direction.UP;
    }
}
