package com.example.myapplication;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

public class BluetoothReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("BluetoothReceiver", "model+scanner");

        if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);

            // Ensure the context is an instance of MainActivity
            if (context instanceof MainActivity) {
                MainActivity activity = (MainActivity) context;

                // Check for permissions
                if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // Request permissions if necessary
                    return;
                }

                // Update UI on the main thread
                new Handler(Looper.getMainLooper()).post(() -> {
                    TextView one = activity.findViewById(R.id.one_i);
                    Log.d("BluetoothReceiver", one.toString());

                    if (one != null) {
                        one.setText("Bluetooth event received!");
                    }

                    TextView two = activity.findViewById(R.id.two_i);
                    if (two != null) {
                        two.setText("" + device.getName() + ", RSSI: " + rssi);
                    }
                });
//                if (device.getName().equals("Holy-IOTrssi")) {
                    Log.d("BluetoothReceiver", "Device: " + device.getName() + ", RSSI: " + rssi);
//                }
            } else {
                Log.e("BluetoothReceiver", "Context is not an instance of MainActivity");
            }
        }
    }
}
