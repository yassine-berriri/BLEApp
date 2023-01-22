package com.example.bleapp.services;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.bleapp.R;
import com.example.bleapp.tools.Utils;

import java.util.List;
import java.util.UUID;

public class BleService extends Service {



    public interface BleServiceListener {
        void onNeedEnable(KindPeripheral p);
        void onNeedPermission();
        void onScanFound(BluetoothDevice device, int rssi);
        void onScanFailed(int errorCode);
        void onStarted();
        void onConnected();
        void onDisconnected();
        void onServicesDiscovered();
        void onCharacteristicReceive();
    }




    public enum KindPeripheral {
        BLUETOOTH,GBS
    }
    private Binder binder = new BleService.localBinder();
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothGatt mBluetoothGatt;
    private boolean mScanning;
    private Context context;
    private BleServiceListener mListener;
    private String mBluetoothDeviceAddress;
    private int mConnectionState = STATE_DISCONNECTED;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;


    public BleService(){

    }

    public void onCreate()
    {
        super.onCreate();
        this.context = getApplicationContext();
    }

    public void setListener(BleServiceListener mListener){
        this.mListener = mListener;
    }



    public boolean initBluetoothManager()
    {
        if(mBluetoothManager == null)
        {
            mBluetoothManager = (BluetoothManager) this.context.getSystemService(Context.BLUETOOTH_SERVICE);
        }
        if(mBluetoothManager != null)
        {
            if(mBluetoothAdapter == null)
            {
                mBluetoothAdapter = mBluetoothManager.getAdapter();
            }
            if(mBluetoothLeScanner == null && mBluetoothAdapter != null)
            {
                mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            }
        }

        return (mBluetoothManager != null && mBluetoothAdapter != null && mBluetoothLeScanner != null);
    }

    private boolean isBleAvailable()
    {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    boolean isEnablePeripheral()
    {
        if(!mBluetoothAdapter.isEnabled())
        {
            if(mListener != null)
            {
                mListener.onNeedEnable(KindPeripheral.BLUETOOTH);
            }
            return false;
        }

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
        {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            {
                mListener.onNeedEnable(KindPeripheral.GBS);
            }
            return false;
        }
     return true;
    }

    private boolean hasPermissionProximity()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        {
         if(checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
         || checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
         ){
             mListener.onNeedPermission();
            return false;
         }
        }
        return true;
    }

    private boolean permissionLocation()
    {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.S){
            if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                 || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                 || checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED
                 || checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED
            ){
                mListener.onNeedPermission();
            return false;
            }
        }
        return true;
    }

    private boolean needPermission()
    {
        return !(hasPermissionProximity() && permissionLocation());
    }

    public boolean IsScan(){
        return mScanning;
    }
    @SuppressLint("MissingPermission")
    public boolean startScan() {
        boolean isInit = initBluetoothManager();
        if(isInit)
        {
            boolean needPermission = needPermission();
            boolean isEnablePeripheral = isEnablePeripheral();
            if(!needPermission || !isEnablePeripheral)
            {
                if(!mScanning)
                {
                    mBluetoothLeScanner.startScan(mScanCallback);
                    mScanning = true;
                    if(mListener != null)
                    {
                      mListener.onStarted();
                    }

                }
                return true;
            }
        }
        return false;
    }

    @SuppressLint("MissingPermission")
    public void stopScan()
    {
        mBluetoothLeScanner.stopScan(mScanCallback);
        mScanning = false;
    }

    //Device Scan callback
    private ScanCallback mScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);

                    if(mListener != null)
                    {
                       mListener.onScanFound(result.getDevice(),result.getRssi());
                    }
                }

                @Override
                public void onScanFailed(int errorCode) {
                    super.onScanFailed(errorCode);
                    if(mListener != null)
                    {
                        mListener.onScanFailed(errorCode);
                    }
                }
            };

    /****/

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback(){

        @SuppressLint("MissingPermission")
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Connected to GATT server.");
                mConnectionState = STATE_CONNECTING;
                mBluetoothGatt.discoverServices();
                mListener.onConnected();
                Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());
            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected from GATT server.");
                mConnectionState = STATE_DISCONNECTED;
                mListener.onDisconnected();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status)
        {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mListener.onServicesDiscovered();
            }
            else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status)
        {
            if (status == BluetoothGatt.GATT_SUCCESS) {

                mListener.onCharacteristicReceive();

            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic)
        {
            mListener.onCharacteristicReceive();
        }



        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status)
        {
            if (status == BluetoothGatt.GATT_SUCCESS)
            {
                gatt.getService(UUID.fromString(getString(R.string.UUID_SERVICE)))
                        .getCharacteristic(UUID.fromString(getString(R.string.UUID_HEART_RATE_MEASUREMENT)));
                mListener.onCharacteristicReceive();
            }
            else
            {
                Log.d(TAG, "onDescriptorWrite: modification du descripteur a échoué ");
            }
        }


    };



    @SuppressLint("MissingPermission")
    public boolean connect(final String address)
    {
        if (mBluetoothAdapter == null || address == null)
        {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress) && mBluetoothGatt != null)
        {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect())
            {
                mConnectionState = STATE_CONNECTING;
                return true;
            }
            else
            {
                return false;
            }
        }
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null)
        {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        return true;
    }
    @SuppressLint("MissingPermission")
    public void disconnect()
    {
        if (mBluetoothAdapter == null || mBluetoothGatt == null)
        {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        mBluetoothGatt.disconnect();
    }

    @SuppressLint("MissingPermission")
    public void close()
    {

        if (mBluetoothGatt == null)
        {
            return;
        }

        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    @SuppressLint("MissingPermission")
    public void readCharacteristic(BluetoothGattCharacteristic characteristic)
    {
        if (mBluetoothAdapter == null || mBluetoothGatt == null)
        {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @SuppressLint("MissingPermission")
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled)
    {
        if (mBluetoothAdapter == null || mBluetoothGatt == null|| characteristic == null)
        {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        mBluetoothGatt.setCharacteristicNotification(characteristic,enabled);
/*

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(getString(R.string.CLIENT_CHARACTERISTIC_CONFIG)));
        if (enabled)
        {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        }
        else
        {
            descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }
        mBluetoothGatt.writeDescriptor(descriptor);

 */
    }






    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) {
            return null;
        }
        return mBluetoothGatt.getServices();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    public class localBinder extends Binder {
        public BleService getService() {
            return BleService.this;
        }
    }
}
