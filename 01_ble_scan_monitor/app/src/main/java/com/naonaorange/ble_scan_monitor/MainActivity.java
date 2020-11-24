package com.naonaorange.ble_scan_monitor;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Button startScanButton;
    Button stopScanButton;
    ListView deviceListView;

    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothLeScanner btScanner;

    private final static int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    ArrayList<DeviceInfo> deviceList= new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startScanButton = findViewById(R.id.startScanButton);
        startScanButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                startScan();
            }
        });

        stopScanButton = findViewById(R.id.stopScanButton);
        stopScanButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                stopScan();
            }
        });
        stopScanButton.setEnabled(false);

        deviceListView = findViewById(R.id.peripheralListView);

        btManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        btScanner = btAdapter.getBluetoothLeScanner();
        if(btAdapter == null || !btAdapter.isEnabled()){
            Intent btEnableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(btEnableIntent, REQUEST_ENABLE_BT);
        }
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);

        startScan();
    }

    public ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if(result.getDevice().getName() == null) return;//デバイス名がないものは表示しない

            boolean isNewDevice = true;
            for(int i = 0; i < deviceList.size(); i++) {
                if (result.getDevice().getAddress().equals(deviceList.get(i).bluetoothDevice.getAddress())){
                    deviceList.get(i).setRssi(result.getRssi());
                    isNewDevice = false;
                }
            }
            if(isNewDevice) {
                DeviceInfo info = new DeviceInfo(result.getDevice(), result.getRssi());
                deviceList.add(info);
            }
            UserAdapter adapter = new UserAdapter(getApplicationContext(), 0, deviceList);
            deviceListView.setAdapter(adapter);
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Because location access has not been granted.");
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
    private List<ScanFilter> buildScanFilters() {
        List<ScanFilter> scanFilters = new ArrayList<>();
        ScanFilter.Builder builder = new ScanFilter.Builder();
        //builder.setServiceUuid(null);
        scanFilters.add(builder.build());
        return scanFilters;
    }

    private ScanSettings buildScanSettings() {
        ScanSettings.Builder builder = new ScanSettings.Builder();
        builder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        builder.setLegacy(false);
        //builder.setPhy(ScanSettings.PHY_LE_ALL_SUPPORTED);
        return builder.build();
    }
    public void startScan() {
        startScanButton.setEnabled(false);
        stopScanButton.setEnabled(true);
        deviceList= new ArrayList<>();
        UserAdapter adapter = new UserAdapter(getApplicationContext(), 0, deviceList);
        deviceListView.setAdapter(adapter);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.startScan(buildScanFilters(), buildScanSettings(), leScanCallback);
            }
        });
    }

    public void stopScan(){
        startScanButton.setEnabled(true);
        stopScanButton.setEnabled(false);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.stopScan(leScanCallback);
            }
        });

    }
}
