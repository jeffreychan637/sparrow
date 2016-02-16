package com.jeffreychan637.sparrow.BLE;

import java.util.UUID;

/**
 * Created by jeffreychan on 2/16/16.
 *
 * These are the UUIDs used by the central and peripheral devices to communicate with one another.
 * More specifically, these are special identifiers to the service and characteristics provided
 * by the peripheral that both use to communicate with one another with.
 */
public class BleUuids {
    public static final UUID SERVICE_UUID = UUID.fromString("bb5b2ae2-e1d8-4ac5-a9bb-4ee5d469aa17");
    public static final UUID HANDSHAKE_CHAR_UUID = UUID.fromString("422c0b78-7c65-48ee-91b8-2a91b02a938d");
    public static final UUID DATA_CHAR_UUID = UUID.fromString("9fdc53aa-56e5-46fc-89a0-637d7cf4240d");
}
