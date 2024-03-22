package com.example.simpleobdjavatest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.simpleobdjavatest.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    public Context mContext;

    private static final int PERMISSION_REQUEST_FINE_LOCATION = 2;
    private static final int MY_PERMISSIONS_REQUEST_BLUETOOTH_CONNECT = 1;
    private static final int MY_PERMISSIONS_REQUEST_BLUETOOTH_SCAN = 1;

    @SuppressLint("MissingPermission")
    @SuppressWarnings("all")
    private final BroadcastReceiver connectionStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (OBDBluetoothService.ACTION_OBD_STATE.equals(intent.getAction())) {
                int connect_state = intent.getIntExtra(OBDBluetoothService.EXTRA_OBD_STATE, 0);
                int speed = intent.getIntExtra(OBDBluetoothService.EXTRA_OBD_SPEED, 0);

                if (connect_state == 1) {
                    binding.connectState.setText("Device Connected");
                } else {
                    binding.connectState.setText("Not Connect");
                }

                if (binding != null) {
                    if (speed != 0 ) {
                        binding.testSpeed.setText(speed +"Km/h");
                    }
                }
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.S)
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // initialization BroadcastReceiver
        IntentFilter filter = new IntentFilter(OBDBluetoothService.ACTION_OBD_STATE);
        registerReceiver(connectionStateReceiver, filter);


        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_FINE_LOCATION);
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] { android.Manifest.permission.BLUETOOTH_SCAN },
                    MY_PERMISSIONS_REQUEST_BLUETOOTH_SCAN);
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.BLUETOOTH_CONNECT },
                    MY_PERMISSIONS_REQUEST_BLUETOOTH_CONNECT);
        }

        Log.i("MainActivity","Start OBD-II BluetoothService");
        Intent bsdIntent = new Intent(this, OBDBluetoothService.class);
        startService(bsdIntent);
    }

    private static Context sContext;

    public static void initializeContext(Context context) {
        sContext = context.getApplicationContext();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(connectionStateReceiver);
    }
}