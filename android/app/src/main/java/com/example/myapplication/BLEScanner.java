package com.example.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BLEScanner {

    private static final String TAG = "BLEScanner";
    private String[] targetBeaconsName = {"Daikin"};
    private BluetoothAdapter bluetoothAdapter;
    private boolean scanning;
    private Handler handler;
    private List<ScanResult> scanResults = new ArrayList<>();
    private TensorFlowLiteModel tfLiteModel;
    private Context context;

    public BLEScanner(Context context, TensorFlowLiteModel tfLiteModel) {
        this.tfLiteModel = tfLiteModel;
        this.context = context.getApplicationContext();
        handler = new Handler();
        final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null) {
            Log.e(TAG, "Bluetooth Adapter is null");
        }
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Bluetooth permissions not granted");
                return;
            }
            int rssi = result.getRssi();
            Log.d(TAG, "onScanResult: " + result.getDevice().getName() + " RSSI: " + rssi);
            scanResults.add(result);
            handleScanResult(scanResults);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Bluetooth permissions not granted");
                return;
            }
            Log.i("BLEScanner", "onBatchScanResults");
            scanResults.addAll(results);
            handleScanResult(scanResults);
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e(TAG, "BLE scan failed with error code:"+ errorCode);
            Log.e(TAG, "BLE scan failed with error code:"+ errorCode);
            switch (errorCode) {
                case ScanCallback.SCAN_FAILED_ALREADY_STARTED:
                    Log.e(TAG, "Scan failed: Scan already started");
                    break;
                case ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                    Log.e(TAG, "Scan failed: Application registration failed");
                    break;
                case ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED:
                    Log.e(TAG, "Scan failed: Feature unsupported");
                    break;
                case ScanCallback.SCAN_FAILED_INTERNAL_ERROR:
                    Log.e(TAG, "Scan failed: Internal error");
                    break;
                default:
                    Log.e(TAG, "BLE scan failed with unknown error code: " + errorCode);
            }
        }


    };

    public void startScan() {
        if (!scanning) {
            scanResults.clear();
            if (bluetoothAdapter != null ) {
                scanning = true;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        Log.e(TAG, "Bluetooth scan/connect permissions not granted");
                        return;
                    }
                    bluetoothAdapter.getBluetoothLeScanner().startScan(buildScanFilters(), buildScanSettings(), scanCallback);
                    Log.d(TAG, "Scanning started");
                } else {
                    Log.d(TAG, "Device is running pre-Lollipop version, scan not supported");
                }
            } else {
                Log.e(TAG, "BluetoothAdapter is null or not enabled");
            }
        }
    }

    public void stopScan() {
        if (scanning && bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            scanning = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "Bluetooth scan/connect permissions not granted");
                    return;
                }
                bluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
                Log.d(TAG, "Scanning stopped");
            } else {
                Log.d(TAG, "Device is running pre-Lollipop version, scan not supported");
            }
        }
    }

    private List<ScanFilter> buildScanFilters() {
        List<ScanFilter> filters = new ArrayList<>();
        // Implement scan filters if needed
        return filters;
    }

    private ScanSettings buildScanSettings() {
        ScanSettings.Builder builder = new ScanSettings.Builder();
        builder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        return builder.build();
    }

    private void handleScanResult(List<ScanResult> results) {
        float[] rssiValues = new float[targetBeaconsName.length];
        Arrays.fill(rssiValues, Float.NaN); // Initialize with NaN to handle missing values
        for (ScanResult result : results) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Bluetooth permissions not granted");
                return;
            }
            String deviceName = result.getDevice().getName();
            int rssi = result.getRssi();
            int index = Arrays.asList(targetBeaconsName).indexOf(deviceName);
            if (index != -1) {
                rssiValues[index] = rssi;
            }
            Log.d(TAG, "device name: " + deviceName + "rssi: " + rssi);

        }

        int predictedPoint = tfLiteModel.predictPoint(rssiValues);
        Log.d(TAG, "Predicted Point: " + predictedPoint);
        // Update UI or perform other actions based on predictedPoint
    }
}
