package com.jeffreychan637.sparrow.BLE;

import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by jeffreychan on 2/15/16.
 */
public class BLEFragment extends Fragment {
    private final String TAG = "BLEFragment";
    private BluetoothAdapter BA;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BA = BluetoothAdapter.getDefaultAdapter();

        if (!BA.isEnabled()) {
            //request permission to use Bluetooth
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, 1);
        } else {
            Log.d(TAG, "Bluetooth already enabled");
        }
    }


}
