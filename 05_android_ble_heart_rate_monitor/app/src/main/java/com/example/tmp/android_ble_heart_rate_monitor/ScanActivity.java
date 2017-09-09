package com.example.tmp.android_ble_heart_rate_monitor;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

public class ScanActivity extends AppCompatActivity {

    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothLeScanner btScanner;

    Button startScanningButton;
    ProgressBar scanProgressBar;
    ListView deviceListView;

    private final static int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    ArrayList<DeviceInfo> deviceList= new ArrayList<DeviceInfo>();
    BluetoothDevice selectedPeripheralDevice;

    private Handler mHandler = new Handler();
    private static final long SCAN_PERIOD = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        startScanningButton = (Button) findViewById(R.id.RescanButton);
        startScanningButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startScan();
            }
        });

        scanProgressBar = (ProgressBar)findViewById(R.id.ScanProgressBar);
        scanProgressBar.setVisibility(View.INVISIBLE);

        btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        btScanner = btAdapter.getBluetoothLeScanner();

        deviceListView = (ListView)findViewById(R.id.PeripheralListView);
        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                view.setSelected(true);
                ListView listView = (ListView)parent;
                selectedPeripheralDevice = ((DeviceInfo)listView.getItemAtPosition(pos)).bluetoothDevice;

                Intent intent = new Intent();
                intent.putExtra( "PeripheralDevice", selectedPeripheralDevice );
                setResult( Activity.RESULT_OK, intent );
                finish();
            }
        });

        if (btAdapter != null && !btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);

        startScan();
    }


    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {

            //デバイス名がないものは表示しない
            if(result.getDevice().getName() == null) return;

            //重複するものは表示しない
            for(int i = 0; i < deviceList.size(); i++) {
                if (result.getDevice().getAddress().equals(deviceList.get(i).bluetoothDevice.getAddress())) return;
            }
            DeviceInfo info = new DeviceInfo(result.getDevice(), result.getRssi());
            deviceList.add(info);
            //peripheralDeviceList.add(result.getDevice());

            UserAdapter adapter = new UserAdapter(getApplicationContext(), 0, deviceList);
            deviceListView.setAdapter(adapter);
        }
    };

    public void startScan() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.startScan(leScanCallback);
                ScanActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        startScanningButton.setEnabled(false);
                        scanProgressBar.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopScan();
                ScanActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        startScanningButton.setEnabled(true);
                        scanProgressBar.setVisibility(View.INVISIBLE);
                    }
                });
            }
        }, SCAN_PERIOD);
    }

    public void stopScan() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.stopScan(leScanCallback);
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    builder.show();
                }
                return;
            }
        }
    }
}




