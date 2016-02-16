package com.jeffreychan637.sparrow;

import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * Created by jeffreychan on 2/8/16.
 *
 * This class starts the Bluetooth message passing protocol. It first requests that Bluetooth
 * be turned on, if it isn't on already. Then in a random interval between 30-90 seconds, it runs
 * the message passing protocol over Bluetooth which consists of discovering nearby devices,
 * connecting to each of those devices and sharing messages, and then spending the rest of the time
 * as a server that other devices can connect to.
 */

public class BluetoothFragment extends Fragment {
    private BluetoothAdapter BA;
    private Set<BluetoothDevice> pairedDevices;
    private static int REQUEST_BLUETOOTH = 1;
    private static int REQUEST_DISCOVERABLE = 2;
    private ProtocolThread protocolThread = null;
    private ScheduledThreadPoolExecutor protocolExecutor;
    private Random random = new Random();
    private Fragment self = this;

    public static boolean discoveryCompleted = false;

    private static ArrayList<String> seenDevices = new ArrayList<String>();
    private static ArrayList<Device> devicesList = new ArrayList<Device>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BA = BluetoothAdapter.getDefaultAdapter();

        if (BA != null) { //True if device has bluetooth

            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            getActivity().registerReceiver(bReceiver, filter);

            if (!BA.isEnabled()) {
                //request permission to use Bluetooth
                Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBT, REQUEST_BLUETOOTH);
            } else {
                startProtocol();
            }
        }
    }

    /* This receiver is used to listen to broadcasts from the system to determine such things
     * as when discovery of other devices is complete or when a device is found via discovery.
     */
    private final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) { //Device found via discovery

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                if (deviceName != null && !seenDevices.contains(device.getName())) {
                    seenDevices.add(deviceName);
                    devicesList.add(new Device(deviceName, device));
                }

            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) { //Bluetooth turned on
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                                                     BluetoothAdapter.ERROR);
                if (BluetoothAdapter.STATE_ON == state) {
                    //start discovery
                    //not sure if that's a good idea
                    startProtocol();
                }

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) { //Discovery done
                discoveryCompleted = true;
                //should connect to all devices in order
                if (protocolThread == null) {
                    protocolThread = new ProtocolThread(BA, devicesList, new DataHandler(self));
                    protocolThread.start();
                } else if (protocolThread.getServerMode()) {
                    protocolThread.restart();
                }
            }
        }
    };

    private void runDiscovery() {
        protocolThread = null;
        devicesList = new ArrayList<Device>();
        seenDevices = new ArrayList<String>();
        makeSelfDiscoverable();
        BA.startDiscovery();
    }

    private void makeSelfDiscoverable() {
        if (BA.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            startActivityForResult(discoverableIntent, REQUEST_DISCOVERABLE); //make oneself discoverable
        }
    };

    /* This function starts the bluetooth messaging protocol after a random amount of seconds
    * between 0 to 30 and then repeats that protocol after a random but set amount of seconds
    * (between 30 - 90 seconds). */
    private void startProtocol() {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                runDiscovery();
            }
        };
        protocolExecutor =  new ScheduledThreadPoolExecutor(1);
        int interval = randomInt(30, 90);
        int startDelay = randomInt(0, 20);
        protocolExecutor.scheduleWithFixedDelay(timerTask, startDelay, interval, TimeUnit.SECONDS);

    }

    /* Generates random numbers between min and max. */
    private int randomInt(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_DISCOVERABLE) {
            if (resultCode == 120) {
                //We are discoverable for the next 120 seconds
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(bReceiver);
        protocolExecutor.shutdownNow();
        if (protocolThread != null) {
            protocolThread.stopEverything();
        }
    }
}
