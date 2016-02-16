package com.jeffreychan637.sparrow;

/**
 * Created by jeffreychan on 2/13/16.
 *
 * This class is used to store a number of constants representing different states in the sharing
 * protocol.
 */
public class ExchangeState {
    public static final int NOT_EXCHANGING = 0;
    public static final int SENT_HANDSHAKE = 1;
    public static final int GOT_HANDSHAKE = 2;
    public static final int SENT_DATA = 3;
    public static final int GOT_DATA = 4;
}
