package com.example.bleapp;

import android.app.Application;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.example.bleapp.datas.BleManager;
import com.example.bleapp.datas.DataManager;
import com.example.bleapp.services.BleService;

public class BleApplication extends Application {

    public void onCreate() {
        super.onCreate();
        DataManager.initInstance();
        BleManager.initInstance(getApplicationContext());
        initBleService();
    }

    ServiceConnection bleServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BleService.localBinder binder = (BleService.localBinder) service;
            BleService bl = binder.getService();
            BleManager.getInstance().setBleService(bl);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            BleManager.getInstance().setBleService(null);
        }
    };

    void initBleService()
    {
        Intent intent = new Intent(this, BleService.class);
        bindService(intent, bleServiceConnection, BIND_AUTO_CREATE);
    }
}
