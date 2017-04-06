package com.example.zouyuan.ecgdeviceapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class DeviceScanActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "DeviceScanActivity";
    private static final long SCAN_PERIOD = 1000;
    private List<BluetoothDevice> ecgDeviceList = new ArrayList<>();
    private EcgDeviceListAdapter ecgDeviceListAdapter;
    private Button scanButton;
    private TextView connectStatusTV;
    private Handler mHandler;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private List<BluetoothGattService> ecgDeviceServicesList = new ArrayList<>();
    private BluetoothGattService ecgService;
    private BluetoothGattCharacteristic notifyCharacteristic;

    private boolean mScanning = false;
    private boolean isConnected = false;

    public final static String HEART_ECG_SERVICE = "0000fff0-0000-1000-8000-00805f9b34fb";
    public final static String NOTIFY_CHARACTERISTIC = "0000fff1-0000-1000-8000-00805f9b34fb";
    public final static String CLIENT_CHARACTERISTIC_CONFIG ="00002902-0000-1000-8000-00805f9b34fb";


    private BluetoothAdapter.LeScanCallback leScanCallback
            = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int i, byte[] bytes) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(!ecgDeviceList.contains(device)){
                        ecgDeviceList.add(device);
                        ecgDeviceListAdapter.notifyDataSetChanged();
                    }
                }
            });
            //Log.d(TAG, "name:" + device.getName() + "\n"+"address:" + device.getAddress());
        }
    };
    private android.bluetooth.BluetoothGattCallback gattCallBack = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, final int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switch(newState){
                        case BluetoothGatt.STATE_CONNECTED:
                            connectStatusTV.setText("已连接上");
                            isConnected = true;

                            bluetoothGatt.discoverServices();//find the service


                            break;
                        case BluetoothGatt.STATE_DISCONNECTED:

                            connectStatusTV.setText("未连接上");
                            isConnected = false;
                            break;
                        case BluetoothGatt.STATE_CONNECTING:

                            connectStatusTV.setText("连接中...");
                            break;
                        case BluetoothGatt.STATE_DISCONNECTING:

                            connectStatusTV.setText("连接取消中...");
                            break;
                        default:
                            break;
                    }
                }
            });

        }
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.d(TAG, "onCharacteristicChanged: been here1");

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);

            if(characteristic.getUuid().toString().equals(notifyCharacteristic.getUuid().toString())){
                Log.d(TAG, "onCharacteristicChanged: " + "receive data from device");
            }
        }
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if(status == BluetoothGatt.GATT_SUCCESS){
                ecgDeviceServicesList = bluetoothGatt.getServices();
                for(BluetoothGattService service:ecgDeviceServicesList){
                    Log.d(TAG, "onServicesDiscovered: " + service.getUuid().toString());
                    List<BluetoothGattCharacteristic> bluetoothGattCharacteristics =
                            service.getCharacteristics();

                    for(BluetoothGattCharacteristic characteristic : bluetoothGattCharacteristics){
                        Log.d(TAG, "Characteristic: " + characteristic.getUuid().toString() );
                        if(characteristic.getUuid().toString().equals(NOTIFY_CHARACTERISTIC)){
                            Log.d(TAG, "onServicesDiscovered: been here");
                            ecgService = bluetoothGatt.getService(UUID.fromString(HEART_ECG_SERVICE));
                            Log.d(TAG, "onServicesDiscovered: " + ecgService.getUuid().toString());
                            notifyCharacteristic = ecgService.getCharacteristic(UUID.fromString(NOTIFY_CHARACTERISTIC));
                            bluetoothGatt.setCharacteristicNotification(notifyCharacteristic,true);
                            bluetoothGatt.readCharacteristic(notifyCharacteristic);
                        }
                    }

                }

            }
        }

    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_scan);
        mHandler = new Handler();

        connectStatusTV = (TextView) findViewById(R.id.itv_connect_status);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ecgDeviceListAdapter = new EcgDeviceListAdapter
                (DeviceScanActivity.this, R.layout.device_item_layout, ecgDeviceList);
        final ListView listView = (ListView) findViewById(R.id.ilv_device_list);
        listView.setAdapter(ecgDeviceListAdapter);//load list into listview


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice ecgDevice = ecgDeviceList.get(position);



                //Toast.makeText(view.getContext(),ecgDevice.getDeviceName(),Toast.LENGTH_SHORT).show();
                if (ecgDevice == null) {
                    return;
                }else{
                    bluetoothGatt = ecgDevice.connectGatt(DeviceScanActivity.this,false,gattCallBack);
                    connectStatusTV.setText("连接中...");



                    if(isConnected){
                        ecgDeviceListAdapter.notifyDataSetChanged();
                        final Intent intent = new Intent(view.getContext(), DeviceControlActivity.class);
                        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, ecgDevice.getName());
                        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, ecgDevice.getAddress());
                        if (mScanning) {
                            bluetoothAdapter.stopLeScan(leScanCallback);
                            mScanning = false;
                        }
                        startActivity(intent);

                    }else{
                        Toast.makeText(view.getContext(),"can't connect BLE device",Toast.LENGTH_SHORT).show();

                    }

                }
            }
        });
        scanButton = (Button) findViewById(R.id.ibt_scan_device);
        scanButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_scan_device: {
                mScanning = true;
                scanEcgDevice(true);
                break;
            }
            default:
                break;
        }
    }

    public void scanEcgDevice(boolean enable) {
        ecgDeviceList.clear();
/*        BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        bluetoothLeScanner.startScan();*/
        //Log.d(TAG, "scanEcgDevice: been here2");
        // bluetoothAdapter.startLeScan(leScanCallback);
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    bluetoothAdapter.stopLeScan(leScanCallback);
                    // Log.d(TAG, "scanEcgDevice: been here3");
                }
            }, SCAN_PERIOD);
            mScanning = true;
            bluetoothAdapter.startLeScan(leScanCallback);
        } else {
            mScanning = false;
            bluetoothAdapter.stopLeScan(leScanCallback);
        }
    }
}
