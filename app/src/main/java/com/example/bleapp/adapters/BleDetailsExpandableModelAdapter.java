package com.example.bleapp.adapters;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.example.bleapp.R;
import com.example.bleapp.datas.BleManager;
import com.example.bleapp.services.BleService;
import com.example.bleapp.tools.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class BleDetailsExpandableModelAdapter extends BaseExpandableListAdapter {


    private static final String UUID_HEART_RATE_MEASUREMENT ="00002a37-0000-1000-8000-00805f9b34fb" ;
    private Context mContext;
    private ArrayList<BluetoothGattService> services_ArrayList;
    private HashMap<String, ArrayList<BluetoothGattCharacteristic>> characteristics_HashMap;

    public BleDetailsExpandableModelAdapter(Context context) {
        this.mContext = context;
        this.services_ArrayList = BleManager.getInstance().getArrayListBLEServices();
        this.characteristics_HashMap = BleManager.getInstance().getHashMapBLEListCharacteristics();
    }

    public void forceNotifyDataSetChanged() {
      //  this.services_ArrayList = BleManager.getInstance().getArrayListBLEServices();
        this.characteristics_HashMap = BleManager.getInstance().getHashMapBLEListCharacteristics();
        this.notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return services_ArrayList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return characteristics_HashMap.get(
                services_ArrayList.get(groupPosition).getUuid().toString()).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        if(services_ArrayList == null)
        {

        }
        return services_ArrayList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {

        return characteristics_HashMap.get(
                services_ArrayList.get(groupPosition).getUuid().toString()).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        BluetoothGattService bluetoothGattService = (BluetoothGattService) getGroup(groupPosition);
        String serviceUUID = bluetoothGattService.getUuid().toString();
        if (convertView == null) {
            LayoutInflater inflater =
                    (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.ble_service_list_item, null);
        }

        TextView tv_service = (TextView) convertView.findViewById(R.id.textView_BleDetails_uuidService);
        tv_service.setText("S: " + serviceUUID);

        return convertView;
    }


    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        BluetoothGattCharacteristic bluetoothGattCharacteristic = (BluetoothGattCharacteristic) getChild(groupPosition, childPosition);
        BluetoothGattDescriptor bluetoothGattDescriptor =   bluetoothGattCharacteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
         int heartRate;

        String characteristicUUID =  bluetoothGattCharacteristic.getUuid().toString();

        if (convertView == null) {
            LayoutInflater inflater =
                    (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.ble_characteristics_list_item, null);
        }

        TextView tv_service = (TextView) convertView.findViewById(R.id.TextView_BleDetails_characteristicUUID);
        TextView tv_value = (TextView) convertView.findViewById(R.id.TextView_BleDetails_characteristicValue);
        TextView tv_property = (TextView) convertView.findViewById(R.id.TextView_BleDetails_characteristicProperties);
        tv_service.setText("C: " + characteristicUUID);
        int properties = bluetoothGattCharacteristic.getProperties();
        StringBuilder sb = new StringBuilder();
        byte[] data = null;

        if (Utils.hasReadProperty(properties) != 0) {
            sb.append("R");
            data = bluetoothGattCharacteristic.getValue();
        }

        if (Utils.hasWriteProperty(properties) != 0) {
            sb.append("W");
        }

        if (Utils.hasNotifyProperty(properties) != 0) {
            sb.append("N");
            // data = bluetoothGattDescriptor.getValue();
             data = bluetoothGattCharacteristic.getValue();


        }
        if(data != null && bluetoothGattCharacteristic.getUuid().equals(UUID.fromString(UUID_HEART_RATE_MEASUREMENT))) {
            int flag = bluetoothGattCharacteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
            }
           // heartRate = bluetoothGattCharacteristic.getIntValue(format, 1);
          //  tv_value.setText("Heart rate measurement = " + heartRate);
        }

        tv_property.setText(sb.toString());


        /*
        if(data != null && bluetoothGattCharacteristic.getUuid().equals(UUID.fromString(UUID_HEART_RATE_MEASUREMENT))){
            if (data != null && data.length > 0) {
                int format = (data[0] & 0x01);
                int index = 1;
                int heartRate;
                if (format == 0) {
                    // 8-bit heart rate value
                     heartRate = data[index];
                } else {
                    // 16-bit heart rate value
                     heartRate = (data[index] & 0xff) | (data[index + 1] << 8);
                }
                // Do something with the heart rate value
                tv_value.setText("Heart rate measurement  = "+ heartRate);
            }

        }
         */

        if (data != null ) {
            tv_value.setText("Value: " + Utils.hexToString(data));
        }
        else {
            tv_value.setText("Value: click here");
        }

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
