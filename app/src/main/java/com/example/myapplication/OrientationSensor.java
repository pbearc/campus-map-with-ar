package com.example.myapplication;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class OrientationSensor implements SensorEventListener {
    private Sensor accelerometer;
    private float[] accelerometer_values;
    private Sensor magnetometer;
    private float[] magnetometer_values;
    private SensorManager sensorManager;
    private TwoDViewFragment twoDViewFragment;
    private double orientation;

    public OrientationSensor(Context context, TwoDViewFragment twoDViewFragment2) {
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.accelerometer = this.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.magnetometer = this.sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        this.twoDViewFragment = twoDViewFragment2;
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
            this.accelerometer_values = null;
            this.magnetometer_values = null;
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
