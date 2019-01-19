package com.example.jonathan.client_mvp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.Set;
import java.util.UUID;

public class PairedDevices {
    Set<BluetoothDevice> pairedDevices; // query list for discovered bluetooth devices


    public PairedDevices(){

    }

    // initialize discovery
    public void initializeDiscovery(BluetoothAdapter mBluetoothAdapter) {
        // <Discover devices>
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        mBluetoothAdapter.startDiscovery(); // has a timer, base inquiry scan of about 12 seconds and then it does a page scan of each device found to retrieve its bluetooth information.
    }

    // Query all discovered devices and can do something with it.
    // for testing sets string for all discovered devices
    public String iterateBluetoothDevices(BluetoothAdapter mBluetoothAdapter) {
        // <Query snippet>
        String bt_log = "";
        if (mBluetoothAdapter != null) {
            //pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                // There are paired devices. Get the name and address of each paired device.
                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // the MAC address. All you need to initiate a connection with a Bluetooth device
                    // Unadvised to connect while performing device discovery since discovery uses a lot of the Bluetooth adapter's resources, use cancelDiscovery() to cancel
                    // Unadvised to initiate a discovery if there is a device connected because it will reduce the bandwidth available for the existing connections.
                    //Log.v("BT: ", "Query: Name: " + deviceName + ".MAC: " + deviceHardwareAddress);
                    bt_log += "Query: Name: " + deviceName + " .MAC: " + deviceHardwareAddress + "\n";
                }
            }
        }
        // </Query snippet>
        return bt_log;
    }

    public BluetoothDevice log(String name){

        BluetoothDevice foundDev = null;
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // the MAC address. All you need to initiate a connection with a Bluetooth device

                //Log.v("TASK: ", "CURR " + deviceName);
                if(deviceName.equals(name)) {
                    Log.v("TASK: ", "FOUND " + deviceHardwareAddress);
                    foundDev = device;
                    //BluetoothAdapter mm = BluetoothAdapter.getDefaultAdapter();
                    //UUID getDevUUID = getUUID(device, mm);
                    //Log.v("TASK: ", "FOUND UUID" + getDevUUID.toString());
                }
            }
        } else {
            Log.v("TASK: ", "EMPTY SET");
        }
        return foundDev;
    }

    public UUID getUUID(BluetoothDevice dev, BluetoothAdapter mBluetoothAdapter){
        UUID currUUID = null;
        if (dev != null) {
            // check if discovering
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
            // request the UUID from the device
            boolean res = dev.fetchUuidsWithSdp();
            ParcelUuid[] uuidExtra = dev.getUuids();


            for (ParcelUuid uuid : uuidExtra) {
                currUUID = uuid.getUuid();
            }

            if (currUUID == null) {
                Log.v("TASK: ", "UUID< NULL");
            }
        }
        return currUUID;
    }
}
