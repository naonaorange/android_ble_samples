package com.example.tmp.android_ble_enable_chacker;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private final int REQUEST_ENABLE_BT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void clickCheckButton(View view){
        BluetoothManager btm = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bta = btm.getAdapter();

        if(bta == null || !bta.isEnabled()){
            Intent btIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(btIntent, REQUEST_ENABLE_BT);
        }else{
            ((TextView)findViewById(R.id.myTextView)).setText("Disable BLE function.");
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        TextView tv = (TextView)findViewById(R.id.myTextView);
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    tv.setText("Enable BLE funciton.");
                } else{
                    tv.setText("Disable BLE funciton.");
                }
                break;
            default:
                break;
        }
    }
}
