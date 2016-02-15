package com.jeffreychan637.sparrow;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Arrays;
import java.util.Random;

public class Sparrow extends AppCompatActivity implements BluetoothFragment.DataSender {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sparrow);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        BluetoothFragment BT = new BluetoothFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.add(BT, "bluetoothFragment");
        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sparrow, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public byte[] sendHandshakeOut() {
        byte[] byteArray = new byte[20];
        new Random().nextBytes(byteArray);
        Log.d("da", "generated handshake " + Arrays.toString(byteArray));
        return byteArray;
    };

    public void processReceivedHandshake(byte[] data) {
        Log.d("Received handshake", "Received handshake " + Arrays.toString(data));
    }

    public byte[] sendDataOut() {
        byte[] byteArray = new byte[50];
        new Random().nextBytes(byteArray);
        Log.d("da", "generated data " + Arrays.toString(byteArray));
        return byteArray;
    };

    public void processReceivedData(byte[] data) {
        Log.d("Received data", "Received data " + Arrays.toString(data));
    }
}
