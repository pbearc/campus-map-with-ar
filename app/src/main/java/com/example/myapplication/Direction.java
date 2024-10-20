package com.example.myapplication;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

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

    private static Context context;
    public static void setContext(Context newContext){
        context = newContext;
    }

    @Override
    public String toString() {
//        Context appContext = context.getApplicationContext();

        switch (this) {
            case FRONT:
                return context.getString(R.string.move_forward_dir);
            case RIGHT:
                return context.getString(R.string.turn_right_dir);
            case BACK:
                return context.getString(R.string.move_backward_dir);
            case LEFT:
                return context.getString(R.string.turn_left_dir);
            case UP:
                return context.getString(R.string.to_upper_floor_dir);
            case DOWN:
                return context.getString(R.string.to_lower_floor_dir);
            case COMPLETE:
                return context.getString(R.string.complete_dir);
            default:
                return context.getString(R.string.unknown_direction_dir);
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
