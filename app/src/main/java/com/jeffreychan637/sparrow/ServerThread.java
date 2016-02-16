package com.jeffreychan637.sparrow;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by jeffreychan on 2/9/16.
 */
public class ServerThread extends Thread {
    private final BluetoothServerSocket bServerSocket;
    private final String appName = "Sparrow";
    private final UUID appID = UUID.fromString("ae0267ba-82fe-4049-85b3-c8c1ad0ac854");
    private final ProtocolThread protocolThread;

    public ServerThread(BluetoothAdapter BA, ProtocolThread PR) {
        protocolThread = PR;
        BluetoothServerSocket tmp = null;
        try {
            tmp = BA.listenUsingRfcommWithServiceRecord(appName, appID);
        } catch (IOException e) {
            Log.d("asd", "uhh...tmp is not logged. WTF");
            this.interrupt();
        }

        bServerSocket = tmp;
        Log.d("asd", "start server thread");
    }

    public void run() {
        BluetoothSocket socket = null;
        while (true) {
            try {
                Log.d("asd", "server thread is now listening");
                socket = bServerSocket.accept();
                Log.d("asd", "accepted");
            } catch (IOException e) {
                Log.d("asd", "exception when accepting");
                break;
            }
            if (interrupted()) break;
            // If a connection was accepted
            if (socket != null) {
                // Do work to manage the connection (in a separate thread)
                protocolThread.manageConnection(socket, false);
                try {
                    bServerSocket.close(); //close socket because we can only have one connection at a time anyways
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
            bServerSocket.close();
        } catch (IOException e) { }
    }
}
