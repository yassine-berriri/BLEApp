package com.example.bleapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.bleapp.R;
import com.example.bleapp.adapters.BleDevicesRecycleModelAdapter;
import com.example.bleapp.datas.BleManager;
import com.example.bleapp.datas.DataManager;
import com.example.bleapp.models.BleDevice;
import com.example.bleapp.services.BleService;

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener,
        DataManager.DataManagerListener,
        BleManager.BleManagerListener, BleDevicesRecycleModelAdapter.BleDevicesRecycleModelAdapterListener {
    private static String[] PermissionLocation = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH
    };
    private static String[] PermissionProximity = {
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
    };

    Button btnScan;
    private BleDevicesRecycleModelAdapter mBleDevicesRecycleModelAdapter;
    public static final int BTLE_SERVICES = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnScan = findViewById(R.id.button_main_scan);
        btnScan.setOnClickListener(this);
        EditText editTextSearch = findViewById(R.id.EditText_Main_Filter);

        RecyclerView recyclerView = findViewById(R.id.recyclerView_devices);
        recyclerView.setHasFixedSize(false);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mBleDevicesRecycleModelAdapter = new BleDevicesRecycleModelAdapter( MainActivity.this,MainActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mBleDevicesRecycleModelAdapter);

        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                BleManager.getInstance().stopScan();
                DataManager.getInstance().TextChanged(s.toString());
            }
        });
        DataManager.getInstance().TextChanged(editTextSearch.getText().toString());



    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_main_scan:
                if(BleManager.getInstance().isScanning())
                {
                    BleManager.getInstance().stopScan();
                    btnScan.setText("Start Scan");
                }
                else {
                    BleManager.getInstance().startScan();
                    btnScan.setText("Stop Scan");
                }
                break;
            default:
                break;
        }

    }

    public void onUpdateDataManager(DataManager.KindItemData kindItemData){
        if(kindItemData == DataManager.KindItemData.ARRAY_BLE_DEVICES){
            runOnUiThread(()->{
                this.mBleDevicesRecycleModelAdapter.forceNotifyDataSetChanged();
            });
        }
    }

    protected void onResume(){
        super.onResume();
        DataManager.getInstance().addDataManagerListener( this);
        BleManager.getInstance().setListener(this);
        //toDo askPermission
        if(askPermission== true)
        {
            askPermission = false;
            BleManager.getInstance().startScan();
        }
        BleManager.getInstance().startScan();
    }


    protected void onStop() {
        super.onStop();
        BleManager.getInstance().stopScan();
    }

    public void onBackPressed() {return ;}

    public void onDestroy(){super.onDestroy();}

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 2000) {

            BleManager.getInstance().startScan();
        }else if (requestCode == 2001) {

            BleManager.getInstance().startScan();
        }
    }

    boolean askPermission = false;



    public void requestPermission(){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (this.shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_SCAN)
                    && this.shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_CONNECT)
            ) {
                requestPermissions(PermissionProximity, 2000);
            } else {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Please active your proximity's permissions");
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        askPermission = true;
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                });

                builder.show();
            }
        }
        else{
            if (this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
                    && this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
                    && this.shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_ADMIN)
                    && this.shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH)
            ) {

                requestPermissions(PermissionLocation,2001);


            }else
            {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Please active your location's permissions");
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        askPermission = true;
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                });

                builder.show();
            }

        }
    }

    public void scanFailed(int errorCode){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Discovery onScanFailed: " + errorCode);
    }

    @Override
    public void bleConnected() {

    }

    @Override
    public void bleDisconnected() {

    }

    @Override
    public void onUpdateBleManager(BleManager.KindItemData k) {

    }

    public void bleNotFound() {
        //Utils.toast(getApplicationContext(), "BLE not supported");
        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
        builder.setMessage("BLE not supported");
    }
    public void requestUserLocation() {
        btnScan.setText("Start Scan");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Device location is turned off, turn on the device location , do you want to turn on Location ?");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();


    }
    @SuppressLint("MissingPermission")
    public void requestUserBluetooth(){
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivity(enableBtIntent);
    }

    @Override
    public void onBleStartError(BleManager.KindErrorBle k) {

        switch (k)
        {

            case BLE_NOT_FOUND:
                bleNotFound();
                break;
            case GPS_DISABLED:
                requestUserLocation();
                break;
            case BLE_DISABLED:
                requestUserBluetooth();
                break;
            case PERMISSION:
                requestPermission();
                break;
        }


    }


    @Override
    public void onCardClicked(BleDevice device) {
        String name = device.getName();
        String address = device.getAddress();
        Intent intent = new Intent(this, BleDetailsActivity.class);
        intent.putExtra(BleDetailsActivity.EXTRA_NAME, name);
        intent.putExtra(BleDetailsActivity.EXTRA_ADDRESS, address);
        startActivityForResult(intent,BTLE_SERVICES);
    }
}