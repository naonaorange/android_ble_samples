package com.example.ble.android_ble_gatt_read_write;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
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
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothLeScanner btScanner;
    Button startScanningButton;
    Button stopScanningButton;
    Button readDeviceNameButton;
    Button readAppearanceButton;
    Button writeDeviceNameButton;
    TextView peripheralTextView;
    private final static int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    Boolean btScanning = false;
    int deviceIndex = 0;
    ArrayList<BluetoothDevice> devicesDiscovered = new ArrayList<BluetoothDevice>();
    EditText deviceIndexInput;
    EditText deviceNameInput;
    Button connectToDevice;
    Button disconnectDevice;
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

    private static String UUID_GENERIC_ACCESS = "00001800-0000-1000-8000-00805f9b34fb";
    private static String UUID_DEVICE_NAME = "00002A00-0000-1000-8000-00805f9b34fb";
    private static String UUID_APPEARANCE = "00002A01-0000-1000-8000-00805f9b34fb";

    public Map<String, String> uuids = new HashMap<String, String>();

    // Stops scanning after 5 seconds.
    private Handler mHandler = new Handler();
    private static final long SCAN_PERIOD = 5000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        peripheralTextView = (TextView) findViewById(R.id.PeripheralTextView);
        peripheralTextView.setMovementMethod(new ScrollingMovementMethod());
        deviceIndexInput = (EditText) findViewById(R.id.InputIndex);
        deviceIndexInput.setText("0");

        connectToDevice = (Button) findViewById(R.id.ConnectButton);
        connectToDevice.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                connectToDeviceSelected();
            }
        });

        disconnectDevice = (Button) findViewById(R.id.DisconnectButton);
        disconnectDevice.setVisibility(View.INVISIBLE);
        disconnectDevice.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                disconnectDeviceSelected();
            }
        });

        startScanningButton = (Button) findViewById(R.id.StartScanButton);
        startScanningButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startScanning();
            }
        });

        stopScanningButton = (Button) findViewById(R.id.StopScanButton);
        stopScanningButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopScanning();
            }
        });
        stopScanningButton.setVisibility(View.INVISIBLE);

        readDeviceNameButton = (Button)findViewById(R.id.ReadDeviceNameButton);
        readDeviceNameButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                readDeviceName();
            }
        });

        readAppearanceButton = (Button)findViewById(R.id.ReadAppearanceButton);
        readAppearanceButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                readAppearance();
            }
        });

        writeDeviceNameButton = (Button)findViewById(R.id.WriteDeviceNameButton);
        writeDeviceNameButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                writeDeviceName();
            }
        });

        deviceNameInput = (EditText) findViewById(R.id.DeviceName);
        deviceNameInput.setText("");

        btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        btScanner = btAdapter.getBluetoothLeScanner();

        if (btAdapter != null && !btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
    }

    // Device scan callback.
    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            peripheralTextView.append("Index: " + deviceIndex + ", Device Name: " + result.getDevice().getName() + " rssi: " + result.getRssi() + "\n");
            devicesDiscovered.add(result.getDevice());
            deviceIndex++;
            // auto scroll for text view
            final int scrollAmount = peripheralTextView.getLayout().getLineTop(peripheralTextView.getLineCount()) - peripheralTextView.getHeight();
            // if there is no need to scroll, scrollAmount will be <=0
            if (scrollAmount > 0) {
                peripheralTextView.scrollTo(0, scrollAmount);
            }
        }
    };

    // Device connect call back
    private final BluetoothGattCallback btleGattCallback = new BluetoothGattCallback() {

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            // this will get called anytime you perform a read or write characteristic operation
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    peripheralTextView.append("device read or wrote to\n");
                }
            });
        }

        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            // this will get called when a device connects or disconnects
            System.out.println(newState);
            switch (newState) {
                case 0:
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            peripheralTextView.append("device disconnected\n");
                            connectToDevice.setVisibility(View.VISIBLE);
                            disconnectDevice.setVisibility(View.INVISIBLE);
                        }
                    });
                    break;
                case 2:
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            peripheralTextView.append("device connected\n");
                            connectToDevice.setVisibility(View.INVISIBLE);
                            disconnectDevice.setVisibility(View.VISIBLE);
                        }
                    });

                    // discover services and characteristics for this device
                    bluetoothGatt.discoverServices();

                    break;
                default:
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            peripheralTextView.append("we encounterned an unknown state, uh oh\n");
                        }
                    });
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            // this will get called after the client initiates a 			BluetoothGatt.discoverServices() call
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    peripheralTextView.append("device services have been discovered\n");
                }
            });
            displayGattServices(bluetoothGatt.getServices());
        }

        @Override
        // Result of a characteristic read operation
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);

            if(status == BluetoothGatt.GATT_SUCCESS){
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        peripheralTextView.append("Success request write characteristics\n");
                    }
                });
            }
        }

    };

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {

        if(UUID.fromString(UUID_DEVICE_NAME).equals(characteristic.getUuid())){
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    peripheralTextView.append("device name : " + characteristic.getStringValue(0) + "\n");
                }
            });
        }else if(UUID.fromString(UUID_APPEARANCE).equals(characteristic.getUuid())){
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    peripheralTextView.append("Appearance : " + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0) + "\n");
                }
            });
        }else{
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    peripheralTextView.append("Characteristics UUID is not supported\n");
                }
            });
        }
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

    public void startScanning() {
        System.out.println("start scanning");
        btScanning = true;
        deviceIndex = 0;
        devicesDiscovered.clear();
        peripheralTextView.setText("");
        peripheralTextView.append("Started Scanning\n");
        startScanningButton.setVisibility(View.INVISIBLE);
        stopScanningButton.setVisibility(View.VISIBLE);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.startScan(leScanCallback);
            }
        });

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopScanning();
            }
        }, SCAN_PERIOD);
    }

    public void stopScanning() {
        System.out.println("stopping scanning");
        peripheralTextView.append("Stopped Scanning\n");
        btScanning = false;
        startScanningButton.setVisibility(View.VISIBLE);
        stopScanningButton.setVisibility(View.INVISIBLE);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.stopScan(leScanCallback);
            }
        });
    }

    public void connectToDeviceSelected() {
        peripheralTextView.append("Trying to connect to device at index: " + deviceIndexInput.getText() + "\n");
        int deviceSelected = Integer.parseInt(deviceIndexInput.getText().toString());
        bluetoothGatt = devicesDiscovered.get(deviceSelected).connectGatt(this, false, btleGattCallback);
    }

    public void disconnectDeviceSelected() {
        peripheralTextView.append("Disconnecting from device\n");
        bluetoothGatt.disconnect();
    }

    public void readDeviceName(){
        BluetoothGattCharacteristic btGattCharacteristic = null;

        BluetoothGattService btGattService = bluetoothGatt.getService(UUID.fromString(UUID_GENERIC_ACCESS));

        if (btGattService != null) {
            btGattCharacteristic = btGattService.getCharacteristic(UUID.fromString(UUID_DEVICE_NAME));

            if(btGattCharacteristic != null){
                if(bluetoothGatt.readCharacteristic(btGattCharacteristic) == true){
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            peripheralTextView.append("Send request reading characteristic\n");
                        }
                    });
                }
            }else{
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        peripheralTextView.append("readCharacteristics is failed\n");
                    }
                });
            }
        }
    }

    public void readAppearance(){
        BluetoothGattCharacteristic btGattCharacteristic = null;

        BluetoothGattService btGattService = bluetoothGatt.getService(UUID.fromString(UUID_GENERIC_ACCESS));

        if (btGattService != null) {
            btGattCharacteristic = btGattService.getCharacteristic(UUID.fromString(UUID_APPEARANCE));

            if(btGattCharacteristic != null){
                if(bluetoothGatt.readCharacteristic(btGattCharacteristic) == true){
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            peripheralTextView.append("Send request reading characteristic\n");
                        }
                    });
                }
            }else{
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        peripheralTextView.append("readCharacteristics is failed\n");
                    }
                });
            }
        }
    }

    public void writeDeviceName(){
        BluetoothGattCharacteristic btGattCharacteristic = null;

        BluetoothGattService btGattService = bluetoothGatt.getService(UUID.fromString(UUID_GENERIC_ACCESS));

        try {
            if (btGattService != null) {
                btGattCharacteristic = btGattService.getCharacteristic(UUID.fromString(UUID_DEVICE_NAME));

                if (btGattCharacteristic != null) {

                    String deviceName = deviceNameInput.getText().toString();
                    byte[] writeData = deviceName.getBytes("US-ASCII");

                    for (int i = 0; i < writeData.length; i++){
                        System.out.println(i + "\n");
                    }

                    if (btGattCharacteristic.setValue(writeData) == true) {
                        if (bluetoothGatt.writeCharacteristic(btGattCharacteristic) == true) {
                            MainActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    peripheralTextView.append("Send request writting characteristic\n");
                                }
                            });
                        }
                    }
                } else {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            peripheralTextView.append("writeCharacteristics is failed\n");
                        }
                    });
                }
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {

            final String uuid = gattService.getUuid().toString();
            System.out.println("Service discovered: " + uuid);
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    peripheralTextView.append("Service disovered: "+uuid+"\n");
                }
            });
            new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic :
                    gattCharacteristics) {

                final String charUuid = gattCharacteristic.getUuid().toString();
                System.out.println("Characteristic discovered for service: " + charUuid);
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        peripheralTextView.append("Characteristic discovered for service: "+charUuid+"\n");
                    }
                });

            }
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    peripheralTextView.append("Finished discovering characteristics\n");
                }
            });
        }
    }
}

