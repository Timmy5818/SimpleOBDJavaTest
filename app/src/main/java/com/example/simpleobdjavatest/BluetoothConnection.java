package com.example.simpleobdjavatest;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.obd.metrics.transport.AdapterConnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BluetoothConnection implements AdapterConnection {
    private static final String LOGGER_TAG = "BluetoothConnection";
    private static final UUID RFCOMM_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final String deviceName;
    private BluetoothSocket socket;
    private InputStream input;
    private OutputStream output;

    private Context context;

    public BluetoothConnection(String deviceName) {
        this.deviceName = deviceName;
        Log.i(LOGGER_TAG, "Created instance of BluetoothConnection with device: " + deviceName);
    }

    @Override
    public void reconnect() {
        try {
            Log.i(LOGGER_TAG, "Reconnecting to the device: " + deviceName);
            close();
            TimeUnit.MILLISECONDS.sleep(1000);
            connect();
            Log.i(LOGGER_TAG, "Successfully reconnected to the device: " + deviceName);
        } catch (InterruptedException | IOException e) {
            Log.e(LOGGER_TAG, "Error reconnecting to the device: " + deviceName, e);
        }
    }

//    @SuppressLint("MissingPermission")
//    @Override
//    public void connect() throws IOException {
//        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        BluetoothDevice device = null;
//
//        if (bluetoothAdapter != null) {
//            for (BluetoothDevice bondedDevice : bluetoothAdapter.getBondedDevices()) {
//                if (bondedDevice.getName() != null && bondedDevice.getName().contains("OBD")) {
//                    device = bondedDevice;
//                    Log.i(LOGGER_TAG, "OBD Device found: " + bondedDevice.getName());
//                    break;
//                }
//            }
//
//            if (device == null) {
//                throw new IOException("Device not found: " + deviceName);
//            }
//
////            socket = device.createRfcommSocketToServiceRecord(RFCOMM_UUID);
//            try {
//                socket = (BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(device, 1);
//            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
//                throw new RuntimeException(e);
//            }
//
//            if (socket != null) {
//                socket.connect();
//
//                if (socket.isConnected()) {
//                    input = socket.getInputStream();
//                    output = socket.getOutputStream();
//                    Log.i(LOGGER_TAG, "Successfully connected to the device: " + deviceName);
//
//                    // Send Connected Broadcast
//                    Intent intent = new Intent(OBDBluetoothService.ACTION_OBD_STATE);
//                    intent.putExtra(OBDBluetoothService.EXTRA_OBD_STATE, 1);
//                } else {
//                    throw new IOException("Failed to connect to the device: " + deviceName);
//                }
//            }
//
//
//
//        } else {
//            throw new IOException("BluetoothAdapter not found");
//        }
//    }

    // 修改構造函數以包括Context
    public BluetoothConnection(Context context, String deviceName) {
        this.context = context;
        this.deviceName = deviceName;
        Log.i(LOGGER_TAG, "Created instance of BluetoothConnection with device: " + deviceName);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void connect() throws IOException {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = null;

        if (bluetoothAdapter != null) {
            for (BluetoothDevice bondedDevice : bluetoothAdapter.getBondedDevices()) {
                if (bondedDevice.getName() != null && bondedDevice.getName().contains("OBD")) {
                    device = bondedDevice;
                    Log.i(LOGGER_TAG, "OBD Device found: " + bondedDevice.getName());
                    break;
                }
            }

            if (device == null) {
                throw new IOException("Device not found: " + deviceName);
            }

            // find BluetoothDevice object to connect
            socket = device.createRfcommSocketToServiceRecord(RFCOMM_UUID);
            socket.connect();

            if (socket.isConnected()) {
                input = socket.getInputStream();
                output = socket.getOutputStream();
                Log.i(LOGGER_TAG, "Successfully connected to the device: " + device.getName());

                // 發送連線成功的廣播
                Intent intent = new Intent(OBDBluetoothService.ACTION_OBD_STATE);
                intent.putExtra(OBDBluetoothService.EXTRA_OBD_STATE, 1);
                context.sendBroadcast(intent);
            } else {
                throw new IOException("Failed to connect to the device: " + device.getName());
            }
        } else {
            throw new IOException("BluetoothAdapter not found");
        }
    }

    @Override
    public void close() {
        try {
            if (input != null) {
                input.close();
            }
            if (output != null) {
                output.close();
            }
            if (socket != null) {
                socket.close();
            }
            Log.i(LOGGER_TAG, "Socket for the device: " + deviceName + " is closed.");
        } catch (IOException e) {
            Log.e(LOGGER_TAG, "Error closing the socket: " + deviceName, e);
        }
    }

    @Override
    public OutputStream openOutputStream() {
        return output;
    }

    @Override
    public InputStream openInputStream() {
        return input;
    }

    @SuppressLint("MissingPermission")
    public void connectWithRetry(int retryCount) throws IOException {
        final int maxTries = retryCount;
        int attempt = 0;
        while (true) {
            try {
                Log.i("BluetoothConnection","Try OBD-II Connection");
                attempt++;
                connect(); // Try Connect
                break; // Connected, break while
            } catch (IOException e) {
                if (attempt > maxTries) {
                    Log.i("BluetoothConnection","OBD-II connect fail");
                    throw e; // Fail
                }
                // Wait some time before retrying
                try {
                    Log.i("BluetoothConnection","retry connect");
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt(); // stay disconnected
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void connectToDevice() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.i(LOGGER_TAG, "Bluetooth not supported on this device");
            return;
        }

        try {
            Log.i(LOGGER_TAG, "Found bounded connections, size: " + bluetoothAdapter.getBondedDevices().size());
            BluetoothDevice adapter = null;

            // find Bluetooth deviceName
            for (BluetoothDevice bondedDevice : bluetoothAdapter.getBondedDevices()) {
                if (deviceName.equals(bondedDevice.getName())) {
                    adapter = bondedDevice;
                    break;
                }
            }

            if (adapter != null) {
                Log.i(LOGGER_TAG, "Opening connection to bounded device: " + adapter.getName());
                socket = adapter.createRfcommSocketToServiceRecord(RFCOMM_UUID);
                socket.connect();
                Log.i(LOGGER_TAG, "Doing socket connect for: " + adapter.getName());

                if (socket.isConnected()) {
                    Log.i(LOGGER_TAG, "Successfully established connection for: " + adapter.getName());
                    input = socket.getInputStream();
                    output = socket.getOutputStream();
                    Log.i(LOGGER_TAG, "Successfully opened the sockets to device: " + adapter.getName());
                } else {
                    Log.e(LOGGER_TAG, "Failed to connect to the device: " + adapter.getName());
                }
            } else {
                Log.e(LOGGER_TAG, "Device not found: " + deviceName);
            }
        } catch (IOException e) {
            Log.e(LOGGER_TAG, "Failed to connect to the device: " + deviceName, e);
        } catch (SecurityException e) {
            Log.e(LOGGER_TAG, "Security exception: permissions might be missing.", e);
        }
    }

}