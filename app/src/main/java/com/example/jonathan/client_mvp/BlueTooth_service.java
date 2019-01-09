package com.example.jonathan.client_mvp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHealthAppConfiguration;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageButton;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BlueTooth_service {
    // todo, try and emulate bluetooth chat app example

    private static final String TAG = "MY_APP_DEBUG_TAG";

    // member fields
    private final BluetoothAdapter mBluetoothAdapter;
    private final Handler mHandler; // Handler that gets info from Bluetooth service // todo mhandler should be global so we it is available for all to read and write
    private BT_ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;
    private int mNewState;
    private UUID mUUID;

    // Door ID so it can be updated from here
    ImageButton doorImg;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;         // doing noting
    public static final int STATE_LISTEN = 1;       // now listening for incoming connection
    public static final int STATE_CONNECTING = 2;   //now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;    // now connected to a remote device

    public BlueTooth_service (Context context, Handler handler, ImageButton currDoorImg){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mNewState = mState;
        mHandler = handler;
        doorImg = currDoorImg;
    }

    /*
        Write to the ConnectedThread in an un-synchronized manner

        @param The bytes to write, out : byte[]
     */
    public void write(byte[] out){
        // create temporary object
        ConnectedThread r;

        // synchronize a copy of the ConnectedThread
        synchronized (this){
            if(mState != STATE_CONNECTED){
                return;
            }
            r = mConnectedThread;
        }
        // perform the write un-synchronized
        r.write(out);
    }

    public boolean checkOpenDoor(){

        boolean open_flag = false;
        // create temporary object
        ConnectedThread r;

        // synchronize a copy of the ConnectedThread
        synchronized (this){
            if(mState != STATE_CONNECTED){
                return false;
            }
            r = mConnectedThread;
        }
        // perform the write un-synchronized
        open_flag = r.checkResponse();
        return open_flag;
    }

    // <BT initialize connection>

    /*
        Start the ConnectThread to initiate a connection to a remote device.

        @param: device to connect to : BluetoothDevice
        @param: UUID of that device : UUID
     */
    public synchronized void connect(BluetoothDevice device, UUID dUUID){
        Log.v("TASK: ", "BTS: Attempt Connection to " + device + " with UUID of " + dUUID);

        // cancel any thread attempting to make a connection
        if(mState == STATE_CONNECTING){
            if(mConnectThread != null){
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if(mConnectedThread != null)
        {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new BT_ConnectThread(device, dUUID);
        mConnectThread.start();
    }

    // The thread
    private class BT_ConnectThread extends Thread{

        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public BT_ConnectThread(BluetoothDevice device, UUID currUUID) {
            BluetoothSocket tmp = null; // since mmSocket is final need a temp object.
            mmDevice = device;

            try {
                // Get a bluetoothsocket to connect with the given bluetooth device, the UUID must match the one in the server code
                tmp = device.createRfcommSocketToServiceRecord(currUUID); // on failure throws IOException
            } catch (IOException e){
                Log.v("TASK: ", "RFCOMM IO: " + e.toString());
            }
            mState = STATE_CONNECTING;
            mmSocket = tmp; // on success
        }

        // thread
        public void run(){
            // cancel discovery is active
            if(mBluetoothAdapter.isDiscovering()){
                mBluetoothAdapter.cancelDiscovery();
            }

            try {
                // try to connect to the remote device through the socket. This call blocks until it is successful or throws an exception, that's why we run this in a separate thread.
                mmSocket.connect();
            } catch (IOException connectException){
                Log.v("TASK: ", "SOCKET could not make connection, IO: " + connectException.toString());
                // it is unable to connect, have to close the socket and return
                try {
                    mmSocket.close();
                } catch (IOException closeException){
                    Log.v("TASK: ","SOCKET for client could not close, IO: " + closeException.toString());
                }
                return;

            }

            // If this section is reached then the connection attempt has succeeded. Perform work associated with the connection is a separate thread.
            //manageMyConnectedSocket(mmSocket)
            connected(mmSocket, mmDevice);
        }

        // manual close of client socket and causes the thread to finish.
        public void cancel(){
            try{
                mmSocket.close();
            } catch (IOException e){
                Log.v("TASK: ", "SOCKET manual client closure failed, IO: " + e.toString());
            }

        }

    }
    // </BT initialize connection>

    public synchronized int getState(){
        return mState;
    }

    // update the caller class with the current state
    private synchronized void updateCaller(){
        mState = getState();
        mNewState = mState;

        // give the new state to the Handler so the caller can update
        mHandler.obtainMessage(Bluetooth_constants.MESSAGE_STATE_CHANGE, mNewState, -1).sendToTarget();
    }

    private synchronized void doorOpenSuccess(){

    }

    // <Manage connection>

    /*
        Start the ConnectedThread to being managing a Bluetooth connection

        @param: socket for the connection : BluetoothSocket
        @param: device that is connected to : BLuetoothDevice
     */
    public synchronized  void connected(BluetoothSocket socket, BluetoothDevice device){
        Log.v("TASK: ", "BTS: Connected");

        //Cancel the thread that completed the connection
        if(mConnectThread != null){
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if(mConnectedThread != null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        // Send the name of the connected device back to the calling class to validate
        Message msg = mHandler.obtainMessage(Bluetooth_constants.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(Bluetooth_constants.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        // Update caller class
        // updateCaller();
    }

    // Thread
    private class ConnectedThread extends  Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // store for the stream

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because member streams are final
            try {
                tmpIn = socket.getInputStream();

            } catch (IOException e){
                Log.v(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e){
                Log.v(TAG, "Error occurred when creating output stream", e);
            }

            // on success assign the streams
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            mState = STATE_CONNECTED;
        }

        // Run thread
        public void run() {
            mmBuffer = new byte[1024]; // buffer size
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until exception occurs
            while(true){
                try {

                    // ON CONNECTION, KEEP THIS IF ANDROID IS EXPECTING MESSAGE FROM REMOTE DEVICE WHEN IT CONNECTS
                    //      ELSE THIS IS THE PROCESS TO READ FROM THE BUFFER
                    // Read from the InputStream
                    numBytes = mmInStream.read(mmBuffer);

                    // Send the obtained bytes to the UI activity
                    // Dont know what is happening here
                    // Dont understand Message Class
                    //  assuming its just a header or something with info that can be parsed
                    Message readMsg = mHandler.obtainMessage(Bluetooth_constants.MESSAGE_READ, numBytes, -1, mmBuffer);
                    readMsg.sendToTarget(); // dont know what this really does
                } catch (IOException e){
                    Log.v(TAG, "Input stream was disconnected", e);
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        // Which is the main activity??
        public void write(byte[] bytes){
            try {
                mmOutStream.write(bytes);

                // Share the message with the UI activity
                Message writtenMsg = mHandler.obtainMessage(Bluetooth_constants.MESSAGE_READ, -1, -1, mmBuffer);
                writtenMsg.sendToTarget();

            } catch (IOException e){
                Log.v(TAG, "Error occurred when sending data", e);

                // Send failure message back to the activity.
                Message writeErrorMsg = mHandler.obtainMessage(Bluetooth_constants.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast", "Couldn't send data to the other device");
                writeErrorMsg.setData(bundle);
                mHandler.sendMessage(writeErrorMsg);
            }
        }


        public byte[] read(){
            ConnectedThread r;

            byte[] buffer = new byte[1024];
            int numBytes;

            synchronized ( this){
                if(mState != STATE_CONNECTED){
                    return null;
                }
                r = mConnectedThread;

                try{
                    // Read from the input stream
                    numBytes = mmInStream.read(buffer);

                    // send the obtained bytes to the caller Activity
                    //mHandler.obtainMessage(Bluetooth_constants.MESSAGE_READ, numBytes, -1, buffer).sendToTarget();

                } catch(IOException e){
                    Log.v("TASK: ", "BTS: Could not read. " + e.toString());
                }
            }
            return buffer;
        }

        public boolean checkResponse(){

            byte[] readMsg = read();

            boolean open_flag = false;

            // analyze the message to see if it indicates that the door was opened

            return open_flag;
        }

        // call this method from the main activity to shut down the connection
        public void cancel(){
            try {
                mmSocket.close();
            } catch(IOException e){
                Log.v(TAG, "Could not close the connection socket", e);
            }
        }

    }
    // </Manage connection>

}
