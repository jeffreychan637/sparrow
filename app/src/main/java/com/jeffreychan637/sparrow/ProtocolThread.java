package com.jeffreychan637.sparrow;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by jeffreychan on 2/11/16.
 */
public class ProtocolThread extends Thread {
    private ServerThread serverThread = null;
    private ConnectThread connectThread = null;
    private ConnectionThread connectionThread = null;
    private BluetoothFragment.DataHandler dataHandler;
    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<Device> devices;
    private int exchangeState;
    private boolean serverMode = true;
    //Object used to wait and synchronize on
    private final Object waitOn = new Object();

    /* How often this protocol should be run*/
    public static int repeatTime = 120000;

    //TODO set a random interval before starting protocol to offset time protocol is run on different devices

    public ProtocolThread(BluetoothAdapter BA, ArrayList<Device> DArray,
                          BluetoothFragment.DataHandler dHandler) {
        bluetoothAdapter = BA;
        devices = DArray;
        dataHandler = dHandler;
        exchangeState = ExchangeState.NOT_EXCHANGING;
    };

    public void run() {
        Log.d("run", "running Protocol");
        Log.d("run", " devices size " + devices.size());
        stopEverything();
        for (int i = 0; i < devices.size(); i++) {
            serverMode = false;
            initiateConnection(devices.get(i).getDevice(), bluetoothAdapter);
        }
        startServer(bluetoothAdapter);

    };

    public void restart() {
        if (serverThread != null) {
            serverThread.interrupt();
        }
        run();
    }

    private void initiateConnection(BluetoothDevice device, BluetoothAdapter BA) {
            stopEverything();
            connectThread = new ConnectThread(device, BA, this, waitOn);
            synchronized (waitOn) {
                connectThread.start();
                Log.d("protocol", "hello initiate connection " + device.getName());
                try {
                    waitOn.wait();
                    stopEverything(); //If this is returned, means that communication with one device is done so kill everything
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
    };

    public void manageConnection(BluetoothSocket socket, boolean isClient) {
        Log.d("proto", "start managing connection");
//        stopEverything(); //bad - setting connectthread to null...
        connectionThread = new ConnectionThread(socket, waitOn, this, isClient);
        Log.d("proto", "start connection thread");
        connectionThread.start();
        if (isClient) {
            byte[] handshake = dataHandler.getHandshake();
            Log.d("proto", "sent handshake " + handshake.toString());
            connectionThread.write(handshake);
        }
    };

    private void startServer(BluetoothAdapter BA) {
        serverMode = true;
        stopEverything();
        Log.d("sad", "starting server");
        serverThread = new ServerThread(BA, this);
        serverThread.start();
    }

    public byte[] getHandshake() { return dataHandler.getHandshake(); }

    public void sendHandshake(byte[] data) {
        dataHandler.sendHandshake(data);
    }

    public void sendData(byte[] data) {
        dataHandler.sendData(data);
    }

    public byte[] getData() { return dataHandler.getData(); }

    public int getExchangeState() {
        return exchangeState;
    }

    public boolean getServerMode() {
        return serverMode;
    }

    public void setExchangeState(int newState) {
        exchangeState = newState;
    }

    public synchronized void stopEverything() {
        Log.d("sad", "KILLING EVERYTHING.");
        stopConnect();
        stopConnection();
        stopServer();
    }

    private void stopConnect() {
        if (connectThread != null) {
            connectThread.cancel();
            connectThread.interrupt();
        }
        connectThread = null;
    }

    private void stopConnection() {
        if (connectionThread != null) {
            connectionThread.cancel();
            connectionThread.interrupt();
        }
        connectionThread = null;
    }

    private void stopServer() {
        if (serverThread != null) {
            serverThread.cancel();
            serverThread.interrupt();
        }
        serverThread = null;
    }

}
