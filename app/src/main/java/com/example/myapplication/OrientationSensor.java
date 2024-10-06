package com.example.myapplication;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrientationSensor implements SensorEventListener {
    private Sensor accelerometer;
    private float[] accelerometer_values;
    private Sensor magnetometer;
    private float[] magnetometer_values;
    private SensorManager sensorManager;
    private TwoDViewFragment twoDViewFragment;
    private double orientation;
    private CardinalDirection prevCardinalDirection;
    private MainActivity mainActivity;
    private NavApi navApiInterface;
    private BLEScanner bleScanner;

    public OrientationSensor(Context context, TwoDViewFragment twoDViewFragment2, NavApi navApiInterface) {
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.accelerometer = this.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.magnetometer = this.sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        this.twoDViewFragment = twoDViewFragment2;
        this.mainActivity = (MainActivity) context;
        this.navApiInterface = navApiInterface;
    }

    public void startSensor() {
        this.sensorManager.registerListener(this, this.accelerometer, 2);
        this.sensorManager.registerListener(this, this.magnetometer, 2);
    }

    public void stopSensor() {
        this.sensorManager.unregisterListener(this, this.accelerometer);
        this.sensorManager.unregisterListener(this, this.magnetometer);
    }

    public double getOrientation(){
        return this.orientation;
    }

    public void setBleScanner(BLEScanner bleScanner){
        this.bleScanner = bleScanner;
    }

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == 1) {
            this.accelerometer_values = event.values;
        } else if (event.sensor.getType() == 2) {
            this.magnetometer_values = event.values;
        }
        if (this.accelerometer_values != null && this.magnetometer_values != null) {
            float[] rotation_matrix = new float[9];
            float[] orientation_values = new float[3];
            if (SensorManager.getRotationMatrix(rotation_matrix, new float[9], this.accelerometer_values, this.magnetometer_values)) {
                SensorManager.getOrientation(rotation_matrix, orientation_values);
            }
            float f = orientation_values[1];
            float f2 = orientation_values[2];
            orientation = (180.0d * ((double) orientation_values[0])) / 3.141592653589793d;
            if (this.twoDViewFragment.isInitializedMap()) {
                this.twoDViewFragment.updateCameraBearing(Double.valueOf(orientation < 0.0d ? 360.0d + orientation : orientation));
            }
            CardinalDirection cardinalDirection = CardinalDirection.normalizeDegree(orientation);
            if (cardinalDirection != this.prevCardinalDirection){
                if(this.mainActivity.getCurrentDestination() != null){
                    navApiInterface.getRoute(this.mainActivity.getCurrentDestination().getId(), bleScanner.getPreviousPrediction().getMac().getLongitude(), bleScanner.getPreviousPrediction().getMac().getLatitude(), bleScanner.getPreviousPrediction().getMac().getZ()).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            try {
                                JsonObject routeData = (JsonObject) new Gson().fromJson(response.body().string(), JsonObject.class);
                                if(!routeData.get("direction").isJsonNull()){
                                    Direction xDirection = Direction.getDirectionX(routeData.get("direction").getAsDouble(), orientation < 0.0d ? 360.0d + orientation : orientation);
                                    Toast.makeText(mainActivity, xDirection.toString(), Toast.LENGTH_SHORT).show();
                                }
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                            Log.e("OrientationSensor", throwable.getMessage());
                        }
                    });
                }
                this.prevCardinalDirection = cardinalDirection;
            }
            this.accelerometer_values = null;
            this.magnetometer_values = null;
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
