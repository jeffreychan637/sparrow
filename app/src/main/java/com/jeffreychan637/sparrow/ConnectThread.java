package com.jeffreychan637.sparrow;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by jeffreychan on 2/9/16.
 *
 * This class is responsible for connecting to the devices that are found via discovery. Once it
 * connects, it passes on the connected socket to the Connection Thread.
 */
public class ConnectThread extends Thread {
    private final UUID mAppID = UUID.fromString("ae0267ba-82fe-4049-85b3-c8c1ad0ac854");
    private final BluetoothSocket mBSocket;
    private final BluetoothAdapter mBluetoothAdaptor;
    private final ProtocolThread mProtocolThread;
    private final Object mWaitOn;

    public ConnectThread(BluetoothDevice device, BluetoothAdapter ba, ProtocolThread pr, Object waitON) {
        BluetoothSocket tmp = null;
        mBluetoothAdaptor = ba;
        mProtocolThread = pr;
        mWaitOn = waitON;

        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            Log.d("connect", "setting up socket" );
            tmp = device.createRfcommSocketToServiceRecord(mAppID);
        } catch (IOException e) { }
        mBSocket = tmp;
    }

    public void run() {
        mBluetoothAdaptor.cancelDiscovery();
        synchronized (mWaitOn) {
            try {
                mBluetoothAdaptor.cancelDiscovery();
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                Log.d("connect", "trying to connect socket" );
                mBSocket.connect();
                Log.d("connect", "connected socket - now want to manage connection");
                mProtocolThread.manageConnection(mBSocket, true);
            } catch (IOException connectException) { //occurs in 12 seconds if connection fails
                try {
                    mBSocket.close();
                    Log.d("ConnectThread", "Connection failed; closing socket");
                } catch (IOException closeException) {
                    Log.d("ConnectThread", "Exception when connecting");
                } finally {
                    mWaitOn.notify();
                }
            }
        }
    }

    /** Will cancel an in-progress connection, and close the socket */
    public void cancel() {
        synchronized (mWaitOn) {
            try {
                mBSocket.close();
            } catch (IOException e) {
            } finally {
                mProtocolThread.setExchangeState(ExchangeState.NOT_EXCHANGING);
                mWaitOn.notify();
            }
        }
    }
}
