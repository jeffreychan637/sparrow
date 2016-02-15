package com.jeffreychan637.sparrow;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by jeffreychan on 2/9/16.
 */
public class ConnectionThread extends Thread {
    private final BluetoothSocket bSocket;
    private final DataInputStream inStream;
    private final DataOutputStream outStream;
    private final Object waitOn;
    private final ProtocolThread protocolThread;
    boolean isClient;

    public ConnectionThread(BluetoothSocket socket, Object waitON, ProtocolThread pt, boolean isCLient) {
        bSocket = socket;
        DataInputStream tmpIn = null;
        DataOutputStream tmpOut = null;
        waitOn = waitON;
        protocolThread = pt;
        isClient = isCLient;

        try {
            tmpIn = new DataInputStream(socket.getInputStream());
            tmpOut = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("con", "error with tmpIN/OUT");
        }

        inStream = tmpIn;
        outStream = tmpOut;
    }

    public void run() {
        byte[] buffer = new byte[1024];  // buffer store for the stream
        byte[] intBuffer = new byte[4];
        int bytesInMessage = 0;
        int initialBytesRead = 0;
        int bytesRead = 0;
        boolean knowMessageLength = false;

        // Keep listening to the InputStream until an exception occurs
        synchronized (waitOn) {
            while (true) {
                try {
                    Log.d("connection made", "actually trying to read data coming in");
                    if (!knowMessageLength) {
                        knowMessageLength = true;
                        bytesInMessage = inStream.readInt();
                        Log.d("connection", "initial bytes read =  " + bytesInMessage);
                        buffer = new byte[bytesInMessage];
                        Log.d("connection", "created byte array of size: " + bytesInMessage);
                    }
//                    if (knowMessageLength) {
//                        if (bytesInMessage == 0) {
//                            //bytesInMessage = java.nio.ByteBuffer.wrap(intBuffer).getInt();
//                            //bytesInMessage = java.nio.ByteBuffer.wrap(intBuffer).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
//                            // TODO: 2/14/16 verify endianess
//                            //assumes first read will contain at least enough to get int
//                            buffer = new byte[bytesInMessage];
//                            Log.d("connection", "created byte array of size: " + bytesInMessage);
//                        }
                        bytesRead += inStream.read(buffer, bytesRead, buffer.length - bytesRead);
                        Log.d("connection", "total Bytes read = " + bytesRead);
                        Log.d("ds", Arrays.toString(buffer));
                        if (bytesRead == bytesInMessage) {
                            int currentExchangeState = protocolThread.getExchangeState();
                            if (currentExchangeState == ExchangeState.NOT_EXCHANGING && !isClient) {
                                protocolThread.setExchangeState(ExchangeState.GOT_HANDSHAKE);
                                protocolThread.sendHandshake(buffer);
                                Log.d("connection made", "got handshake!");
                                write(protocolThread.getHandshake());
                            } else if (currentExchangeState == ExchangeState.SENT_HANDSHAKE) {
                                if (isClient) {
                                    protocolThread.setExchangeState(ExchangeState.GOT_HANDSHAKE);
                                    Log.d("connection made", "got handshake!");
                                    protocolThread.sendHandshake(buffer);
                                    // TODO: 2/13/16 read in first integer to figure out how many bytes i have to read - then send
                                } else {
                                    // TODO: 2/13/16 read a stream; have to listen multiple times can't assume just listening once
                                    Log.d("connection made", "got data!");
                                    protocolThread.setExchangeState(ExchangeState.GOT_DATA);
                                    protocolThread.sendData(buffer);
                                }
                                write(protocolThread.getData());
                            } else if (currentExchangeState == ExchangeState.SENT_DATA && isClient) {
                                protocolThread.setExchangeState(ExchangeState.GOT_DATA);
                                Log.d("connection made", "got data!");
                                protocolThread.sendData(buffer);
                                Log.d("connection thread", "DONE, CLOSING CONNECTION.");
                                protocolThread.setExchangeState(ExchangeState.NOT_EXCHANGING);
                                protocolThread.stopEverything();
                                waitOn.notify();
                                break;
                                //ASSUMES WE WILL ONLY EVER EXCHANGE DATA ONCE
                            } else {
                                Log.d("got data", "got data...but no exchange state");
                                Log.d("got data", "CURRENT EXCHANGE STATE " + protocolThread.getExchangeState());
                            }
                            bytesInMessage = 0;
                            initialBytesRead = 0;
                            bytesRead = 0;
                            knowMessageLength = false;
                        }
//                    } else {
//                        Log.d("connection made", "running but not getting data");
//                    }
                } catch (IOException e) {
                    Log.d("connection made", "exception when listening - " + e.toString());
                    protocolThread.setExchangeState(ExchangeState.NOT_EXCHANGING);
                    if (isClient) {
                        waitOn.notify();
                    }
                    break;
                }
            }
        }
    }

    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] bytes) {
        try {
            int currentState = protocolThread.getExchangeState();
            Log.d("ds", Arrays.toString(bytes));
            Log.d("ds", String.valueOf(outStream.size()));
            if (isClient) {
                Log.d("ds", "Is client");
            } else {
                Log.d("ds", "Is NOT client");
            }
            outStream.writeInt(bytes.length);
            Log.d("write", "sent byte length of " + bytes.length);
            outStream.write(bytes);
            if (currentState == ExchangeState.NOT_EXCHANGING && isClient) {
                protocolThread.setExchangeState(ExchangeState.SENT_HANDSHAKE);
            } else if (currentState == ExchangeState.GOT_HANDSHAKE) {
                if (isClient) {
                    protocolThread.setExchangeState(ExchangeState.SENT_DATA);
                } else {
                    protocolThread.setExchangeState(ExchangeState.SENT_HANDSHAKE);
                }
                Log.d("asd", "isclient " + isClient);
            } else if (currentState == ExchangeState.GOT_DATA && !isClient) {
                protocolThread.setExchangeState(ExchangeState.NOT_EXCHANGING);
                protocolThread.stopEverything(); //ASSUMES WE WILL ONLY EVER EXCHANGE DATA ONCE
                Log.d("connection thread", "DONE, CLOSING CONNECTION.");
            }
        } catch (IOException e) {
            protocolThread.setExchangeState(ExchangeState.NOT_EXCHANGING);
            Log.d("connection made", "exception when sending data" + e.toString());
            if (isClient) {
                waitOn.notify();
            }
        }
    }

    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        synchronized (waitOn) {
            try {
                bSocket.close();
            } catch (IOException e) {

            } finally {
                protocolThread.setExchangeState(ExchangeState.NOT_EXCHANGING);
                if (isClient) {
                    waitOn.notify();
                }
            }
        }
    }
}
