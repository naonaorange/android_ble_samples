package com.example.tmp.android_ble_heart_rate_monitor;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    Button scanButton;
    Button connectButton;
    Button disConnectButton;
    TextView deviceNameTextView;
    TextView addressTextView;
    TextView heartRateValue;
    LineChart chart;

    BluetoothDevice selectedPeripheralDevice;
    BluetoothGatt bluetoothGatt;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    private static String UUID_HEART_RATE = "0000180D-0000-1000-8000-00805f9b34fb";
    private static String UUID_HEART_RATE_MESUREMENT = "00002A37-0000-1000-8000-00805f9b34fb";
    private static String UUID_DESCRIPTORS = "00002902-0000-1000-8000-00805f9b34fb";

    ArrayList<Entry> heartRateList;

    int x;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scanButton = (Button) findViewById(R.id.ScanButton);
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                int requestCode = 1001;
                startActivityForResult(intent, requestCode);
            }
        });

        connectButton = (Button) findViewById(R.id.ConnectButton);
        connectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                connect();
            }
        });

        disConnectButton = (Button) findViewById(R.id.DisconnectButton);
        disConnectButton.setVisibility(View.INVISIBLE);
        disConnectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                disconnect();
            }
        });

        deviceNameTextView = (TextView) findViewById(R.id.DeviceNameTextView);
        addressTextView = (TextView) findViewById(R.id.AddressTextView);
        heartRateValue = (TextView) findViewById(R.id.HeartRateValue);

        chart = (LineChart) findViewById(R.id.Chart);
        heartRateList = new ArrayList<Entry>();

        heartRateList.add(new Entry(100, 0));
        /*
        heartRateList.add(new Entry(120, 1));
        heartRateList.add(new Entry(150, 2));
        heartRateList.add(new Entry(250, 30));
        */
        heartRateList.add(new Entry(0, 0));
        x++;

        LineDataSet valueDataSet = new LineDataSet(heartRateList, "Heart Rate Mesurement");
        chart.setData(new LineData(valueDataSet));

    }

    public void onActivityResult( int requestCode, int resultCode, Intent intent )
    {
        if( requestCode == 1001 ){
            if( resultCode == Activity.RESULT_OK ){
                selectedPeripheralDevice = (BluetoothDevice)intent.getParcelableExtra("PeripheralDevice") ;

                deviceNameTextView.setText(selectedPeripheralDevice.getName());
                addressTextView.setText(selectedPeripheralDevice.getAddress());
            }
        }
    }


    private final BluetoothGattCallback btleGattCallback = new BluetoothGattCallback() {

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }

        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {

            switch (newState) {
                case 0:
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            connectButton.setVisibility(View.VISIBLE);
                            disConnectButton.setVisibility(View.INVISIBLE);
                        }
                    });
                    break;
                case 2:
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            connectButton.setVisibility(View.INVISIBLE);
                            disConnectButton.setVisibility(View.VISIBLE);
                        }
                    });
                    bluetoothGatt.discoverServices();
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            displayGattServices(bluetoothGatt.getServices());
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }
    };

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {

        if(UUID.fromString(UUID_HEART_RATE_MESUREMENT).equals(characteristic.getUuid())){
            int flag = characteristic.getProperties();
            int format = -1;

            if((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
            }else{
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
            }
            final int heartRate = characteristic.getIntValue(format, 1);

            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    heartRateValue.setText(String.valueOf(heartRate));






                }
            });
            heartRateList.add(new Entry(x, heartRate));
            LineDataSet valueDataSet = new LineDataSet(heartRateList, "Heart Rate Mesurement");
            valueDataSet.notifyDataSetChanged();
            chart.setData(new LineData(valueDataSet));
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();
            chart.setVisibleXRange(0, 100);
            chart.moveViewToX(valueDataSet.getEntryCount());
            x++;
        }
    }


    public void connect() {
        bluetoothGatt = selectedPeripheralDevice.connectGatt(this, false, btleGattCallback);
    }

    public void disconnect() {
        bluetoothGatt.disconnect();
    }

    public void setNotificationStatus(boolean enable){
        BluetoothGattCharacteristic btGattCharacteristic = null;

        BluetoothGattService btGattService = bluetoothGatt.getService(UUID.fromString(UUID_HEART_RATE));
        if (btGattService != null) {
            btGattCharacteristic = btGattService.getCharacteristic(UUID.fromString(UUID_HEART_RATE_MESUREMENT));

            if (btGattCharacteristic != null) {
                bluetoothGatt.setCharacteristicNotification(btGattCharacteristic, enable);

                BluetoothGattDescriptor descriptor = btGattCharacteristic.getDescriptor(UUID.fromString(UUID_DESCRIPTORS));
                if(enable == true) {
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                }else{
                    descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                }
                bluetoothGatt.writeDescriptor(descriptor);
            }
        }
    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {

            final String uuid = gattService.getUuid().toString();
            new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();

            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {

                final String charUuid = gattCharacteristic.getUuid().toString();
            }
            setNotificationStatus(true);
        }
    }
}