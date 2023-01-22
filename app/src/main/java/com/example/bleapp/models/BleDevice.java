package com.example.bleapp.models;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;

public class BleDevice {

    private BluetoothDevice bluetoothDevice;
    private int rssi;

    public BleDevice(BluetoothDevice bluetoothDevice){
        this.bluetoothDevice = bluetoothDevice;
    }

    public String getAddress(){
        return bluetoothDevice.getAddress();
    }

    @SuppressLint("MissingPermission")
    public String getName(){
        if(bluetoothDevice.getName() == null){
            return "No name";
        }
        return bluetoothDevice.getName();
    }

    public void setRSSI(int rssi){
        this.rssi = rssi;
    }

    public int getRSSI(){
        return rssi;
    }

}
