package com.example.myapplication;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myapplication.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS = 1;
    private BLEScanner bleScanner;
    private TensorFlowLiteModel tfLiteModel;
    private ActivityMainBinding binding;
    private Switch recordSwitch;
    private DatabaseHelper dbHelper;

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

        tfLiteModel = new TensorFlowLiteModel(getAssets(), "model.tflite");
        bleScanner = new BLEScanner(this, tfLiteModel, this);

        recordSwitch = findViewById(R.id.record_to_database);
        recordSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> bleScanner.setRecording(isChecked));

        dbHelper = new DatabaseHelper(this);

        Button deleteButton = findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(v -> deleteData());
    }

    private void deleteData() {
        dbHelper.deleteAllData();
    }

    private void checkPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                Log.d("hey", "BLUETOOTH_SCAN");
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_SCAN);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.d("hey", "BLUETOOTH_CONNECT");
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                Log.d("hey", "BLUETOOTH_ADMIN");

                permissionsNeeded.add(Manifest.permission.BLUETOOTH);
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_ADMIN);
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("hey", "ACCESS_FINE_LOCATION");

            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toArray(new String[0]), REQUEST_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

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
        Log.d("state", "resume");
        super.onResume();
        bleScanner.startScan();
    }

    @Override
    protected void onPause() {
        Log.d("state", "pause");

        super.onPause();
        bleScanner.stopScan();
    }

    @Override
    protected void onDestroy() {
        Log.d("state", "destroy");

        super.onDestroy();
        bleScanner.stopScan();
    }
}
