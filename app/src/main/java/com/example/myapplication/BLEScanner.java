package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BLEScanner {

    private static final String TAG = "BLEScanner";
    private static final int REQUEST_BLUETOOTH_SCAN = 5;
    private String[] targetBeaconsMac = {"EF:74:5B:A5:63:17", "E3:8A:38:E0:92:15", "C7:79:80:05:D4:A8"};
    private BluetoothAdapter bluetoothAdapter;
    private boolean scanning;
    private Handler handler;
    private Context context;
    private MainActivity mainActivity;
    private Map<String, Integer> macToRSSI = new HashMap<>();
    private DatabaseHelper dbHelper;
    private boolean isRecording = false;
    private TensorFlowLiteModel tfmodel;
    private ScheduledExecutorService scheduler;
    private String BASE_URL = "http://192.168.43.226:5000";
    private NavApi navApiInterface;
    private Retrofit retrofit;

    public BLEScanner(Context context, TensorFlowLiteModel tfLiteModel, MainActivity mainActivity) {
        this.context = context;
        this.mainActivity = mainActivity;
        this.tfmodel = tfLiteModel;
        handler = new Handler();
        final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        dbHelper = new DatabaseHelper(context);

        if (bluetoothAdapter == null) {
            Log.e(TAG, "Bluetooth Adapter is null");
        }

        scheduler = Executors.newScheduledThreadPool(1);

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        navApiInterface = retrofit.create(NavApi.class);
    }

    public void setRecording(boolean recording) {
        isRecording = recording;
    }

    private final ScanCallback scanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                Log.e(TAG, "Bluetooth permissions not granted");
//                return;
            }

//            Log.d(TAG, "onScanResult: " + result.getDevice().getName()  +  result.getDevice().getAddress() + " RSSI: " + result.getRssi());

            String mac = result.getDevice().getAddress();
            if (mac != null && isTargetMac(mac)) {
                //Log.d(TAG, mac);
                int rssi = result.getRssi();
                addDevice(mac, rssi);
            }
        }

        private boolean isTargetMac(String macAddress) {
            for (String targetMac : targetBeaconsMac) {
                if (targetMac.equals(macAddress)) {
                    return true;
                }
            }
            return false;
        }

        public void addDevice(String macAddress, int rssi) {
            macToRSSI.put(macAddress, rssi);

            MainActivity activity = mainActivity;
            Handler mainHandler = new Handler(activity.getMainLooper());

            // Update UI on a separate background thread
            mainHandler.post(() -> {
                if (macAddress.equals(targetBeaconsMac[0])) {
//                    TextView one = activity.findViewById(R.id.one_i);
//                    one.setText(String.valueOf(rssi));
                    Log.d("SCAN_RESULT", "1" + String.valueOf(rssi));
                } else if (macAddress.equals(targetBeaconsMac[1])) {
//                    TextView two = activity.findViewById(R.id.two_i);
//                    two.setText(String.valueOf(rssi));
                    Log.d("SCAN_RESULT", "2" + String.valueOf(rssi));

                } else if (macAddress.equals(targetBeaconsMac[2])) {
//                    TextView three = activity.findViewById(R.id.three_i);
//                    three.setText(String.valueOf(rssi));
                    Log.d("SCAN_RESULT", "3" + String.valueOf(rssi));
                }
            });

//            if (isRecording && macToRSSI.size() == targetBeaconsMac.length) {
//                String point = ((TextView) activity.findViewById(R.id.cur_point)).getText().toString();
//                navApiInterface.addFingerprint(new FingerPrintPost(point, macToRSSI.get(targetBeaconsMac[0]), macToRSSI.get(targetBeaconsMac[1]), macToRSSI.get(targetBeaconsMac[2]))).enqueue(new Callback<ResponseBody>() {
//                    @Override
//                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                        Log.d(TAG, response.toString());
//                    }
//
//                    @Override
//                    public void onFailure(Call<ResponseBody> call, Throwable throwable) {
//                        Log.e(TAG, throwable.getMessage());
//                    }
//                });
//                dbHelper.insertRSSIData(macToRSSI.get(targetBeaconsMac[0]), macToRSSI.get(targetBeaconsMac[1]), macToRSSI.get(targetBeaconsMac[2]), point);
//                //macToRSSI.clear();
//            }

            if (macToRSSI.size() == targetBeaconsMac.length) {
                scheduler.scheduleAtFixedRate(() -> {
                    float[] input = new float[3];
                    for (int i = 0; i < targetBeaconsMac.length; i++) {
                        input[i] = macToRSSI.get(targetBeaconsMac[i]);
                    }

                    int prediction = tfmodel.predict(input);

                    // Update the prediction TextView on the UI thread
                    mainHandler.post(() -> {
//                        TextView predictionView = activity.findViewById(R.id.prediction_i);
//                        predictionView.setText(String.valueOf(prediction));
                    });
                }, 0, 1, TimeUnit.SECONDS);
            }
        }
    };

    public void startScan() {
        if (scanning) {
            return; // Avoid starting scan if already scanning
        }

        scanning = true;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.BLUETOOTH_SCAN}, REQUEST_BLUETOOTH_SCAN);
//            Log.d("starto","here");
//            return;
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                Log.d("what","what");
            }
        }
        bluetoothAdapter.getBluetoothLeScanner().startScan(scanCallback);
        //Log.d(TAG, Boolean.toString(bluetoothAdapter.startDiscovery()));
        Log.d(TAG, "BLE scan started");


    }

    public void stopScan() {
        if (!scanning) {
            return; // Avoid stopping scan if not scanning
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // Handle missing permissions
            return;
        }

        bluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
        scanning = false;
        Log.d(TAG, "BLE scan stopped");
    }
}
