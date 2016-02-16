package com.jeffreychan637.sparrow.BLE;

import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;

import com.jeffreychan637.sparrow.DataHandler;
import com.jeffreychan637.sparrow.ExchangeState;

/**
 * Created by jeffreychan on 2/15/16.
 *
 * This class represents the Peripheral device in the BLE mesh network. It waits for central
 * devices to connect to it after making its presence known through advertising. Once connected,
 * it communicates with central devices through the characteristic fields in the services that it
 * provides. Central devices read that those characteristics to get data from te peripheral devices
 * and write to them to share data with the peripheral.
 */
public class PeripheralDevice {
    private Fragment mFragment;
    private BluetoothManager mBluetoothManager;
    private BluetoothGattCharacteristic mHandshakeChar;
    private BluetoothGattCharacteristic mDataChar;
    private BluetoothGattService mService;
    private BluetoothGattServer mServer;
    private BluetoothLeAdvertiser mAdvertiser;
    private AdvertiseData mData;
    private AdvertiseSettings mSettings;
    private boolean mAdvertising;
    private int mExchangeState = ExchangeState.NOT_EXCHANGING;
    private DataHandler mDataHandler;

    PeripheralDevice(BluetoothAdapter BA, Fragment fragment) {
        mFragment = fragment;
        mDataHandler = new DataHandler(fragment);
        mBluetoothManager = (BluetoothManager) fragment.getActivity()
                                                       .getSystemService(Context.BLUETOOTH_SERVICE);
        mAdvertiser = BA.getBluetoothLeAdvertiser();
        buildCharacteristics();
        buildServices();
        buildAdvertiser();
        startAdvertising();
    }

    private void buildCharacteristics() {
        mHandshakeChar = new BluetoothGattCharacteristic(BleUuids.HANDSHAKE_CHAR_UUID,
                             BluetoothGattCharacteristic.PROPERTY_READ |
                             BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                             BluetoothGattCharacteristic.PERMISSION_READ |
                             BluetoothGattCharacteristic.PERMISSION_WRITE);
        mDataChar = new BluetoothGattCharacteristic(BleUuids.DATA_CHAR_UUID,
                        BluetoothGattCharacteristic.PROPERTY_READ |
                        BluetoothGattCharacteristic.PROPERTY_WRITE,
                        BluetoothGattCharacteristic.PERMISSION_READ |
                        BluetoothGattCharacteristic.PERMISSION_WRITE);
    }

    private void buildServices() {
        mService = new BluetoothGattService(BleUuids.SERVICE_UUID,
                       BluetoothGattService.SERVICE_TYPE_PRIMARY);
        mService.addCharacteristic(mHandshakeChar);
        mService.addCharacteristic(mDataChar);
    }

    private void buildAdvertiser() {
        AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
        dataBuilder.addServiceUuid(ParcelUuid.fromString(BleUuids.SERVICE_UUID.toString()));
        mData = dataBuilder.build();

        AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder();
        //settingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);
        //settingsBuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
        mSettings = settingsBuilder.build();
    }

    private final AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings advertiseSettings) {
            String successMsg = "Advertisement command attempt successful";
        }

        @Override
        public void onStartFailure(int i) {
            String failMsg = "Advertisement command attempt failed: " + i;
        }
    };

    private final BluetoothGattServerCallback mServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("cen", "connected on server side");
            } else {
                Log.d("cen", "disconnected on server side");
            }
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            if (mExchangeState == ExchangeState.GOT_HANDSHAKE) {
                characteristic.setValue(mDataHandler.getHandshake());
                mExchangeState = ExchangeState.SENT_HANDSHAKE;
            } else if (mExchangeState == ExchangeState.GOT_DATA) {
                characteristic.setValue(mDataHandler.getData());
                mExchangeState = ExchangeState.NOT_EXCHANGING;
            }
            mServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
            if (mExchangeState == ExchangeState.NOT_EXCHANGING) {
                mDataHandler.sendHandshake(characteristic.getValue());
                mExchangeState = ExchangeState.GOT_HANDSHAKE;
            } else if (mExchangeState == ExchangeState.SENT_HANDSHAKE) {
                mDataHandler.sendData(characteristic.getValue());
                mExchangeState = ExchangeState.GOT_DATA;
            }
            mServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());
        }
        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            Log.d("peri", "service added");
        }
    };


    private void startServer() {
        mServer = mBluetoothManager.openGattServer(mFragment.getActivity(), mServerCallback);
        mServer.addService(mService);
    }

    public void startAdvertising() {
        startServer();
        mAdvertiser.startAdvertising(mSettings, mData, mAdvertiseCallback);
        mAdvertising = true;
    }

    public void stopAdvertising() {
        if (mAdvertising) {
            mAdvertiser.stopAdvertising(mAdvertiseCallback);
            mServer.clearServices();
            mServer.close();
            mAdvertising = false;
        }
    }


}
