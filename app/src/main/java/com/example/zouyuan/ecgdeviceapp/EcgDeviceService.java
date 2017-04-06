package com.example.zouyuan.ecgdeviceapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class EcgDeviceService extends Service {

    public final static String HEART_ECG_SERVICE = "0000fff0-0000-1000-8000-00805f9b34fb";
    public final static String NOTIFY_CHARACTERISTIC = "0000fff1-0000-1000-8000-00805f9b34fb";
    public final static String CLIENT_CHARACTERISTIC_CONFIG ="00002902-0000-1000-8000-00805f9b34fb";

    public EcgDeviceService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
