package com.jeffreychan637.sparrow;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by jeffreychan on 2/9/16.
 *
 * This class is used to as a thread that acts as a server listening for other Bluetooth devices
 * that want to connect to this device. Once a connection is initiated, the connected socket
 * is handed to the Connection thread.
 *
 */
public class ServerThread extends Thread {
    private final BluetoothServerSocket mBServerSocket;
    private final String mAppName = "Sparrow";
    private final UUID mAppID = UUID.fromString("ae0267ba-82fe-4049-85b3-c8c1ad0ac854");
    private final ProtocolThread mProtocolThread;

    public ServerThread(BluetoothAdapter BA, ProtocolThread PR) {
        mProtocolThread = PR;
        BluetoothServerSocket tmp = null;
        try {
            tmp = BA.listenUsingRfcommWithServiceRecord(mAppName, mAppID);
        } catch (IOException e) {
            this.interrupt();
        }

        mBServerSocket = tmp;
    }

    public void run() {
        BluetoothSocket socket = null;
        while (true) {
            try {
                socket = mBServerSocket.accept();
            } catch (IOException e) {
                break;
            }
            if (interrupted()) break;
            // If a connection was accepted
            if (socket != null) {
                // Do work to manage the connection (in a separate thread)
                mProtocolThread.manageConnection(socket, false);
                try {
                    mBServerSocket.close(); //close socket because we can only have one connection at a time anyways
                } catch (IOException e) {
                    break;
                }
                break;
            }
        }
    }

    /** Will cancel the listening socket, and cause the thread to finish */
    public void cancel() {
        try {
            mBServerSocket.close();
        } catch (IOException e) { }
    }
}
