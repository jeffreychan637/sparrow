package com.jeffreychan637.sparrow;

import android.app.Fragment;

/**
 * Created by jeffreychan on 2/16/16.
 *
 * This class is a wrapper around methods implemented by the main activity that are used by the
 * Bluetooth fragments to get data from the rest of the app to send to other phones as well as to
 * send received data from other phones to the rest of the app.
 */
public class DataHandler {
    private Fragment mfragment;

    public DataHandler(Fragment fragment) {
        mfragment = fragment;
    };

    public byte[] getHandshake() {
        DataSender ds = (DataSender) mfragment.getActivity();
        return ds.sendHandshakeOut();
    };

    public void sendHandshake(byte[] handshake) {
        DataSender ds = (DataSender) mfragment.getActivity();
        ds.processReceivedHandshake(handshake);
    };

    public byte[] getData() {
        DataSender ds = (DataSender) mfragment.getActivity();
        return ds.sendDataOut();
    };

    public void sendData(byte[] data) {
        DataSender ds = (DataSender) mfragment.getActivity();
        ds.processReceivedData(data);
    };
}
