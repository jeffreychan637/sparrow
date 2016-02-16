package com.tmochida.sparrow.adapters;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.tmochida.sparrow.R;

import java.util.ArrayList;

public class BtDeviceAdapter extends ArrayAdapter<BluetoothDevice> {
    public BtDeviceAdapter(Context context, int viewResourceId, ArrayList<BluetoothDevice> devices) {
        super(context, viewResourceId, devices);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        BluetoothDevice device = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_device, parent, false);
        }
        // Lookup view for data population
        TextView deviceName = (TextView) convertView.findViewById(R.id.device_name);
        TextView deviceAddr = (TextView) convertView.findViewById(R.id.device_addr);
        deviceName.setText(device.getName());
        deviceAddr.setText(device.getAddress());
        // Return the completed view to render on screen
        return convertView;
    }
}
