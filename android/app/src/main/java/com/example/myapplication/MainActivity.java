package com.example.myapplication;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myapplication.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private BLEScanner bleScanner;
    private TensorFlowLiteModel tfLiteModel;
    private ActivityMainBinding binding;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final int REQUEST_BLUETOOTH_PERMISSION = 2;
    private static final int REQUEST_BLUETOOTH_SCAN_PERMISSION = 3;
    private final BroadcastReceiver bluetoothReceiver = new BluetoothReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        int sdkVersion = Build.VERSION.SDK_INT;
        String versionName = Build.VERSION.RELEASE;

        Log.d("VersionCheck", "Android SDK Version: " + sdkVersion);
        Log.d("VersionCheck", "Android Version Name: " + versionName);

        checkPermissions();


        tfLiteModel = new TensorFlowLiteModel(this);
        bleScanner = new BLEScanner(this, tfLiteModel);

    }

    private void checkPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        // Check location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        // Check Bluetooth connect permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.BLUETOOTH_CONNECT);
        }

        // Check Bluetooth scan permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.BLUETOOTH_SCAN);
        }

        // Request all necessary permissions at once
        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toArray(new String[0]), REQUEST_PERMISSIONS);
        }
    }

    private static final int REQUEST_PERMISSIONS = 1; // A single request code for all permissions

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (permissions.length == 0 || grantResults.length == 0) {
            Log.e("Permissions", "Permission request results are empty.");
            return;
        }

        if (requestCode == REQUEST_PERMISSIONS) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Permissions", "Permission granted: " + permissions[i]);
                } else {
                    Log.d("Permissions", "Permission denied: " + permissions[i]);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        bleScanner.startScan();
    }

    @Override
    protected void onPause() {
        super.onPause();
        bleScanner.stopScan();
    }
}
