package com.example.jonathan.client_mvp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class BlueTooth_test extends Activity {

    private static final int REQUEST_ENABLE_BT = 0;

    public String bt_log;

    BluetoothAdapter mBluetoothAdapter;
    Set<BluetoothDevice> pairedDevices; // query list for discovered bluetooth devices

    BlueTooth_test() {

    }

    /*
    public void startBTconnectThread(BluetoothDevice device, UUID currUUID){
        BT_ConnectThread bt_t = new BT_ConnectThread(device, currUUID);
        bt_t.start();
    }
    */

    // Refresh
    // for test returns string of all queried devices
    public String refreshBT(){

        bt_log = "";
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Log.v("TASK: ", "RES, START");
        if (mBluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Log.v("TASK: ", "NULL BT ADAPTER");
        }
        // else mBluetoothAdapter is assigned the device's Bluetooth adapter (bluetooth radio)
        else {
            Log.v("TASK: ", "RES, NOT NULL");
            // check if Bluetooth is not enabled
            if (!mBluetoothAdapter.isEnabled()) {
                // not enabled, turn on
                Log.v("TASK: ", "RES, NOT ENABLED");
                // <Crashing>
                //Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                //startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                // </Crashing>
                bt_log += "Bluetooth not enabled!";
            } else {
                initializeDiscovery();
                // todo, after discovery all done the data structure should be populated with doors that are currently in range. In the Set<BluetoothDevice> pairedDevices;
                iterateBluetoothDevices(); // print in log to debug
            }
        }
        return bt_log;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        Log.v("TASK: ", "RES, INTENT");
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK){
                    //bluetooth is on
                    Log.v("TASK: ", "RES, ON");
                }
                else {
                    //user is denied turning on Bluetooth
                    Log.v("TASK: ", "RES, DENIED");
                }
                break;
            default:
                Log.v("TASK: ", "RES, ??");
                break;
        }
        Log.v("TASK: ", "RES, END 1");
        super.onActivityResult(requestCode, resultCode, data);
        Log.v("TASK: ", "RES, END 2");

    }

    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                // continue with SDP, Service Discovery Protocol to search the local area for Bluetooth devices and request some info about each one.
                // The other devices must be set to discoverable if they want to exchange data, and this app will only store info from discoverable devices that are valid doors to avoid pairing with irrelevant bluetooth enabled devices.
                // The saved data is device's name, class, and MAC address. todo, Need to determine what the admin configures to identify the doors on the floor plan
                // Just want the paired* list of devices, need to refresh at an interval if not automatic
                // When user wants access, it will connect*

                // Before discovery, need to query the set of paired devices to have a record of the already known devices.
                initializeDiscovery();

                // todo, after discovery all done the data structure should be populated with doors that are currently in range. In the Set<BluetoothDevice> pairedDevices;
                iterateBluetoothDevices(); // print in log to debug
                // todo, When a door icon is clicked this data structure is searched and a connection is established and the data is exchanged.
                // note, Android will probably be the client, since the door Pi will has a socket open and listening for requests.
                //      Client will initiate a connection using the server device's MAC address.
                // Connection technique could use RFCOMM, has 30 available ports, it acts like TCP (connection oriented).


                // </Discover devices>
            } else {
                // Refused bluetooth, limit application functionality
            }
        }
    }
    */

    // initialize discovery
    void initializeDiscovery() {
        // <Discover devices>
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        mBluetoothAdapter.startDiscovery(); // has a timer, base inquiry scan of about 12 seconds and then it does a page scan of each device found to retrieve its bluetooth information.
    }

    // Query all discovered devices and can do something with it.
    // for testing sets string for all discovered devices
    void iterateBluetoothDevices() {
        // <Query snippet>
        if (mBluetoothAdapter != null) {
            pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                // There are paired devices. Get the name and address of each paired device.
                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // the MAC address. All you need to initiate a connection with a Bluetooth device
                    // Unadvised to connect while performing device discovery since discovery uses a lot of the Bluetooth adapter's resources, use cancelDiscovery() to cancel
                    // Unadvised to initiate a discovery if there is a device connected because it will reduce the bandwidth available for the existing connections.
                    //Log.v("BT: ", "Query: Name: " + deviceName + ".MAC: " + deviceHardwareAddress);
                    bt_log += "Query: Name: " + deviceName + ".MAC: " + deviceHardwareAddress + "\n";
                }
            }
        }


        // </Query snippet>
    }

    public UUID getUUID(BluetoothDevice dev){

        // check if discovering
        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
        }
        // request the UUID from the device
        boolean res = dev.fetchUuidsWithSdp();
        ParcelUuid[] uuidExtra = dev.getUuids();
        UUID currUUID = null;

        for(ParcelUuid uuid : uuidExtra){
            currUUID = uuid.getUuid();
        }

        if(currUUID == null){
            Log.v("TASK: ", "UUID< NULL");
        }

        return currUUID;
    }

    // <Discover devices>
    @Override
    protected void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
    }


    // create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                // Discovery has found a device. Get the Bluetooth device
                // object and its info from intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.v("BT: ", "Discovered: Name: " + deviceName + ".MAC: " + deviceHardwareAddress);

                // Can save into a data structure here as they discover
            }
            /* // constant discovery loop
            else if (BluetoothDevice.ACTION_DISCOVERY_FINISHED.equals(action)){
                mBluetoothAdapter.startDiscovery();
            }

             */
        }
    };

    @Override
    protected void onDestroy(){
        super.onDestroy();

        // Must unregister the ACTION_FOUND receiver
        unregisterReceiver(mReceiver);
    }

    // </Discover devices>


}
