package com.example.bleapp.datas;

import android.os.AsyncTask;
import android.os.Handler;

import com.example.bleapp.models.BleDevice;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class DataManager {
    private static DataManager singleInstance;

    private DataManager() {
    }

    public static DataManager getInstance() {
        return singleInstance;
    }

    public static void initInstance() {
        if (singleInstance == null) {
            singleInstance = new DataManager();
        }
    }




    public enum KindItemData {
        ARRAY_BLE_DEVICES
    }

    public interface DataManagerListener {
        void onUpdateDataManager(KindItemData kindItemData);
    }

    public ArrayList<DataManagerListener> mListeners = new ArrayList<>();

    public void addDataManagerListener(DataManagerListener newDataManagerListener) {
        this.mListeners.add(newDataManagerListener);
    }

    public void removeDataManagerListener(DataManagerListener toRemoveDataManagerListener) {
        this.mListeners.remove(toRemoveDataManagerListener);
    }

    private void notifyUpdateDataManager(KindItemData kindItemData) {
        for (DataManagerListener listener : this.mListeners) {
            listener.onUpdateDataManager(kindItemData);
        }
    }

    //---

    private String stringSearch = null;
    private ArrayList<BleDevice> showListBTLEDevice;
    private ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
    private Object lock_map_ble_device = new Object();

    public ArrayList<BleDevice> getShowListBTLEDevice() {
        if (this.showListBTLEDevice == null)
        { this.showListBTLEDevice = new ArrayList<BleDevice>(BleManager.getInstance().getHashMapBLE_Devices().values()); }
        return this.showListBTLEDevice;
    }

    private void updateShowMapObject(List<BleDevice> listMapObjectModel){
        this.executor.execute(() -> {
            synchronized (lock_map_ble_device) {
                if (listMapObjectModel == null)
                {
                    this.showListBTLEDevice = new ArrayList<>();
                }
                else
                {
                    this.showListBTLEDevice = new ArrayList<>(listMapObjectModel);
                }
                this.notifyUpdateDataManager(KindItemData.ARRAY_BLE_DEVICES);
            }});
    }
    //-----

    private FilterMapObjectTask taskFilterMap = null;

    public void TextChanged(String filter) {
        if (filter == null) {
            filter = "";
        }
        this.stringSearch = filter;
        this.launchObjectTask();
    }

    private void runFilterMapObjectTaskNow() {
        if (this.taskFilterMap != null) {
            this.taskFilterMap.cancel(true);
        }
        this.taskFilterMap = new FilterMapObjectTask();
        this.taskFilterMap.execute(this.stringSearch);
    }

    private Runnable runnableLaunchObjectTask = () -> this.runFilterMapObjectTaskNow();

    private void launchObjectTask() {
        Handler getAppHandler = new Handler();
        getAppHandler.removeCallbacks(this.runnableLaunchObjectTask);
        getAppHandler.postDelayed(this.runnableLaunchObjectTask, 100);
    }

    //----

    private class FilterMapObjectTask extends AsyncTask<String, Void, List<BleDevice>>
    {
        @Override
        protected void onPostExecute(List<BleDevice> listMapObjectModel)
        {
            taskFilterMap = null;
            if (listMapObjectModel != null)
            {
                updateShowMapObject(listMapObjectModel);
            }
        }

        @Override protected List<BleDevice> doInBackground(String... params)
        {
            String filterString = null;
            if (params != null) {
                filterString = params[0];
            }
            if (BleManager.getInstance().getHashMapBLE_Devices() == null) {
                return null;
            }
            List<BleDevice> copyMapObjectList = new ArrayList<>();
            synchronized (lock_map_ble_device)
            {
                copyMapObjectList = new ArrayList<>(BleManager.getInstance().getHashMapBLE_Devices().values());
            }

            List<BleDevice> resultMapObjectList = new ArrayList<>();

            for (BleDevice mapObject : copyMapObjectList)
            {
                boolean canAdd = false;
                if (filterString == null || filterString.equals("")) {
                    canAdd = true;
                }
                else {
                    String nameObject = mapObject.getName().toUpperCase().trim();
                    nameObject = Normalizer.normalize(nameObject, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
                    nameObject = nameObject.replaceAll("[^a-zA-Z0-9]", " ");
                    filterString = filterString.toUpperCase().trim();
                    filterString = Normalizer.normalize(filterString, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
                    filterString = filterString.replaceAll("[^a-zA-Z0-9]", " ");
                    canAdd = nameObject.contains(filterString);
                }
                if (canAdd) {
                    resultMapObjectList.add(mapObject);
                }
            }

            return resultMapObjectList;

        }
    }

}
