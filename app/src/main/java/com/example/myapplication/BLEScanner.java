package com.example.myapplication;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
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
                    Log.d(BLEScanner.TAG, scanned_mac_address + ":" + Integer.toString(scanned_mac_rssi) + " | " + BLEScanner.this.previousPrediction.getMac_address() + ":" + Integer.toString(BLEScanner.this.previousPrediction.getRssi()));
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
                }
                if ((BLEScanner.this.mainActivity.getCurrentDestination() == null && BLEScanner.this.isRouting.booleanValue()) || (BLEScanner.this.mainActivity.getCurrentDestination() != null && !BLEScanner.this.isRouting.booleanValue())) {
                    updateRouteUI(mainHandler, scanned_mac.getLatitude(), scanned_mac.getLongitude(), scanned_mac.getZ());
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
                            if(!routeData.get("direction").isJsonNull()){
                                Direction xDirection = Direction.getDirectionX(routeData.get("direction").getAsDouble(), orientationSensor.getOrientation());
                                Toast.makeText(context, xDirection.toString(), Toast.LENGTH_SHORT).show();
                            }
                            if(!routeData.get("floor").isJsonNull()){
                                Direction yDirection = Direction.getDirectionY(routeData.get("floor").getAsInt());
                                Toast.makeText(context, yDirection.toString(), Toast.LENGTH_SHORT).show();
                            }
                            List<List<Double>> floorRoute = new ArrayList<>();
                            Iterator<JsonElement> it = route.iterator();
                            while (it.hasNext()) {
                                JsonArray coordinate = it.next().getAsJsonArray();
                                floorRoute.add(new ArrayList<>(List.of(Double.valueOf(coordinate.get(0).getAsDouble()), Double.valueOf(coordinate.get(1).getAsDouble()))));
                            }
                            handler.post(() -> {
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