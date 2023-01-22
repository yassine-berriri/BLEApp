package com.example.bleapp.datas;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import com.example.bleapp.models.BleDevice;
import com.example.bleapp.services.BleService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BleManager implements BleService.BleServiceListener {

    private static BleManager singleInstance;

    private BleManager(Context context){
        this.mContext = context;
        this.mHashMapBLEDevices = new HashMap<>();
    }

    public static BleManager getInstance() {
        return singleInstance;
    }

    public static void initInstance(Context context) {
        if (singleInstance == null) {
            singleInstance = new BleManager(context);
        }
    }

    public enum KindItemData {
        ARRAY_BLE_SERVICES,
        ARRAY_BLE_CHARACTERISTICS
    }

    public enum KindErrorBle {
        BLE_NOT_FOUND,
        GPS_DISABLED,
        BLE_DISABLED,
        PERMISSION
    }

    public interface BleManagerListener {
        void onBleStartError(KindErrorBle k);
        void bleNotFound();
        void scanFailed(int errorCode);
        void bleConnected();
        void bleDisconnected();
        void onUpdateBleManager(KindItemData k);
    }

    private Context mContext;
    private HashMap<String, BleDevice> mHashMapBLEDevices;
    private String stringSearch = null;
    private BleManagerListener mListener;
    private HashMap<String, BluetoothGattCharacteristic> mHashMapCharacteristics;
    private ArrayList<BluetoothGattService> mArrayListBLEServices;
    private HashMap<String, ArrayList<BluetoothGattCharacteristic>> mHashMapBLECharacteristics;

    public void setListener(BleManagerListener mListener){
        this.mListener = mListener;
    }

    BleService serviceBle;

    public void setBleService(BleService bl){
        serviceBle = bl;
        if (bl != null) {
            bl.setListener(this);
        }
    }

    public HashMap<String, BleDevice > getHashMapBLE_Devices() {
        return this.mHashMapBLEDevices;
    }

    public void addBluetoothDevice(BluetoothDevice bluetoothDevice, int rssi) {
        if (!this.mHashMapBLEDevices.containsKey(bluetoothDevice.getAddress())) {
            BleDevice btleDevice = new BleDevice(bluetoothDevice);
            btleDevice.setRSSI(rssi);
            this.mHashMapBLEDevices.put(bluetoothDevice.getAddress(), btleDevice);
        }
        else {
            this.mHashMapBLEDevices.get(bluetoothDevice.getAddress()).setRSSI(rssi);
        }
        DataManager.getInstance().TextChanged(this.stringSearch);
    }

    @Override
    public void onNeedEnable(BleService.KindPeripheral p) {
        switch (p)
        {
            case BLUETOOTH:
                if(mListener != null)
                {
                    mListener.onBleStartError(KindErrorBle.BLE_DISABLED);
                }
                break;
            case GBS:
                if (mListener != null)
                {
                    mListener.onBleStartError(KindErrorBle.GPS_DISABLED);
                }
                break;
        }
    }

    private void notifyScanFailed(int errorCode){
        this.mListener.scanFailed(errorCode);
    }

    @Override
    public void onNeedPermission() {
        if(mListener != null)
        {
            mListener.onBleStartError(KindErrorBle.PERMISSION);
        }
    }

    @Override
    public void onScanFound(BluetoothDevice device, int rssi) {
        addBluetoothDevice(device,rssi);
    }

    @Override
    public void onScanFailed(int errorCode) {
        notifyScanFailed(errorCode);
    }

    public void startScan(){
        if (serviceBle != null){
            serviceBle.startScan();
        }
       // serviceBle.startScan();
    }
    public void stopScan(){
        if(serviceBle != null){
            serviceBle.stopScan();
        }
    }

    @Override
    public void onStarted() {

    }



    public boolean isScanning(){
        if(serviceBle != null)
        {
            return serviceBle.IsScan();
        }
        return false;
    }



    /***BleDetails***/
    public HashMap<String, BluetoothGattCharacteristic> getHashMapCharacteristics(){
        if(this.mHashMapCharacteristics == null){
            this.mHashMapCharacteristics = new HashMap<>();
        }
        return this.mHashMapCharacteristics;
    }

    public ArrayList<BluetoothGattService> getArrayListBLEServices() {
        if(mArrayListBLEServices == null){
            this.mArrayListBLEServices = new ArrayList<>();
        }

        return this.mArrayListBLEServices;
    }

    public HashMap<String, ArrayList<BluetoothGattCharacteristic>> getHashMapBLEListCharacteristics(){
        if(this.mHashMapBLECharacteristics == null){
            this.mHashMapBLECharacteristics = new HashMap<>();
        }
        return this.mHashMapBLECharacteristics;
    }

    public void addService(BluetoothGattService service){
        this.mArrayListBLEServices.add(service);
    }
    public void addCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic){
        if(this.mHashMapCharacteristics == null){
            this.mHashMapCharacteristics = new HashMap<>();
        }
        this.mHashMapCharacteristics.put(bluetoothGattCharacteristic.getUuid().toString(), bluetoothGattCharacteristic);
    }
    public void addCharacteristicList(String UIIDService, ArrayList<BluetoothGattCharacteristic> newCharacteristicsList){
       if(this.mHashMapBLECharacteristics == null)
       {
           this.mHashMapBLECharacteristics = new HashMap<>();
       }
        this.mHashMapBLECharacteristics.put(UIIDService, newCharacteristicsList);
    }

  /*
    public void updateService(){

        if (mBTLE_Service != null) {

            getArrayListBLEServices().clear();
            getHashMapCharacteristics().clear();
            getHashMapBLEListCharacteristics().clear();

            List<BluetoothGattService> servicesList = mBTLE_Service.getSupportedGattServices();

            for (BluetoothGattService service : servicesList) {

                addService(service);

                List<BluetoothGattCharacteristic> characteristicsList = service.getCharacteristics();
                ArrayList<BluetoothGattCharacteristic> newCharacteristicsList = new ArrayList<>();

                for (BluetoothGattCharacteristic characteristic: characteristicsList) {
                    addCharacteristic(characteristic);
                    newCharacteristicsList.add(characteristic);
                }
                addCharacteristicList(service.getUuid().toString(), newCharacteristicsList);
                //mHashMapBLECharacteristics.put(service.getUuid().toString(), newCharacteristicsList);
            }

            if (servicesList != null && servicesList.size() > 0) {
                // expandableListAdapter.notifyDataSetChanged();
             //   mListener.notifyServiceAdapter();
            }
        }

    }
    */

    public void connectToBle(String address){
        if(serviceBle !=null){
            serviceBle.connect(address);
        }
    }

    @Override
    public void onConnected() {
        mListener.bleConnected();
    }

    @Override
    public void onDisconnected() {
        if(serviceBle != null){
            serviceBle.disconnect();
            mListener.bleDisconnected();
        }

    }

    @Override
    public void onServicesDiscovered() {

        if (serviceBle != null) {

          //  getArrayListBLEServices().clear();
            //getHashMapCharacteristics().clear();
            // getHashMapBLEListCharacteristics().clear();

            List<BluetoothGattService> servicesList = serviceBle.getSupportedGattServices();

            for (BluetoothGattService service : servicesList) {

                addService(service);

                List<BluetoothGattCharacteristic> characteristicsList = service.getCharacteristics();
                ArrayList<BluetoothGattCharacteristic> newCharacteristicsList = new ArrayList<>();

                for (BluetoothGattCharacteristic characteristic: characteristicsList) {
                    addCharacteristic(characteristic);
                    newCharacteristicsList.add(characteristic);
                }
                addCharacteristicList(service.getUuid().toString(), newCharacteristicsList);
                //mHashMapBLECharacteristics.put(service.getUuid().toString(), newCharacteristicsList);
            }
           /*
            if(this.mHashMapBLECharacteristics != null &&  mHashMapBLECharacteristics.size()>0){
                this.notifyUpdateBleManager(KindItemData.ARRAY_BLE_CHARACTERISTICS);
            }
            */


            if (servicesList != null && servicesList.size() > 0) {
             //   mListener.notifyExpandableListAdapter();

                //this.notifyUpdateBleManager(KindItemData.ARRAY_BLE_SERVICES);

                this.mListener.onUpdateBleManager(KindItemData.ARRAY_BLE_SERVICES);

                //   mListener.notifyServiceAdapter();
            }
        }

    }
    /***/
    public void onCharacteristicsDiscovered() {

        if (serviceBle != null) {

            //  getArrayListBLEServices().clear();
            //getHashMapCharacteristics().clear();
            // getHashMapBLEListCharacteristics().clear();

            List<BluetoothGattService> servicesList = serviceBle.getSupportedGattServices();

            for (BluetoothGattService service : servicesList) {

                //addService(service);
                List<BluetoothGattCharacteristic> characteristicsList = service.getCharacteristics();
                ArrayList<BluetoothGattCharacteristic> newCharacteristicsList = new ArrayList<>();

                for (BluetoothGattCharacteristic characteristic: characteristicsList) {
                    addCharacteristic(characteristic);
                    newCharacteristicsList.add(characteristic);
                }
                addCharacteristicList(service.getUuid().toString(), newCharacteristicsList);
                //mHashMapBLECharacteristics.put(service.getUuid().toString(), newCharacteristicsList);
            }
          /*
            if(this.mHashMapBLECharacteristics != null &&  mHashMapBLECharacteristics.size()>0){
                this.notifyUpdateBleManager(KindItemData.ARRAY_BLE_CHARACTERISTICS);
            }
            */
            if (servicesList != null && servicesList.size() > 0) {
                 //mListener.notifyExpandableListAdapter();
                 //this.notifyUpdateBleManager(KindItemData.ARRAY_BLE_CHARACTERISTICS);
                 this.mListener.onUpdateBleManager(KindItemData.ARRAY_BLE_CHARACTERISTICS);
                 //   mListener.notifyServiceAdapter();
            }
        }

    }
    /***/

    @Override
    public void onCharacteristicReceive() {
        this.onCharacteristicsDiscovered();
    }

    public void onReadCharacteristics( BluetoothGattCharacteristic characteristic ){
        if (serviceBle != null) {
            serviceBle.readCharacteristic(characteristic);
        }
    }

    public void onSetCharacteristicNotification( BluetoothGattCharacteristic characteristic,boolean enabled){
        if (serviceBle != null) {
            serviceBle.setCharacteristicNotification(characteristic, enabled);
        }
    }


}
