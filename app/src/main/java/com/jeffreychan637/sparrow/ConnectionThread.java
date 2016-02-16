package com.jeffreychan637.sparrow;

import android.bluetooth.BluetoothSocket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by jeffreychan on 2/9/16.
 *
 * This thread class is responsible for processing all connections. It is used to listen for
 * data (i.e. handshakes and tweet exchanges) from other devices as well as send data out to
 * these devices. It knows what to do with the data received and what data to send out by
 * keeping track of the state of data exchange process.
 */
public class ConnectionThread extends Thread {
    private final BluetoothSocket mBSocket;
    private final DataInputStream mInStream;
    private final DataOutputStream mOutStream;
    private final Object mWaitOn;
    private final ProtocolThread mProtocolThread;
    boolean mIsClient;

    public ConnectionThread(BluetoothSocket socket, Object waitOn, ProtocolThread pt, boolean isClient) {
        mBSocket = socket;
        DataInputStream tmpIn = null;
        DataOutputStream tmpOut = null;
        mWaitOn = waitOn;
        mProtocolThread = pt;
        mIsClient = isClient;

        try {
            tmpIn = new DataInputStream(socket.getInputStream());
            tmpOut = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        mInStream = tmpIn;
        mOutStream = tmpOut;
    }

    /* This function basically runs the listener for this thread. Until the input stream
     * is closed, this function will listen continuously for data from the connected device.
     */
    public void run() {
        byte[] buffer = new byte[1024];
        int bytesInMessage = 0;
        int bytesRead = 0;
        boolean knowMessageLength = false;

        synchronized (mWaitOn) {
            while (true) {
                try {
                    if (!knowMessageLength) {
                        knowMessageLength = true;
                        bytesInMessage = mInStream.readInt();
                        buffer = new byte[bytesInMessage];
                    }
                    bytesRead += mInStream.read(buffer, bytesRead, buffer.length - bytesRead);
                    if (bytesRead == bytesInMessage) {
                        int currentExchangeState = mProtocolThread.getExchangeState();
                        if (currentExchangeState == ExchangeState.NOT_EXCHANGING && !mIsClient) {
                            mProtocolThread.setExchangeState(ExchangeState.GOT_HANDSHAKE);
                            mProtocolThread.sendHandshake(buffer);
                            write(mProtocolThread.getHandshake());
                        } else if (currentExchangeState == ExchangeState.SENT_HANDSHAKE) {
                            if (mIsClient) {
                                mProtocolThread.setExchangeState(ExchangeState.GOT_HANDSHAKE);
                                mProtocolThread.sendHandshake(buffer);
                            } else {
                                mProtocolThread.setExchangeState(ExchangeState.GOT_DATA);
                                mProtocolThread.sendData(buffer);
                            }
                            write(mProtocolThread.getData());
                        } else if (currentExchangeState == ExchangeState.SENT_DATA && mIsClient) {
                            mProtocolThread.setExchangeState(ExchangeState.GOT_DATA);
                            mProtocolThread.sendData(buffer);
                            mProtocolThread.setExchangeState(ExchangeState.NOT_EXCHANGING);
                            mProtocolThread.stopEverything();
                            mWaitOn.notify();
                            break;
                            //ASSUMES WE WILL ONLY EVER EXCHANGE DATA ONCE
                        }
                        bytesInMessage = 0;
                        bytesRead = 0;
                        knowMessageLength = false;
                    }
                } catch (IOException e) {
                    mProtocolThread.setExchangeState(ExchangeState.NOT_EXCHANGING);
                    if (mIsClient) {
                        mWaitOn.notify();
                    }
                    break;
                }
            }
        }
    }

    /* This function is used to send data to remote devices. It prepends the amount of bytes
     * that is being sent to all messages. */
    public void write(byte[] bytes) {
        try {
            int currentState = mProtocolThread.getExchangeState();
            mOutStream.writeInt(bytes.length);
            mOutStream.write(bytes);
            if (currentState == ExchangeState.NOT_EXCHANGING && mIsClient) {
                mProtocolThread.setExchangeState(ExchangeState.SENT_HANDSHAKE);
            } else if (currentState == ExchangeState.GOT_HANDSHAKE) {
                if (mIsClient) {
                    mProtocolThread.setExchangeState(ExchangeState.SENT_DATA);
                } else {
                    mProtocolThread.setExchangeState(ExchangeState.SENT_HANDSHAKE);
                }
            } else if (currentState == ExchangeState.GOT_DATA && !mIsClient) {
                mProtocolThread.setExchangeState(ExchangeState.NOT_EXCHANGING);
                mProtocolThread.stopEverything(); //ASSUMES WE WILL ONLY EVER EXCHANGE DATA ONCE
                mProtocolThread.startServer();
            }
        } catch (IOException e) {
            mProtocolThread.setExchangeState(ExchangeState.NOT_EXCHANGING);
            if (mIsClient) {
                mWaitOn.notify();
            }
        }
    }

    /* Call this to shutdown the connection */
    public void cancel() {
        synchronized (mWaitOn) {
            try {
                mBSocket.close();
            } catch (IOException e) {

            } finally {
                mProtocolThread.setExchangeState(ExchangeState.NOT_EXCHANGING);
                if (mIsClient) {
                    mWaitOn.notify();
                }
            }
        }
    }
}
