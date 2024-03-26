package com.example.simpleobdjavatest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.ParcelUuid;
import android.util.Log;

import org.obd.metrics.transport.AdapterConnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BluetoothConnection implements AdapterConnection {
    private static final String LOGGER_TAG = "BluetoothConnection";

    private final String deviceAddress;
    private BluetoothSocket socket;
    private InputStream input;
    private OutputStream output;

    public BluetoothConnection(String deviceAddress) {
        this.deviceAddress = deviceAddress;
        Log.i(LOGGER_TAG, "Created instance of BluetoothConnection with device: " + deviceAddress);
    }

    @Override
    public void reconnect() {
        try {
            Log.i(LOGGER_TAG, "Reconnecting to the device: " + deviceAddress);
            close();
            TimeUnit.MILLISECONDS.sleep(1000);
            connect();
            Log.i(LOGGER_TAG, "Successfully reconnected to the device: " + deviceAddress);
        } catch (InterruptedException | IOException e) {
            Log.e(LOGGER_TAG, "Error reconnecting to the device: " + deviceAddress, e);
        }
    }

    @Override
    public void connect() throws IOException {
        try{

            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice adapter = null;

            if (bluetoothAdapter != null) {

                for (BluetoothDevice bondedDevice : bluetoothAdapter.getBondedDevices()) {
                    if (bondedDevice.getName() != null && bondedDevice.getAddress().equals(deviceAddress)) {
                        adapter = bondedDevice;
                        Log.e(LOGGER_TAG, "OBD Device found: " + bondedDevice.getName());
                        break;
                    }
                }

                if (null == adapter) {
                    throw new IOException("Device not found: " + deviceAddress);
                }

                final ParcelUuid[] uuids = adapter.getUuids();
                final UUID uuid = uuids[0].getUuid();
                socket = adapter.createInsecureRfcommSocketToServiceRecord(uuid);
                socket.connect();

                if (socket.isConnected()) {
                    input = socket.getInputStream();
                    output = socket.getOutputStream();
                    Log.e(LOGGER_TAG, "Successfully connected to the adapter: " + deviceAddress);
                } else {
                    throw new IOException("Failed to connect to the adapter: " + deviceAddress);
                }
            } else {
                throw new IOException("BluetoothAdapter not found");
            }
        }catch (SecurityException e){
            Log.e(LOGGER_TAG,"Failed to connect to BT due to missing permissions.",e);
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
            Log.e(LOGGER_TAG, "Socket for the device: " + deviceAddress + " is closed.");
        } catch (IOException e) {
            Log.e(LOGGER_TAG, "Error closing the socket: " + deviceAddress, e);
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
}