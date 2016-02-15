package com.jeffreychan637.sparrow;

/**
 * Created by jeffreychan on 2/13/16.
 */
public class ExchangeState {
//    public static final int LISTENING = 0;
//    public static final int CONNECTING = 1;
//    public static final int CONNECTED = 2;
    public static final int NOT_EXCHANGING = 0;
    public static final int SENT_HANDSHAKE = 1;
    public static final int GOT_HANDSHAKE = 2;
    public static final int SENT_DATA = 3;
    public static final int GOT_DATA = 4;
}
