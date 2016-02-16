package com.jeffreychan637.sparrow.BLE;

import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.util.Log;

import com.jeffreychan637.sparrow.DataHandler;
import com.jeffreychan637.sparrow.ExchangeState;

import java.util.ArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by jeffreychan on 2/15/16.
 *
 * This class represents the central device in BLE mesh network. It scans for peripheral devices
 * and tries to connect with and query them for data. The way it sends data to these peripheral
 * devices is by writing to their characteristics provided in their services.
 */
public class CentralDevice {
    private final int SCAN_PERIOD = 5;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetootherLeScanner;
    private Fragment mfragement;
    private ArrayList<BluetoothDevice> mdevices = new ArrayList<BluetoothDevice>();
    private boolean mScanning = false;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattService mService;
    private BluetoothGattCharacteristic mHandshakeChar;
    private BluetoothGattCharacteristic mDataChar;
    private int mExchangeState = ExchangeState.NOT_EXCHANGING;
    private DataHandler mDataHandler;
    private Object mWaitOn;

    CentralDevice(BluetoothAdapter BA, Fragment fragment) {
        mBluetoothAdapter = BA;
        mBluetootherLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        mfragement = fragment;
        mDataHandler = new DataHandler(fragment);
        scanForDevices();
    }

    private final ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            mdevices.add(result.getDevice());
            Log.d("cen", "discovered device " + result.getDevice().getName());
        }
    };


    private void scanForDevices() {
        ScheduledThreadPoolExecutor stopScanExecutor =  new ScheduledThreadPoolExecutor(1);
        stopScanExecutor.schedule(new Runnable() {
            @Override
            public void run() {
                mScanning = false;
                mBluetootherLeScanner.stopScan(mScanCallback);
                Log.d("cen", "ending scan; device found: " + mdevices.size());
                for (int i = 0; i < mdevices.size(); i++) {
                    connectToDevice(mdevices.get(i));
                }
                close();
            }
        }, SCAN_PERIOD, TimeUnit.SECONDS);

        mScanning = true;
        mBluetootherLeScanner.startScan(mScanCallback);
    };

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            mService = mBluetoothGatt.getService(BleUuids.SERVICE_UUID);
            Log.d("cen", "2sending handshake from client side");
            mHandshakeChar = mService.getCharacteristic(BleUuids.HANDSHAKE_CHAR_UUID);
            Log.d("cen", "3sending handshake from client side");
            mDataChar = mService.getCharacteristic(BleUuids.DATA_CHAR_UUID);
            getHandshake();
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            synchronized (mWaitOn) {
                super.onCharacteristicRead(gatt, characteristic, status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d("cen", "read succeeded");
                    if (mExchangeState == ExchangeState.SENT_HANDSHAKE) {
                        mExchangeState = ExchangeState.GOT_HANDSHAKE;
                        Log.d("cen", "got handshake!");
                        mDataHandler.sendHandshake(characteristic.getValue());
                        sendData();
                    } else if (mExchangeState == ExchangeState.SENT_DATA) {
                        mExchangeState = ExchangeState.GOT_DATA;
                        Log.d("cen", "got data!");
                        mDataHandler.sendData(characteristic.getValue());
                        mBluetoothGatt.disconnect();
                        mExchangeState = ExchangeState.NOT_EXCHANGING;
                        mWaitOn.notify();
                    }
                } else {
                    Log.d("cen", "read failed " + status);
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("cen", "write succeeded");
            } else {
                Log.d("cen", "write failed " + status);
            }
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("cen", "connected on client side");
            } else {
                Log.d("cen", "disconnected on client side");
            }
        }
    };

    private void connectToDevice(BluetoothDevice device) {
        Log.d("cen", "connecting to device " + device.getName());
        mBluetoothGatt = device.connectGatt(mfragement.getActivity(), false, mGattCallback);
        Log.d("cen", "1sending handshake from client side");
//        mService = mBluetoothGatt.getService(BleUuids.SERVICE_UUID);
//        Log.d("cen", "2sending handshake from client side");
//        Log.d("cen", "2" + mService);
        mBluetoothGatt.discoverServices();
//        mHandshakeChar = mService.getCharacteristic(BleUuids.HANDSHAKE_CHAR_UUID);
//        Log.d("cen", "3sending handshake from client side");
//        mDataChar = mService.getCharacteristic(BleUuids.DATA_CHAR_UUID);
//        getHandshake();
//        Log.d("cen", "sending handshake from client side");
        try {
            mWaitOn.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void getHandshake() {
        mBluetoothGatt.readCharacteristic(mHandshakeChar);
        mExchangeState = ExchangeState.SENT_HANDSHAKE;
        Log.d("cen", "trying to get handshake");
    }

    private void getData() {
        mBluetoothGatt.readCharacteristic(mDataChar);
        mExchangeState = ExchangeState.SENT_DATA;
    }

    private void sendHandshake() {
        mHandshakeChar.setValue(mDataHandler.getHandshake());
        mBluetoothGatt.writeCharacteristic(mHandshakeChar);
    }

    private void sendData() {
        mDataChar.setValue(mDataHandler.getData());
        mBluetoothGatt.writeCharacteristic(mDataChar);
    }

    public void close() {
        Log.d("cen", "closing central device");
        if (mScanning) {
            mBluetootherLeScanner.stopScan(mScanCallback);
        }
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }
}
