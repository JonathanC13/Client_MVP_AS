package com.example.jonathan.client_mvp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

public class Bluetooth_Device_management {

    // Bluetooth discovery
    BluetoothAdapter mBluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices; // query list for paired bluetooth devices
    private Set<BluetoothDevice> discoveredDevices; // query list for discovered bluetooth devices
    protected Semaphore discoveredList_sem;

    // List of floor/doors/button objects
    List<Data_Collection> flr_dr_class_list = new ArrayList<Data_Collection>();

    public Bluetooth_Device_management(){
        discoveredList_sem = new Semaphore(1,true); //FIFO
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void setFlrList(List<Data_Collection> flrinfo){
        flr_dr_class_list = flrinfo;
    }

    // <Bluetooth>
    // Bluetooth refresh procedure
    public void BT_refresh(){
        if(mBluetoothAdapter == null){
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        // fill the Set for already paired devices
        pairedDevices = mBluetoothAdapter.getBondedDevices(); // Return the set of BluetoothDevice objects that are bonded (paired) to the local adapter. Stores them in Set<BluetoothDevice>
        // print for logging
        printPairedDevices();
        try {
            discoveredList_sem.acquire(); //try to lock discovered list
            initializeDiscovery(); // discovers devices, when it ends the pairedDevices set and discoveredDevices set are filled. Then we try to pair all relevant devices that are doors
        } catch (InterruptedException e){
            Log.v("Sem: ", "BT_refresh could not lock discovered list: " + e.toString());
        }
    }

    // Bluetooth discovery
    private void initializeDiscovery() {
        Log.v("BT: ", "<initializeDiscovery>");
        // <Discover devices>
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        // since async call, don't need a separate thread but do need semaphores on the data structures it uses
        mBluetoothAdapter.startDiscovery(); // has a timer, base inquiry scan of about 12 seconds and then it does a page scan of each device found to retrieve its bluetooth information.
        Log.v("BT: ", "</initializeDiscovery>");
    }



    // Intent for startDiscovery
    // create a BroadcastReceiver for ACTION_FOUND
    protected final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
                Log.v("BT: ", "<> Discovery started!");
                discoveredDevices = new HashSet<>();
            } else if(BluetoothDevice.ACTION_FOUND.equals(action)){
                // Discovery has found a device. Get the Bluetooth device
                // object and its info from intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.v("BT: ", "<> Discovered: Name: " + deviceName + ".MAC: " + deviceHardwareAddress);
                // Add the discovered device to the Set
                discoveredDevices.add(device);

                // Can save into a data structure here as they discover
            } else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                Log.v("BT: ", "<> Discovery ended!");

                if (mBluetoothAdapter != null) {
                    if (mBluetoothAdapter.isDiscovering()) {
                        Log.v("BT: ", "<iterateBluetoothDevices> Discovery cancel");
                        mBluetoothAdapter.cancelDiscovery();
                    }
                    //printDiscoveredDevices();

                    //
                    attemptPairAll();

                    discoveredList_sem.release(); // add to the list is complete, release lock
                }

            }
            /* // constant discovery loop
            else if (BluetoothDevice.ACTION_DISCOVERY_FINISHED.equals(action)){
                mBluetoothAdapter.startDiscovery();
            }

             */
        }
    };

    public boolean checkPairedList(door_struct dr){

        pairedDevices = mBluetoothAdapter.getBondedDevices();

        BluetoothDevice btDev_placeholder = null;
        BluetoothDevice currentBT_dev = dr.getBt_dev();
        String devMAC = dr.getDev_MAC();

        boolean pairedStatus = false;

        Log.v("BT START: ", "onClickBTcheck > Looking for device [" + currentBT_dev + "] or MAC [" + devMAC + "] in paired devices");

        if(currentBT_dev == null){
            // since null, need to check with MAC
            btDev_placeholder = checkPairedMAC(devMAC);
            if(btDev_placeholder != null){
                dr.setBT_device(btDev_placeholder);
                Log.v("BT START: ", "onClickBTcheck > Found device [" + btDev_placeholder + "] or MAC [" + devMAC + "] in paired devices through MAC");
                pairedStatus = true;
            } else {
                Log.v("BT START: ", "onClickBTcheck > Not found in paired list through MAC");
                pairedStatus = false;
            }
        } else {
            // if device is not null, means it may have been paired before.
            // need to confirm still paired

            pairedStatus = checkPairedDev(currentBT_dev);

            if (pairedStatus == false){
                // if cannot determine paired with the bluetooth device
                Log.v("BT START: ", "onClickBTcheck > Not found in paired through device");
                // can try with MAC
                btDev_placeholder = checkPairedMAC(devMAC);
                if(btDev_placeholder != null){
                    // if the device is found in the paired list through MAC
                    dr.setBT_device(btDev_placeholder);
                    Log.v("BT START: ", "onClickBTcheck > Found device [" + btDev_placeholder + "] or MAC [" + devMAC + "] in paired devices through MAC");
                    pairedStatus = true;
                } else {
                    // At this point, BT device not found in the paired list through BT device or MAC
                    Log.v("BT START: ", "onClickBTcheck > Not found in paired list through MAC or BT device");
                    pairedStatus = false;
                }
            } else {
                // the device is found in the paired list through BT device
                pairedStatus = true;
                Log.v("BT START: ", "onClickBTcheck > Found device [" + currentBT_dev + "] or MAC [" + devMAC + "] in paired devices, through Device");
            }
        }
        return pairedStatus;
    }

    public boolean checkDiscoveredList(door_struct dr){
        BluetoothDevice btDev_placeholder = null;
        BluetoothDevice currentBT_dev = dr.getBt_dev();
        String devMAC = dr.getDev_MAC();

        boolean foundInDis = false;

        Log.v("BT START: ", "onClickBTcheck > Looking for device [" + currentBT_dev + "] or MAC [" + devMAC + "] in discovered devices");
        // if not paired attempt to look in discovered and pair
        if(currentBT_dev == null){
            // have to use MAC
            btDev_placeholder = checkDiscMAC(devMAC);
            if(btDev_placeholder != null){
                // if the device is found in the discovered list through MAC
                dr.setBT_device(btDev_placeholder);
                Log.v("BT START: ", "onClickBTcheck > Found device [" + btDev_placeholder + "] or MAC [" + devMAC + "] in discovered devices through MAC");
                foundInDis = true;
            } else {
                // Not found through MAC
                Log.v("BT START: ", "onClickBTcheck > Not found through MAC");
                foundInDis = false;
            }
        } else {
            // use BT device since it is available
            boolean pair_ret = checkDiscoveredDev(currentBT_dev);

            if (pair_ret == false){
                // not found in discovered list through Device
                Log.v("BT START: ", "onClickBTcheck > Not found in discovered through device");

                // can try with MAC
                btDev_placeholder = checkDiscMAC(devMAC);
                if(btDev_placeholder != null){
                    // if found in discovered list with MAC
                    dr.setBT_device(btDev_placeholder);
                    Log.v("BT START: ", "onClickBTcheck > Found device [" + btDev_placeholder + "] or MAC [" + devMAC + "] in discovered devices through MAC");
                    foundInDis = true;
                } else {
                    // Not found in discovered list through MAC or device
                    Log.v("BT START: ", "onClickBTcheck > Not found in discovered through MAC or device");
                    foundInDis = false;
                }
            } else if(pair_ret == true){
                // device found in discovered list through device
                dr.setBT_device(currentBT_dev);
                Log.v("BT START: ", "onClickBTcheck > Found device [" + currentBT_dev + "] or MAC [" + devMAC + "] in discovered devices through device");
                foundInDis = true;
            }
        }

        Log.v("BT START: ", "==");
        return foundInDis;
    }

    // operation:int, is to determine what function to perfrom, if 0 only scan and validate, if 1 scan, validate, and then attempt to send message
    // todo split checkAndAttemptPair into methods of checkPairedList and checkDiscoveredList
    /*  Checks if the device is paired already
            if not, then look in discovered list and if it is there then attempt to pair
                if successful pair then return true, if failed to bond or not found in discovered then return false.
     */
    public boolean checkAndAttemptPair(door_struct dr){

        Log.v("BT START: ", "==");
        Log.v("BT START: ", "onClickBTcheck > Start" );

        if (mBluetoothAdapter.isDiscovering()) {
            Log.v("BT: ", "<attemptPair> Discovery cancel");
            mBluetoothAdapter.cancelDiscovery();
        }

        // check paired list
        boolean pairedStatus = checkPairedList(dr);


        // discovered list section
        Log.v("BT START: ", "> Discovery queue ><");
        boolean foundInDis = false;
        // if paired, message can be sent, return true;
        if(pairedStatus == true){

            Log.v("BT START: ", "onClickBTcheck > PAIRED already, MAC [" + dr.getDev_MAC() + "] in paired devices");
            // return true since already paired and don't need to look through discovered devices
            return true;

        } else {
            foundInDis = checkDiscoveredList(dr);
        }

        // bonding
        if (foundInDis == true){
            // if true, it means that the device is in the discovered list and needs to be paired
            BluetoothDevice btDev_placeholder = dr.getBt_dev();
            Boolean createdPair = false;
            try {
                createdPair = createBond(btDev_placeholder);
            } catch(Exception e){}

            Log.v("BT START: ", "onClickBTcheck > paired end with bond result " + createdPair );

            return checkPairedList(dr); // instead of returning result of bonded (even when the RPi is already paired, it appears in the discovered list again and causes an additional bond attempt which will return false), do a check through paired devices

        } else {
            // if was not found in discovered list, then return false
            Log.v("BT START: ", "onClickBTcheck > not found end ><" );
            return false;
        }
    }

    private boolean checkDiscoveredDev(BluetoothDevice bt_dev){

        return (discoveredDevices.contains(bt_dev));

    }

    private boolean checkPairedDev(BluetoothDevice bt_dev){
        return (pairedDevices.contains(bt_dev));
    }

    // looks through paired doors and checks if it was paired, if not then looks through discovered devices.
    public void attemptPairAll(){
        for (Data_Collection flr: flr_dr_class_list){
            for (door_struct dr : flr.arr_doors){
                checkAndAttemptPair(dr);
            }
        }
    }

    public BluetoothDevice checkDiscMAC(String devMAC){
        if (mBluetoothAdapter.isDiscovering()) {
            Log.v("BT: ", "<check Discovered MAC> Discovery cancel");
            mBluetoothAdapter.cancelDiscovery();
        }
        if(discoveredDevices != null) {
            for (BluetoothDevice btDev : discoveredDevices) {
                if (devMAC.equals(btDev.getAddress())) {
                    return btDev;
                }
            }
        }
        return null;
    }

    public BluetoothDevice checkPairedMAC(String devMAC){

        if(pairedDevices != null) {
            for (BluetoothDevice btDev : pairedDevices) {
                if (devMAC.equals(btDev.getAddress())) {
                    return btDev;
                }
            }
        }
        return null;
    }

    public boolean createBond(BluetoothDevice btDevice)
            throws Exception
    {
        Class class1 = Class.forName("android.bluetooth.BluetoothDevice");
        Method createBondMethod = class1.getMethod("createBond");
        Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }

    //print Paired devices
    private void printPairedDevices(){
        if(pairedDevices != null) {
            if (pairedDevices.size() > 0) {
                // There are paired devices. Get the name and address of each paired device.
                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // the MAC address. All you need to initiate a connection with a Bluetooth device
                    // Unadvised to connect while performing device discovery since discovery uses a lot of the Bluetooth adapter's resources, use cancelDiscovery() to cancel
                    // Unadvised to initiate a discovery if there is a device connected because it will reduce the bandwidth available for the existing connections.
                    //Log.v("BT: ", "Query: Name: " + deviceName + ".MAC: " + deviceHardwareAddress);
                    Log.v("BT: ", "Paired - Query paired: Name: " + deviceName + ". MAC: " + deviceHardwareAddress + "\n");
                }
            }
        }
    }

    private void printDiscoveredDevices(){
        if(discoveredDevices != null) {
            if (discoveredDevices.size() > 0) {
                // There are paired devices. Get the name and address of each paired device.
                for (BluetoothDevice device : discoveredDevices) {
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // the MAC address. All you need to initiate a connection with a Bluetooth device
                    Log.v("BT: ", "Discovered - Name: " + deviceName + ". MAC: " + deviceHardwareAddress + "\n");
                }
            }
        }
    }

}
