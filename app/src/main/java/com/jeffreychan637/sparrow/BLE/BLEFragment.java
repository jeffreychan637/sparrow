package com.jeffreychan637.sparrow.BLE;

import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

/**
 * Created by jeffreychan on 2/15/16.
 *
 * This class starts the BluetoothLE message passing protocol.
 */
public class BLEFragment extends Fragment {
    private final String TAG = "BleFragment";
    private BluetoothAdapter mBluetoothAdapter;
    private CentralDevice mCentralDevice;
    private PeripheralDevice mPeripheralDevice;

    @Override
    public void onCreate(Bundle savedInstanceState) {
            //ASSUMING THAT BLE IS A SUPPORTED BY DEVICE IF THIS CODE RUNS
            super.onCreate(savedInstanceState);

            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if (!mBluetoothAdapter.isEnabled()) {
                //request permission to use Bluetooth
                Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBT, 1);
            } else {
            }

            if (Build.MANUFACTURER.equals("LGE")) {
                mCentralDevice = new CentralDevice(mBluetoothAdapter, this);
            } else {
                mPeripheralDevice = new PeripheralDevice(mBluetoothAdapter, this);
            }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCentralDevice != null) {
            mCentralDevice.close();
        }
        if (mPeripheralDevice != null) {
            mPeripheralDevice.stopAdvertising();
        }
    }






}
