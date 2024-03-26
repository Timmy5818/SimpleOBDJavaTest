package com.example.simpleobdjavatest;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import com.example.simpleobdjavatest.databinding.ActivityMainBinding;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

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
        EasyPermissions.requestPermissions(this,"I need BT permissions ",1,  Manifest.permission.BLUETOOTH_ADMIN,  Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_CONNECT);

        IntentFilter filter = new IntentFilter(OBDBluetoothService.ACTION_OBD_STATE);
        registerReceiver(connectionStateReceiver, filter);

        Log.i("MainActivity","Start OBD-II BluetoothService");
        Intent obdIntent = new Intent(this, OBDBluetoothService.class);
        startService(obdIntent);

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