package com.example.bleapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.example.bleapp.R;
import com.example.bleapp.adapters.BleDetailsExpandableModelAdapter;
import com.example.bleapp.datas.BleManager;
import com.example.bleapp.tools.Utils;

public class BleDetailsActivity extends AppCompatActivity implements
        ExpandableListView.OnChildClickListener,
        BleManager.BleManagerListener


{

    public static final String EXTRA_NAME = "BleDetailsActivity.NAME";
    public static final String EXTRA_ADDRESS = "BleDetailsActivity.ADDRESS";

    private BleDetailsExpandableModelAdapter expandableListAdapter;
  //  private BleDetailsRecycleModelAdapter mBleDetailsRecycleModelAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_details);
        Intent intent = getIntent();
        ExpandableListView expandableListView;
        String name = intent.getStringExtra(BleDetailsActivity.EXTRA_NAME);
        String address = intent.getStringExtra(BleDetailsActivity.EXTRA_ADDRESS);
        expandableListAdapter = new BleDetailsExpandableModelAdapter(this);
        expandableListView = findViewById(R.id.ExpandableListView_BleDetailsActivity_servicesList);
        expandableListView.setAdapter(expandableListAdapter);
        expandableListView.setOnChildClickListener(this);

      /*
        RecyclerView recyclerView = findViewById(R.id.recyclerView_services);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mBleDetailsRecycleModelAdapter = new BleDetailsRecycleModelAdapter( this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mBleDetailsRecycleModelAdapter);

       */

        ((TextView) findViewById(R.id.textView_BleDetailsActivity_BleName)).setText(name + "Services");
        ((TextView) findViewById(R.id.textView_BleDetailsActivity_BleAddress)).setText(address);
        BleManager.getInstance().connectToBle(address);
        BleManager.getInstance().setListener(this);
        BleManager.getInstance().onServicesDiscovered();
    }

    @Override
    protected void onResume() {
        super.onResume();
        BleManager.getInstance().setListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
       // BleManager.getInstance().addDataManagerListener( this);
        //BleManager.getInstance().setListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        BleManager.getInstance().onDisconnected();
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        BluetoothGattCharacteristic characteristic = BleManager.getInstance().getHashMapBLEListCharacteristics().get(
                        BleManager.getInstance().getArrayListBLEServices().get(groupPosition).getUuid().toString())
                .get(childPosition);

        if (Utils.hasWriteProperty(characteristic.getProperties()) != 0) {
           /* String uuid = characteristic.getUuid().toString();
            Dialog_BTLE_Characteristic dialog_btle_characteristic = new Dialog_BTLE_Characteristic();
            dialog_btle_characteristic.setTitle(uuid);
            dialog_btle_characteristic.setService(mBTLE_Service);
            dialog_btle_characteristic.setCharacteristic(characteristic);
            dialog_btle_characteristic.show(getFragmentManager(), "Dialog_BTLE_Characteristic");*/
        }
        else if (Utils.hasReadProperty(characteristic.getProperties()) != 0) {
            BleManager.getInstance().onReadCharacteristics(characteristic);
        }

        else if (Utils.hasNotifyProperty(characteristic.getProperties()) != 0) {
            //BleManager.getInstance().onReadCharacteristics(characteristic);
           BleManager.getInstance().onSetCharacteristicNotification(characteristic,true);

        }
        return false;
    }


    @Override
    public void onBleStartError(BleManager.KindErrorBle k) {

    }

    @Override
    public void bleNotFound() {

    }

    @Override
    public void scanFailed(int errorCode) {

    }

    @Override
    public void bleConnected() {

    }

    @Override
    public void bleDisconnected() {
        finish();

    }

    @Override
    public void onUpdateBleManager(BleManager.KindItemData k) {
        if(k == BleManager.KindItemData.ARRAY_BLE_SERVICES || k == BleManager.KindItemData.ARRAY_BLE_CHARACTERISTICS){
            runOnUiThread(()->{
                this.expandableListAdapter.forceNotifyDataSetChanged();
            });
        }
    }

}