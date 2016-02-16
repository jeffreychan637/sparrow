package com.jeffreychan637.sparrow;

/**
 * Created by jeffreychan on 2/16/16.
 *
 * This is an interface that is extended by the main activity. It is used to ensure communication
 * between the main activity (and the rest of the app) and the Bluetooth fragments is possible.
 * The bluetooth fragments call on these functions in the main activity to get data that needs
 * to be sent out as well as send in data that has been received from other devices.
 */
public interface DataSender {
    byte[] sendHandshakeOut();

    void processReceivedHandshake(byte[] handshake);

    byte[] sendDataOut();

    void processReceivedData(byte[] data);
};