package com.example.simpleobdjavatest;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import org.obd.metrics.api.Workflow;
import org.obd.metrics.api.model.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.BatchPolicy;
import org.obd.metrics.api.model.CachePolicy;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.Init.Header;
import org.obd.metrics.api.model.Init.Protocol;
import org.obd.metrics.api.model.Pids;
import org.obd.metrics.api.model.ProducerPolicy;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.command.group.DefaultCommandGroup;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class OBDBluetoothService extends Service {

    public static final String ACTION_OBD_STATE = "com.example.OBD.ACTION_OBD_STATE";
    public static final String EXTRA_OBD_STATE = "obd_state";
    public static final String EXTRA_OBD_SPEED = "obd_speed";

    private BluetoothAdapter bluetoothAdapter;
    private String obdMacAddress = null;

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Bluetooth is not support
            Log.e("OBDBluetoothService","Bluetooth is not support");
        } else {
            if (!bluetoothAdapter.isEnabled()) {
                // Open Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                enableBtIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(enableBtIntent);
            } else {
                scanForBluetoothDevices();
            }
        }


        return flags;
    }

    @SuppressLint("MissingPermission")
    private void scanForBluetoothDevices() {
        bluetoothAdapter.startDiscovery();
        BroadcastReceiver receiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String deviceName = null;

                    // Bluetooth Device name
                    if (device != null) {
                        deviceName = device.getName();
                    }

                    // Bluetooth Mac address
                    String deviceHardwareAddress = null;
                    if (device != null) {
                        deviceHardwareAddress = device.getAddress();
                    }

                    // Bluetooth name : OBD
                    if (deviceName != null && deviceName.contains("OBD")) {
                        obdMacAddress = deviceHardwareAddress;
                        Log.e("OBDBluetoothService","Mac address:" + obdMacAddress);

                        // Find Mac Address to Connect
                        if (obdMacAddress != null) {
                            try {
                                test();
                            } catch (IOException | ExecutionException | InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }

//                        BluetoothDevice deviceToConnect = bluetoothAdapter.getRemoteDevice(obdMacAddress);

                        // Set PIN
//                        deviceToConnect.setPin("1234".getBytes());
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
    }

    public void test() throws IOException, InterruptedException, ExecutionException {
        var connection = new BluetoothConnection(obdMacAddress);

        // var connection = new BluetoothConnection("CE:5E:89:74:3D:4C");
        // var connection = new BluetoothConnection("BA:0D:ED:5B:E7:35");
        var collector = new DataCollector();

        final Pids pids = Pids
                .builder()
                .resource(Objects.requireNonNull(Thread.currentThread().getContextClassLoader()).getResource("giulia_2.0_gme.json"))
                .resource(Objects.requireNonNull(Thread.currentThread().getContextClassLoader()).getResource("extra.json"))
                .resource(Objects.requireNonNull(Thread.currentThread().getContextClassLoader()).getResource("mode01.json"))
                .resource(Objects.requireNonNull(Thread.currentThread().getContextClassLoader()).getResource("mode01_2.json"))
                .build();

        int commandFrequency = 6;
        var workflow = Workflow.instance().pids(pids).observer(collector).initialize();

//        var query = Query.builder().pid(7005l).pid(7006l).pid(7007l).pid(7008l).build();

        // Enter the required OBD-II PID parameters (the PID parameters are defined in this file,
        // not as defined in the encyclopedia https://github.com/tzebrowski/ObdMetrics/blob/main/src/main/resources/mode01 )
        // Calculated Engine Load
        var calculatedEngineLoad = 5L;
        // Coolant
        var coolant= 6L;
        // Engine Speed
        var engineSpeedPID = 13L;
        // Speed
        var speed = 14L;
        // Control Module Voltage
        var controlModuleVoltage = 67L;
        var query = Query.builder().pid(engineSpeedPID).pid(coolant).pid(calculatedEngineLoad).
                pid(speed).pid(controlModuleVoltage).build();

        var optional = Adjustments
                .builder()
                .vehicleCapabilitiesReadingEnabled(Boolean.TRUE)
                .vehicleMetadataReadingEnabled(Boolean.TRUE)
                .adaptiveTimeoutPolicy(AdaptiveTimeoutPolicy.builder().enabled(Boolean.TRUE).checkInterval(5000).commandFrequency(commandFrequency).build())
                .producerPolicy(ProducerPolicy.builder().priorityQueueEnabled(Boolean.TRUE).build())
                .cachePolicy(CachePolicy.builder().resultCacheEnabled(Boolean.FALSE).build())
                .batchPolicy(
                        BatchPolicy.builder().responseLengthEnabled(Boolean.FALSE).enabled(Boolean.FALSE).build())
                .build();

        var init = Init.builder()
                .delayAfterInit(1000)
                .header(Header.builder().mode("22").header("DA10F1").build())
                .header(Header.builder().mode("01").header("DB33F1").build())
                .protocol(Protocol.CAN_29)
                .sequence(DefaultCommandGroup.INIT).build();

        workflow.start(connection, query, init, optional);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}