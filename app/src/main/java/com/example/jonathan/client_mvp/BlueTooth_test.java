package com.example.jonathan.client_mvp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import java.util.Set;

public class BlueTooth_test extends Activity {

    private int REQUEST_ENABLE_BT = 0;

    public String bt_log;

    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    Set<BluetoothDevice> pairedDevices; // query list for discovered bluetooth devices

    BlueTooth_test() {

    }

    // Refresh
    // for test returns string of all queried devices
    public String refreshBT(){

        bt_log = "";

        if (mBluetoothAdapter == null) {
            // Device doesn't support Bluetooth
        }
        // else mBluetoothAdapter is assigned the device's Bluetooth adapter (bluetooth radio)
        else {
            // check if Bluetooth is not enabled
            if (!mBluetoothAdapter.isEnabled()) {
                // not enabled, turn on
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
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
            /* // discovery constant
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
