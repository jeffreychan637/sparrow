package com.jeffreychan637.sparrow;

import android.bluetooth.BluetoothDevice;

/**
 * Created by jeffreychan on 2/9/16.
 *
 * This class is used to represent devices.
 */
public class Device {

    private String mName;
    private BluetoothDevice mDevice;

    public Device(String name, BluetoothDevice device) {
        mName = name;
        mDevice = device;

    }

    public String getName() { return mName; }

    public BluetoothDevice getDevice() {
        return mDevice;
    };
}
