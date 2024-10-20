package com.example.myapplication;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BLEScanner {
    private static final int REQUEST_BLUETOOTH_SCAN = 5;
    private static final String TAG = "BLEScanner";
    private BluetoothAdapter bluetoothAdapter;
    private Context context;
    private Boolean isRouting;
    private MainActivity mainActivity;
    private NavApi navApiInterface;
    private Prediction previousPrediction;
    private TextToSpeech textToSpeech;

    public Prediction getPreviousPrediction(){
        return this.previousPrediction;
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        public void onScanResult(int callbackType, ScanResult result) {
            if (ActivityCompat.checkSelfPermission(BLEScanner.this.context, "android.permission.BLUETOOTH_CONNECT") != 0) {
                Log.e(BLEScanner.TAG, "Bluetooth permissions not granted");
                return;
            }
            String scanned_mac_address = result.getDevice().getAddress();
            int scanned_mac_rssi = result.getRssi();
            Mac scanned_mac = BLEScanner.this.targetBeacons.get(scanned_mac_address);
            Handler mainHandler = new Handler(BLEScanner.this.mainActivity.getMainLooper());
            if (scanned_mac_address != null && BLEScanner.this.targetBeacons.containsKey(scanned_mac_address)) {
                if (BLEScanner.this.previousPrediction != null) {
                    //Log.d(BLEScanner.TAG, scanned_mac_address + ":" + Integer.toString(scanned_mac_rssi) + " | " + BLEScanner.this.previousPrediction.getMac_address() + ":" + Integer.toString(BLEScanner.this.previousPrediction.getRssi()));
                }
                if (BLEScanner.this.previousPrediction == null) {
                    updateUserMapUI(mainHandler, scanned_mac.getLatitude(), scanned_mac.getLongitude(), scanned_mac.getZ());
                    Prediction unused = BLEScanner.this.previousPrediction = new Prediction(scanned_mac_address, scanned_mac_rssi, scanned_mac);
                } else if (scanned_mac_address.equals(BLEScanner.this.previousPrediction.getMac_address())) {
                    BLEScanner.this.previousPrediction.setRssi(scanned_mac_rssi);
                } else if (scanned_mac_rssi > BLEScanner.this.previousPrediction.getRssi()) {
                    updateUserMapUI(mainHandler, scanned_mac.getLatitude(), scanned_mac.getLongitude(), scanned_mac.getZ());
                    updateRouteUI(mainHandler, scanned_mac.getLatitude(), scanned_mac.getLongitude(), scanned_mac.getZ());
                    BLEScanner.this.previousPrediction.setMac_address(scanned_mac_address);
                    BLEScanner.this.previousPrediction.setRssi(scanned_mac_rssi);
                    BLEScanner.this.previousPrediction.setMac(scanned_mac);
                    //provideNavigationInstruction(Direction.COMPLETE);
                }
                if ((BLEScanner.this.mainActivity.getCurrentDestination() == null && BLEScanner.this.isRouting.booleanValue()) || (BLEScanner.this.mainActivity.getCurrentDestination() != null && !BLEScanner.this.isRouting.booleanValue())) {
                    updateRouteUI(mainHandler, previousPrediction.getMac().getLatitude(), previousPrediction.getMac().getLongitude(), previousPrediction.getMac().getZ());
                }
            }
        }

        public void updateUserMapUI(Handler handler, Double latitude, Double longitude, int z) {
            if (BLEScanner.this.twoDViewFragment.isInitializedMap()) {
                handler.post(() -> {
                    BLEScanner.this.twoDViewFragment.updateMap(z);
                    BLEScanner.this.twoDViewFragment.addUserPosition(latitude, longitude);
                });
                BLEScanner.this.twoDViewFragment.updateCameraPosition(latitude, longitude);
            }
        }

        public void updateRouteUI(final Handler handler, Double latitude, Double longitude, final int z) {
            setLanguage(Locale.getDefault());
            if (BLEScanner.this.twoDViewFragment.isInitializedMap()) {
                if (BLEScanner.this.mainActivity.getCurrentDestination() == null) {
                    Boolean unused = BLEScanner.this.isRouting = false;
                    handler.post(() -> {
                        BLEScanner.this.twoDViewFragment.removeRoute();
                    });
                    return;
                }
                Boolean unused2 = BLEScanner.this.isRouting = true;
                BLEScanner.this.navApiInterface.getRoute(BLEScanner.this.mainActivity.getCurrentDestination().getId(), longitude.doubleValue(), latitude.doubleValue(), z).enqueue(new Callback<ResponseBody>() {
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            JsonObject routeData = (JsonObject) new Gson().fromJson(response.body().string(), JsonObject.class);
                            JsonArray route = routeData.get("route").getAsJsonObject().get(Integer.toString(z)).getAsJsonArray();
                            int distance = routeData.get("distance").getAsInt();

                            String directionString = "";


                            if(!routeData.get("direction").isJsonNull()){
                                Direction xDirection = Direction.getDirectionX(routeData.get("direction").getAsDouble(), orientationSensor.getOrientation());
//                                update snackbar with the latest xDirection.toString()
                                Log.d(TAG, Double.toString(latitude) + " | " + Double.toString(longitude));
                                if (latitude == 3.06472595220122 && longitude == 101.600474029472){
                                    provideNavigationInstruction(Direction.COMPLETE);
                                    directionString = "COMPLETED";
                                } else{
                                    provideNavigationInstruction(xDirection);
                                    directionString = xDirection.toString();
                                }


                            }
                            if(!routeData.get("floor").isJsonNull()){
                                Direction yDirection = Direction.getDirectionY(routeData.get("floor").getAsInt());
                                directionString = yDirection.toString();
                            }
                            List<List<Double>> floorRoute = new ArrayList<>();
                            Iterator<JsonElement> it = route.iterator();
                            while (it.hasNext()) {
                                JsonArray coordinate = it.next().getAsJsonArray();
                                floorRoute.add(new ArrayList<>(List.of(Double.valueOf(coordinate.get(0).getAsDouble()), Double.valueOf(coordinate.get(1).getAsDouble()))));
                            }

                            // Create final variables to pass to the lambda
                            final String finalDirectionString = directionString;
                            final String finalTimeAndDistance = "2 min" + " | " + distance +" m";
                            final String finalArrivalTime = context.getString(R.string.arrival_time)+ ": 12:34pm"; // Placeholder

                            handler.post(() -> {
                                mainActivity.updateBottomSheetInfo(finalDirectionString, finalTimeAndDistance, finalArrivalTime);
                                BLEScanner.this.twoDViewFragment.removeRoute();
                                BLEScanner.this.twoDViewFragment.addRoute(floorRoute);
                            });
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                        Log.e(BLEScanner.TAG, throwable.getMessage());
                    }
                });
            }
        }
    };

    private boolean scanning;
    private Map<String, Mac> targetBeacons;
    private TwoDViewFragment twoDViewFragment;
    private OrientationSensor orientationSensor;

    public BLEScanner(Context context2, MainActivity mainActivity2, NavApi navApiInterface2, TwoDViewFragment twoDViewFragment2, OrientationSensor orientationSensor) {
        this.context = context2;
        this.mainActivity = mainActivity2;
        this.bluetoothAdapter = ((BluetoothManager) context2.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        if (this.bluetoothAdapter == null) {
            Log.e(TAG, "Bluetooth Adapter is null");
        }
        this.navApiInterface = navApiInterface2;
        this.twoDViewFragment = twoDViewFragment2;
        this.targetBeacons = new HashMap();
        this.isRouting = false;
        this.orientationSensor = orientationSensor;
        navApiInterface2.getMacs().enqueue(new Callback<List<Mac>>() {

            public void onResponse(Call<List<Mac>> call, Response<List<Mac>> response) {
                for (Mac mac : response.body()) {
                    BLEScanner.this.targetBeacons.put(mac.getAddress(), mac);
                }
            }

            public void onFailure(Call<List<Mac>> call, Throwable throwable) {
                Log.e(BLEScanner.TAG, throwable.getMessage());
            }
        });

        // Initialize Text-to-Speech
        textToSpeech = new TextToSpeech(context2, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e(TAG, "Language not supported");
                    }
                } else {
                    Log.e(TAG, "Initialization failed");
                }
            }
        });
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
                Log.e(TAG, "Language not supported");
            }
        }
    }


    public void provideNavigationInstruction(Direction direction) {
        // Set the language based on user selection (for example, Locale.FRENCH)
        setLanguage(Locale.getDefault()); // Use the user's default locale or a specific locale

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




    public void shutdownTTS() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    public void startScan() {
        if (!this.scanning) {
            this.scanning = true;
            if (ActivityCompat.checkSelfPermission(this.context, "android.permission.BLUETOOTH_SCAN") != 0) {
                ActivityCompat.requestPermissions((Activity) this.context, new String[]{"android.permission.BLUETOOTH_SCAN"}, 5);
                if (ActivityCompat.checkSelfPermission(this.context, "android.permission.BLUETOOTH_SCAN") != 0) {
                    Log.d("what", "what");
                }
            }
            this.bluetoothAdapter.getBluetoothLeScanner().startScan(this.scanCallback);
            Log.d(TAG, "BLE scan started");
        }
    }

    public void stopScan() {
        if (this.scanning && ActivityCompat.checkSelfPermission(this.context, "android.permission.BLUETOOTH_SCAN") == 0) {
            this.bluetoothAdapter.getBluetoothLeScanner().stopScan(this.scanCallback);
            this.scanning = false;
            Log.d(TAG, "BLE scan stopped");
        }
    }
}