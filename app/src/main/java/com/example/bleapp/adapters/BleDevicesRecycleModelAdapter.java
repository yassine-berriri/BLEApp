package com.example.bleapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bleapp.R;
import com.example.bleapp.datas.DataManager;
import com.example.bleapp.models.BleDevice;

import java.util.ArrayList;

public class BleDevicesRecycleModelAdapter extends RecyclerView.Adapter<BleDevicesRecycleModelAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<BleDevice> devices;
    private BleDevicesRecycleModelAdapterListener mListener;

    public BleDevicesRecycleModelAdapter(Context context, BleDevicesRecycleModelAdapterListener listener ){
        this.mListener = listener;
        this.mContext = context;
        this.devices = DataManager.getInstance().getShowListBTLEDevice();
    }

    public BleDevice getItem(int position)
    {
        if(this.devices == null)
        {
            this.devices = new ArrayList<>();
        }
        return this.devices.get(position);
    }

    public void forceNotifyDataSetChanged(){
        if(this.devices == null){
            this.devices = new ArrayList<>();
        }
        this.devices = DataManager.getInstance().getShowListBTLEDevice();
        this.notifyDataSetChanged();
    }


    @NonNull
    @Override
    public BleDevicesRecycleModelAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater =(LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View bleDeviceView = inflater.inflate(R.layout.ble_device_list_item,parent,false);
        ViewHolder viewHolder = new ViewHolder(bleDeviceView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull BleDevicesRecycleModelAdapter.ViewHolder holder, int position) {
        BleDevice mBleDevice = this.getItem(position);
        TextView textViewMacAddress = holder.textViewMacAddress;
        TextView textViewRssi = holder.textViewRSSI;
        TextView textViewName = holder.textViewName;
        textViewMacAddress.setText(mBleDevice.getAddress());
        textViewRssi.setText(String.valueOf(mBleDevice.getRSSI()));
        textViewName.setText(mBleDevice.getName());
        holder.cardViewDevice.setOnClickListener((view) ->{
            this.mListener.onCardClicked(mBleDevice);
        });
    }

    @Override
    public int getItemCount() {
        return DataManager.getInstance().getShowListBTLEDevice().size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewMacAddress;
        TextView textViewRSSI;
        TextView textViewName;
        CardView cardViewDevice;
        public ViewHolder(View itemView){
            super(itemView);
            textViewMacAddress = itemView.findViewById(R.id.textView_bleDevice_macAddress);
            textViewRSSI = itemView.findViewById(R.id.textView_bleDevice_rssi);
            textViewName = itemView.findViewById(R.id.textView_bleDevice_name);
            cardViewDevice = itemView.findViewById(R.id.CardView_device_detail);

        }
    }

    public interface BleDevicesRecycleModelAdapterListener {
        void onCardClicked(BleDevice device);
    }

}
