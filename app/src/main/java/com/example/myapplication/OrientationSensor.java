package com.example.myapplication;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.Locale;

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
    private TextToSpeech textToSpeech;
    private Context context;

    public OrientationSensor(Context context, TwoDViewFragment twoDViewFragment2, NavApi navApiInterface) {
        this.context = context;
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.accelerometer = this.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.magnetometer = this.sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        this.twoDViewFragment = twoDViewFragment2;
        this.mainActivity = (MainActivity) context;
        this.navApiInterface = navApiInterface;

        // Initialize Text-to-Speech
        textToSpeech = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.getDefault());
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("OrientationSensor", "Language not supported");
                }
            } else {
                Log.e("OrientationSensor", "Initialization failed");
            }
        });
    }

    public void startSensor() {
        this.sensorManager.registerListener(this, this.accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        this.sensorManager.registerListener(this, this.magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void stopSensor() {
        this.sensorManager.unregisterListener(this, this.accelerometer);
        this.sensorManager.unregisterListener(this, this.magnetometer);
    }

    public void shutdownTTS() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    public double getOrientation(){
        return this.orientation;
    }

    public void setBleScanner(BLEScanner bleScanner){
        this.bleScanner = bleScanner;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            this.accelerometer_values = event.values.clone();
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            this.magnetometer_values = event.values.clone();
        }
        if (this.accelerometer_values != null && this.magnetometer_values != null) {
            float[] rotation_matrix = new float[9];
            float[] orientation_values = new float[3];
            if (SensorManager.getRotationMatrix(rotation_matrix, null, this.accelerometer_values, this.magnetometer_values)) {
                SensorManager.getOrientation(rotation_matrix, orientation_values);
            }
            orientation = Math.toDegrees(orientation_values[0]);
            if (orientation < 0) {
                orientation += 360;
            }

            if (this.twoDViewFragment.isInitializedMap()) {
                this.twoDViewFragment.updateCameraBearing(orientation);
            }
            CardinalDirection cardinalDirection = CardinalDirection.normalizeDegree(orientation);
            if (cardinalDirection != this.prevCardinalDirection){
                if(this.mainActivity.getCurrentDestination() != null && bleScanner != null && bleScanner.getPreviousPrediction() != null){
                    navApiInterface.getRoute(this.mainActivity.getCurrentDestination().getId(), bleScanner.getPreviousPrediction().getMac().getLongitude(), bleScanner.getPreviousPrediction().getMac().getLatitude(), bleScanner.getPreviousPrediction().getMac().getZ()).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            try {
                                JsonObject routeData = new Gson().fromJson(response.body().string(), JsonObject.class);
                                if(!routeData.get("direction").isJsonNull()){
                                    Direction xDirection = Direction.getDirectionX(routeData.get("direction").getAsDouble(), orientation);
                                    provideNavigationInstruction(xDirection);
                                    mainActivity.updateBottomSheetInfo(xDirection.toString());
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

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void speak(String message) {
        if (textToSpeech != null) {
            textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    public void setLanguage(Locale locale) {
        if (textToSpeech != null) {
            int result = textToSpeech.setLanguage(locale);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("OrientationSensor", "Language not supported");
            }
        }
    }

    public void provideNavigationInstruction(Direction direction) {
        // Set the language based on user selection
        setLanguage(Locale.getDefault());


        switch (direction) {
            case FRONT:
                speak(context.getString(R.string.walk_straight)); // Use localized string
                break;
            case LEFT:
                speak(context.getString(R.string.turn_left)); // Use localized string
                break;
            case BACK:
                speak(context.getString(R.string.walk_backward)); // Use localized string
                break;
            case RIGHT:
                speak(context.getString(R.string.turn_right)); // Use localized string
                break;
            case UP:
                speak(context.getString(R.string.go_up)); // Use localized string
                break;
            case DOWN:
                speak(context.getString(R.string.go_down)); // Use localized string
                break;
            case COMPLETE:
                speak(context.getString(R.string.complete));
                break;
            default:
                speak(context.getString(R.string.walk_straight)); // Use localized string
                break;
        }
    }
}
