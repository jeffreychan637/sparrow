package com.jeffreychan637.sparrow;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by jeffreychan on 2/9/16.
 */
public class ConnectThread extends Thread {
    private final UUID appID = UUID.fromString("ae0267ba-82fe-4049-85b3-c8c1ad0ac854");
    private final BluetoothSocket bSocket;
    private final BluetoothDevice bDevice;
    private final BluetoothAdapter BA;
    private final ProtocolThread protocolThread;
    private final Object waitOn;

    public ConnectThread(BluetoothDevice device, BluetoothAdapter ba, ProtocolThread pr, Object waitON) {
        BluetoothSocket tmp = null;
        bDevice = device;
        BA = ba;
        protocolThread = pr;
        waitOn = waitON;

        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            Log.d("connect", "setting up socket" );
            tmp = device.createRfcommSocketToServiceRecord(appID);
        } catch (IOException e) { }
        bSocket = tmp;
    }

    public void run() {
        BA.cancelDiscovery();
        synchronized (waitOn) {
            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                Log.d("connect", "trying to connect socket" );
                bSocket.connect();
                Log.d("connect", "connected socket - now want to manage connection");
                protocolThread.manageConnection(bSocket, true);
            } catch (IOException connectException) { //occurs in 12 seconds if connection fails
                try {
                    bSocket.close();
                    Log.d("ConnectThread", "Connection failed; closing socket");
                } catch (IOException closeException) {
                    Log.d("ConnectThread", "Exception when connecting");
                } finally {
                    waitOn.notify();
                }
            }
        }
    }

    /** Will cancel an in-progress connection, and close the socket */
    public void cancel() {
        synchronized (waitOn) {
            try {
                bSocket.close();
            } catch (IOException e) {
            } finally {
                protocolThread.setExchangeState(ExchangeState.NOT_EXCHANGING);
                waitOn.notify();
            }
        }
    }
}
