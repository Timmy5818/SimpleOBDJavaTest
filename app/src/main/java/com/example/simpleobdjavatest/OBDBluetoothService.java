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

import org.assertj.core.api.Assertions;
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
import org.obd.metrics.diagnostic.RateType;
import org.obd.metrics.transport.AdapterConnection;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class OBDBluetoothService extends Service {

    public static final String ACTION_OBD_STATE = "com.example.OBD.ACTION_OBD_STATE";
    public static final String EXTRA_OBD_STATE = "obd_state";
    public static final String EXTRA_OBD_SPEED = "obd_speed";
    private BluetoothConnection bluetoothConnection;

    private final BroadcastReceiver connectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Objects.equals(intent.getAction(), ACTION_OBD_STATE)) {
                int state = intent.getIntExtra(EXTRA_OBD_STATE, 0);
                // Connected
                if (state == 1) {
                    Log.i("OBDBluetoothService","BroadcastReceiver OBD State is Connected");
                    // Run test()
                    try {
                        test();
                    } catch (IOException | InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    Log.i("OBDBluetoothService","BroadcastReceiver OBD State is not Connect");
                }
            }
        }
    };

//    @SuppressLint("UnspecifiedRegisterReceiverFlag")
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        bluetoothConnection = new BluetoothConnection("OBDII");
//        registerReceiver(connectionReceiver, new IntentFilter(ACTION_OBD_STATE));
//
////        try {
////            test();
////        } catch (IOException | ExecutionException | InterruptedException e) {
////            throw new RuntimeException(e);
////        }
//
//        new Thread(() -> {
//            try {
//                Log.i("OBDBluetoothService","Try OBD-II Connection");
//                bluetoothConnection = new BluetoothConnection("OBDII");
//                bluetoothConnection.connectWithRetry(3); // Try 3 times
//
//                Intent intent2 = new Intent(OBDBluetoothService.ACTION_OBD_STATE);
//                intent2.putExtra(OBDBluetoothService.EXTRA_OBD_STATE, 1);
//                test();
//            } catch (IOException | ExecutionException | InterruptedException e) {
//                Intent intent3 = new Intent(OBDBluetoothService.ACTION_OBD_STATE);
//                intent3.putExtra(OBDBluetoothService.EXTRA_OBD_STATE, 0);
//            }
//        }).start();
//        return flags;
//    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    public void onCreate() {
        super.onCreate();
        bluetoothConnection = new BluetoothConnection("OBD");

        // initialization BroadcastReceiver
        IntentFilter filter = new IntentFilter(OBDBluetoothService.ACTION_OBD_STATE);
        registerReceiver(connectionReceiver, filter);

        bluetoothConnection = new BluetoothConnection(this, "OBD");

//        try {
//            test();
//        } catch (IOException | ExecutionException | InterruptedException e) {
//            throw new RuntimeException(e);
//        }

        new Thread(() -> {
            try {
                Log.i("OBDBluetoothService","Try OBD-II Connection");
                bluetoothConnection = new BluetoothConnection("OBDII");
                bluetoothConnection.connectWithRetry(10); // Try 10 times

                Intent intent = new Intent(OBDBluetoothService.ACTION_OBD_STATE);
                intent.putExtra(OBDBluetoothService.EXTRA_OBD_STATE, 1);
                test();
            } catch (IOException | ExecutionException | InterruptedException e) {
                Intent intent = new Intent(OBDBluetoothService.ACTION_OBD_STATE);
                intent.putExtra(OBDBluetoothService.EXTRA_OBD_STATE, 0);
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(connectionReceiver);
    }

    public void sendBroadcast(Intent intent) {
        sendBroadcast(intent);
    }

    public void test() throws IOException, InterruptedException, ExecutionException {
        AdapterConnection connection = bluetoothConnection;
        // call connect()
        connection.connect();
        var collector = new DataCollector(this);

        final Pids pids = Pids
                .builder()
                .resource(Objects.requireNonNull(Thread.currentThread().getContextClassLoader()).getResource("giulia_2.0_gme.json"))
                .resource(Objects.requireNonNull(Thread.currentThread().getContextClassLoader()).getResource("extra.json"))
                .resource(Objects.requireNonNull(Thread.currentThread().getContextClassLoader()).getResource("mode01.json"))
                .build();

        int commandFrequency = 6;
        var workflow = Workflow.instance().pids(pids).observer(collector).initialize();

        var query = Query.builder().pid(7005l).pid(7006l).pid(7007l).pid(7008l).build();

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

        WorkflowFinalizer.finalizeAfter(workflow,25000);

        var registry = workflow.getPidRegistry();

        var intakePressure = registry.findBy(7005l);
        double ratePerSec = workflow.getDiagnostics().rate().findBy(RateType.MEAN, intakePressure).get().getValue();

        Assertions.assertThat(ratePerSec).isGreaterThanOrEqualTo(commandFrequency);
    }

    @SuppressLint("MissingPermission")
    private BluetoothDevice getDeviceByName(String name) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            if (pairedDevices != null && !pairedDevices.isEmpty()) {
                for (BluetoothDevice device : pairedDevices) {
                    if (device.getName().equals(name)) {
                        return device;
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}