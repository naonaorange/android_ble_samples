package com.example.nao.ble_scan_monitor;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    TextView scanResultTextView;

    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothLeScanner btScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startScanningButton = (Button)findViewById(R.id.startScanningButton);
        startScanningButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                startScanning();
            }
        });

        Button stopScanningButton = (Button)findViewById(R.id.stopScanningButton);
        stopScanningButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                stopScanning();
            }
        });

        scanResultTextView = (TextView)findViewById(R.id.scanResultTextView);
        scanResultTextView.setMovementMethod(new ScrollingMovementMethod());

        //setup ble
        btManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        btScanner = btAdapter.getBluetoothLeScanner();

        if(btAdapter == null || !btAdapter.isEnabled()){
            Intent btEnableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(btEnableIntent, REQUEST_ENABLE_BT);
        }

        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
    }

    public ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            scanResultTextView.append(result.getDevice().getName() + "\n");

            final int scrollAmount = scanResultTextView.getLayout().getLineTop(scanResultTextView.getLineCount()) - scanResultTextView.getHeight();
            if (scrollAmount > 0)
                scanResultTextView.scrollTo(0, scrollAmount);
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

    public void startScanning() {
        scanResultTextView.setText("");
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.startScan(leScanCallback);
            }
        });

    }

    public void stopScanning(){
        scanResultTextView.setText("Stop Scanning");
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.stopScan(leScanCallback);
            }
        });

    }

}
