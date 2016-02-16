package com.jeffreychan637.sparrow;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by jeffreychan on 2/11/16.
 *
 * This thread is used to manage the other threads used in making the message passing protocol
 * run. Given a list of devices, it starts up threads to connect to each one and share messages.
 * Once it goes through the list, it starts up a server thread and just listens for the rest of the
 * time for incoming connections.
 *
 * At any one time, there should only be one of the three threads (connect, connection, and server)
 * running since there can only be one Bluetooth connection at a time. The use of wait() and
 * synchronize() is to make sure that this condition is followed.
 */
public class ProtocolThread extends Thread {
    private ServerThread mServerThread = null;
    private ConnectThread mConnectThread = null;
    private ConnectionThread mConnectionThread = null;
    private DataHandler mDataHandler;
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayList<Device> mDevices;
    private int mExchangeState;
    private boolean mServerMode = true;
    private final Object mWaitOn = new Object();


    public ProtocolThread(BluetoothAdapter BA, ArrayList<Device> DArray, DataHandler dHandler) {
        mBluetoothAdapter = BA;
        mDevices = DArray;
        mDataHandler = dHandler;
        mExchangeState = ExchangeState.NOT_EXCHANGING;
    };

    /* This function runs through the list of devices and initiates connections to them. */
    public void run() {
        Log.d("run", "running Protocol");
        Log.d("run", " mDevices size " + mDevices.size());
        stopEverything();
        for (int i = 0; i < mDevices.size(); i++) {
            mServerMode = false;
            initiateConnection(mDevices.get(i).getDevice(), mBluetoothAdapter);
        }
        startServer();

    }

    public void restart() {
        if (mServerThread != null) {
            mServerThread.interrupt();
        }
        run();
    }

    private void initiateConnection(BluetoothDevice device, BluetoothAdapter BA) {
            stopEverything();
            mConnectThread = new ConnectThread(device, BA, this, mWaitOn);
            synchronized (mWaitOn) {
                mConnectThread.start();
                Log.d("protocol", "hello initiate connection " + device.getName());
                try {
                    mWaitOn.wait();
                    stopEverything(); //If this is returned, means that communication with one device is done so kill everything
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
    }

    public void manageConnection(BluetoothSocket socket, boolean isClient) {
        Log.d("proto", "start managing connection");
        mConnectionThread = new ConnectionThread(socket, mWaitOn, this, isClient);
        Log.d("proto", "start connection thread");
        mConnectionThread.start();
        if (isClient) {
            byte[] handshake = mDataHandler.getHandshake();
            Log.d("proto", "sent handshake " + handshake.toString());
            mConnectionThread.write(handshake);
        }
    }

    public void startServer() {
        if (mServerThread == null || !mServerThread.isInterrupted()) {
            mServerMode = true;
            stopEverything();
            Log.d("sad", "starting server");
            mServerThread = new ServerThread(mBluetoothAdapter, this);
            mServerThread.start();
        }
    }

    /* The following methods wrap around the methods in the data handler so code
     * in the other thread classes are cleaner.
     */
    public byte[] getHandshake() { return mDataHandler.getHandshake(); }

    public void sendHandshake(byte[] data) {
        mDataHandler.sendHandshake(data);
    }

    public void sendData(byte[] data) {
        mDataHandler.sendData(data);
    }

    public byte[] getData() { return mDataHandler.getData(); }


    public int getExchangeState() {
        return mExchangeState;
    }

    public void setExchangeState(int newState) {
        mExchangeState = newState;
    }

    public boolean getServerMode() {
        return mServerMode;
    }


    /* Shuts down all the threads. */
    public synchronized void stopEverything() {
        Log.d("sad", "KILLING EVERYTHING.");
        stopConnect();
        stopConnection();
        stopServer();
    }

    private void stopConnect() {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread.interrupt();
        }
        mConnectThread = null;
    }

    private void stopConnection() {
        if (mConnectionThread != null) {
            mConnectionThread.cancel();
            mConnectionThread.interrupt();
        }
        mConnectionThread = null;
    }

    private void stopServer() {
        if (mServerThread != null) {
            mServerThread.cancel();
            mServerThread.interrupt();
        }
        mServerThread = null;
    }

}
