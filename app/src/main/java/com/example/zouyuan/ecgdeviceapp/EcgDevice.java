package com.example.zouyuan.ecgdeviceapp;

/**
 * Created by zouyuan on 2017/3/31.
 */

public class EcgDevice{
    private String deviceName;
    private String deviceAddress;


    public EcgDevice(String deviceName, String deviceAddress) {
        this.deviceName = deviceName;
        this.deviceAddress = deviceAddress;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}
