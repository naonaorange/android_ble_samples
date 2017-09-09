package com.example.tmp.android_ble_heart_rate_monitor;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by naolu on 2017/09/09.
 */

class DeviceInfo{
    public BluetoothDevice bluetoothDevice;
    public int rssi;

    public DeviceInfo(BluetoothDevice device, int rssi){
        this.bluetoothDevice = device;
        this.rssi = rssi;
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public int getRssi() {
        return rssi;
    }
}


class UserAdapter extends ArrayAdapter<DeviceInfo> {
    private LayoutInflater layoutInflater;
    public UserAdapter(Context c, int id, ArrayList<DeviceInfo> di) {
        super(c, id, di);
        this.layoutInflater = (LayoutInflater) c.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE
        );
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_item, parent, false);
        }

        DeviceInfo info = (DeviceInfo) getItem(position);
        ((TextView) convertView.findViewById(R.id.name)).setText("Name : " + info.getBluetoothDevice().getName());
        ((TextView) convertView.findViewById(R.id.comment)).setText("Addr   : " + info.getBluetoothDevice().getAddress());
        ((TextView) convertView.findViewById(R.id.rssi)).setText("Rssi    : " + String.valueOf(info.getRssi()));

        return convertView;
    }
}
