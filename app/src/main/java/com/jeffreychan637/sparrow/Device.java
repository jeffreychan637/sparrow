package com.jeffreychan637.sparrow;

import android.bluetooth.BluetoothDevice;

/**
 * Created by jeffreychan on 2/9/16.
 */
public class Device {

    private String name;
    private String address;
    private BluetoothDevice device;

    public Device(String Dname, String Daddress, BluetoothDevice Ddevice) {
        name = Dname;
        address = Daddress;
        device = Ddevice;

    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public BluetoothDevice getDevice() {
        return device;
    };
}
