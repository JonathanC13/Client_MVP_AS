package com.example.jonathan.client_mvp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import java.io.File;


import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Semaphore;

import android.widget.RelativeLayout.LayoutParams;
// todo 1.Check if connect and connected works. 2.need way to test read and write - Fill packet with valid data and send to a stub to print.
public class Data_Controller {

    Semaphore BTconnectedSem; // semaphore to block until connection to desired BT device is made before doing any transfers
    Semaphore BTadapterSem; // only allow one BT connection at a time

    // <File system info>
    private String curr_app_dir;
    private String image_folder;
    private String full_img_path;
    private String jpg_ext;
    private String closed_door;
    private String open_door;

    //private String locked_door_image;
    //private String open_door_path;
    // </File system info>

    private String employeeCard;
    private String sp_fail = "fail";

    private float scale;

    // List of floor/doors/button objects
    List<Data_Collection> flr_dr_class_list = new ArrayList<Data_Collection>();

    // pure string array to set the combobox(spinner) items
    private List<String> arr_flr_name = new ArrayList<String>();

    // List of placed doors
    private List<door_struct> placed_doors = new ArrayList<door_struct>();

    // ScrollView internal layout
    ConstraintLayout grd_scr;

    Context currContext;

    FloorActivity calling_FloorAct;
    Bluetooth_Device_management BT_devManage;

    private String mConnectedDeviceName = null;
    //private BlueTooth_service mTransferService = null;
    private BluetoothChatService mTransferService = null;
    BluetoothAdapter mBluetoothAdapter;
    private static final UUID MY_UUID_SECURE = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
            //"0000110a-0000-1000-8000-00805f9b34fb");


    public Data_Controller(String appDIR, ConstraintLayout grd_s, float IV_scale, Context cont, FloorActivity flrAct, Bluetooth_Device_management BT_dev_mng){
        BT_devManage = BT_dev_mng;
        //BTcurrentDevSem = new Semaphore(1);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BTadapterSem = new Semaphore(1, true);

        //Calling activity handles bluetooth
        calling_FloorAct = flrAct;
        //

        currContext = cont;

        grd_scr = grd_s;
        scale = IV_scale;

        //curr_app_dir = appDIR;
        image_folder = "/images/";
        full_img_path = appDIR;

        //closed_door = "/closed_door.png";
        //open_door = "/open_door.png";

        //locked_door_image = full_img_path + closed_door;
        //open_door_path = full_img_path + open_door;

        // get employee card
        final SharedPreferences sharedPref = cont.getSharedPreferences(cont.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        employeeCard = sharedPref.getString(cont.getString(R.string.card), sp_fail);

        if (employeeCard.equals(sp_fail)){
            // need to require user to input a card id manually in a alert box
        }

    }

    
    public void resetFloorList(){
        flr_dr_class_list = new ArrayList<Data_Collection>();
    }

    // Fill spinner, done in main activity, need context
    public void set_combobox_items(Context main, Spinner cmbo, ImageView imgV, Button upBtn, Button dnBtn){

        arr_flr_name = new ArrayList<String>();

        // fill an array with only the floor name, assuming already ordered from lowest to highest
        for(Data_Collection flr : flr_dr_class_list){
            //Log.v("TASK: ", flr.st_floor.getDisplayName());
            arr_flr_name.add(flr.st_floor.getDisplayName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(main, android.R.layout.simple_spinner_item, arr_flr_name);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner sItems = cmbo;
        sItems.setAdapter(adapter);
        sItems.setSelection(0); // default first

        // Set the initial floor plan
        try {
            String curr_flr = flr_dr_class_list.get(0).st_floor.getImgDir();

            File f = new File(curr_flr);
            //Log.v("TASK: ", curr_flr); // ~~~~~ WORKS UNTIL HERE, IMAGE NOT SHOWING, just may be incomplete coding and not error
            //if(f.exists()) {
              //  Log.v("TASK: ", "dir exists");
            //} else {
              //  Log.v("TASK: ", "dir not exists");
            //}
            // Retrieve image into bitmap
            Bitmap loaded = BitmapFactory.decodeFile(f.getAbsolutePath());
            //Log.v("TASK: ", "FILE SIZE: " + loaded.getByteCount());
            //Log.v("TASK: ", "HEIGHT: " + loaded.getHeight());

            // android:scaleType="center" , used for no scaling
            // set imageView properties
            //imgV.setScaleType(ImageView.ScaleType.CENTER);
            //imgV.getLayoutParams().height = RelativeLayout.LayoutParams.WRAP_CONTENT;
            //imgV.getLayoutParams().width = RelativeLayout.LayoutParams.WRAP_CONTENT;

            //imgV.getLayoutParams().height = 605;
            //imgV.getLayoutParams().width = 800;

            //get image properties
            int set_width = loaded.getWidth();
            int set_height = loaded.getHeight();

            //Log.v("TASK: ", "BEFORE W: " + imgV.getLayoutParams().width + " H: " + imgV.getLayoutParams().height);
            // set the image

            // imageView properties
            imgV.requestLayout();
            imgV.getLayoutParams().width = (int) (set_width * scale);
            imgV.getLayoutParams().height = (int) (set_height * scale);

            imgV.setImageBitmap(loaded);

            //Log.v("TASK: ", "AFTER W: " + imgV.getLayoutParams().width + " H: " + imgV.getLayoutParams().height);



        } catch (Exception e){
            //
        }

        // set down level button not clickable since this is the lowest floor
        dnBtn.setClickable(false);

        clear_prev_doors(main);

        place_curr_doors(0, main);
    }

    // Get the bitmapimage for the selected index of the spinner, also clear and set the doors
    public void updateFloor(Context main, int position, ImageView imgV){
        String loadFile =flr_dr_class_list.get(position).st_floor.getImgDir();

        // Retrieve image into bitmap
        Bitmap loadedBI = BitmapFactory.decodeFile(loadFile);

        //get image properties
        int set_width = loadedBI.getWidth();
        int set_height = loadedBI.getHeight();

        // set the image

        // imageView properties
        imgV.requestLayout();
        imgV.getLayoutParams().width = (int) (set_width * scale);
        imgV.getLayoutParams().height = (int) (set_height * scale);

        //Log.v("TASK: ", "AFTER W: " + imgV.getLayoutParams().width + " H: " + imgV.getLayoutParams().height);

        imgV.setImageBitmap(loadedBI);

        // clear prev doors
        clear_prev_doors(main);

        // Set current floor doors
        place_curr_doors(position, main);

    }

    public void emptyInfo(Context m, ImageView imgV, Spinner cmbo){

        imgV.setImageResource(R.drawable.default_floor_image);

        arr_flr_name = new ArrayList<String>();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(m, android.R.layout.simple_spinner_item, arr_flr_name);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner sItems = cmbo;
        sItems.setAdapter(adapter);

    }

    // Clear doors from previous floor
    public void clear_prev_doors(Context main){

        for(door_struct dr : placed_doors){
            ImageButton curr_btn = (ImageButton) dr.getImgBtn();
            grd_scr.removeView(curr_btn);

            // nullify Image button obj, garbage collected
            dr.removeImageButtonObj();
        }
    }

    private void place_curr_doors(int floor_sel, Context main){

        placed_doors = new ArrayList<door_struct>();

        // refresh doors test, the process for refreshing on floor change if needed
        /*
        DB_Controller refDB = new DB_Controller();
        refDB.refreshDoors();
        refDB.setDoors(flr_dr_class_list.get(floor_sel), flr_dr_class_list.get(floor_sel).st_floor.getDoorID());
        */

        int door_size = (int) (50 * scale);

        // Retrieve image into bitmap
        //Bitmap loaded = BitmapFactory.decodeFile(locked_door_image);
        //Drawable locked_dr_draw = Drawable.createFromPath(locked_door_image);
        //Bitmap loaded = BitmapFactory.decodeFile(locked_door_image);

        //
        int i = 0;
        if(flr_dr_class_list.size() != 0 ) {
            for (door_struct dr : flr_dr_class_list.get(floor_sel).arr_doors) {


                // <set button attributes>
                final ImageButton btn_new = new ImageButton(main);
                btn_new.setId(i);

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                );
                btn_new.setLayoutParams(params);

                // set size of button
                btn_new.requestLayout();
                btn_new.getLayoutParams().width = door_size;
                btn_new.getLayoutParams().height = door_size;

                // NEED TO SET CONSTRAINTS IN ORDER TO SET MARGINS
                ConstraintSet set1 = new ConstraintSet();
                set1.clone(grd_scr);    // get existing constraints into ConstraintSet

                set1.connect(btn_new.getId(), ConstraintSet.LEFT, R.id.grd_ScrollV, ConstraintSet.LEFT, 0);
                set1.connect(btn_new.getId(), ConstraintSet.TOP, R.id.grd_ScrollV, ConstraintSet.TOP, 0);

                // set image of closed door icon
                btn_new.setImageResource(R.drawable.closed_door);

                // Set position on the floor plan
                double top_marg = dr.getTop() * scale;
                double left_marg = dr.getLeft() * scale;
                btn_new.setTranslationY((float) top_marg);
                btn_new.setTranslationX((float) left_marg); // ONLY THIS WORK, setting margins didn't work with dynamically created

                // get the device name
                String drDev_name = dr.getDrName();

                //add button to the layout
                grd_scr.addView(btn_new);

                // generic button click
                btn_new.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {

                        generic_button_click(btn_new);
                    }
                });

                // </Set button attributes>


                dr.setImageButton(btn_new, i);
                placed_doors.add(dr);

                i++;
            }
        }

    }

    class doorClickThread extends Thread {
        private Thread t;
        private View currView;

        doorClickThread(View v) {
            currView = v;
        }

        public void start () {
            if (t == null) {
                t = new Thread (this);
                t.start ();
            }
        }

        public void run() {
            final ImageButton IB = (ImageButton) currView;

            door_struct clickedDoor = null;
            BluetoothDevice BT_dev = null;
            String button_IP = "";
            String devName = "";
            String devMAC = "";
            int devPort = -1;

            // search button_struct placed_doors() List for the door and get its IP
            for (door_struct btn : placed_doors) {

                if (IB.getId() == btn.getBtnID()) {
                    clickedDoor = btn;
                    BT_dev = btn.getBt_dev();
                    button_IP = btn.getDrIP();
                    devName = btn.getDev_remoteName();
                    devMAC = btn.getDev_MAC();
                    devPort = btn.getDoor_Port();
                    break;
                }
            }

            Log.v("TASK: ", button_IP);

            // continue if employee card not "fail" since the Pi should automatically reject a packet with no card
            if (employeeCard.equals(sp_fail) || button_IP.equals("") || devName.equals("") || devMAC.equals("") || devPort == -1){
                Log.v("Door click: ", "employee card was [" + employeeCard + "]. If [fail], then problem with employee DB or problem storing in app.");
                Log.v("Door click: ", "Door DB properties.");
                Log.v("Door click: ", "IP_door was [" + button_IP + "].");
                Log.v("Door click: ", "dev_remName was [" + devName + "].");
                Log.v("Door click: ", "dev_MAC was [" + devMAC + "].");
                Log.v("Door click: ", "dev_port was [" + devPort + "].");
            }
            else {
                //clickedBtn = IB;
                Log.v("Door click: ", "Printing properties needed to operate udp and Bluetooth transport:");
                Log.v("Door click: ", "Employee card: " + employeeCard);
                Log.v("Door click: ", "IP_door: " + button_IP);
                Log.v("Door click: ", "dev_remName: " + devName);
                Log.v("Door click: ", "dev_MAC: " + devMAC);
                Log.v("Door click: ", "dev_port: " + devPort);


/*
                // UDP works, comment out to test bluetooth. todo, test udp 1 more time (Test single, multiple simultaneous click on multiple button and on same button), then commit to master
                // udp send to open door and wait for receive message
                Log.v("RESPONSE: ", "START");
                //UDP_controller udpTask = new UDP_controller(button_IP, "RDR5, C6:I0:R2",65000, employeeCard);
                UDP_controller udpTask = new UDP_controller(button_IP, devName, devPort, employeeCard);

                int response = udpTask.executeUDP(); // Starts async task for udp operation
                Log.v("RESPONSE: ", "Data_Controller returned: " + response);
                // need to determine success code
                if(response == 1){
                    // change door icon to green
                    calling_FloorAct.iconOpenDoor(IB);
                } else {
                    // change to yellow to indicate problem and not open
                    calling_FloorAct.iconErrorDoor(IB);

                }

                // Depending on how we lock the door change this. This wait initially caused an error, crashes the app.
                setDoorClosed();
*/



                // BT test
                //String re_print = bt_testObj.refreshBT();
                //Log.v("BT: ", "onClick: " + re_print);
/*
                queuedBtns.add(IB); // queue for the buttons clicked if more clicked while a transfer is still in progress
                try {
                    BTcurrentDevSem.acquire();
                    if(queuedBtns.size() > 0) {
                        clickedBtn = queuedBtns.remove(0);
                    } else {
                        clickedBtn = IB;
                    }
                } catch (InterruptedException e){

                }
*/
                // todo, FIRST THING, test paired list, discovered list.
                // Test RPi connection, 1. turn on discoverable on RPi, 2. Start this app, it should try and auto pair.
                // 3. replace busy waiting for connected BT
                try {
                    Log.v("BT: ", "================ waiting for click sem");
                    BTadapterSem.acquire();
                } catch (InterruptedException e){
                    Log.v("BT: ", "click BT device sem interrupted. " + e.toString());
                }

                Boolean click_ret = false;

                // look in the paired list first
                click_ret = BT_devManage.checkPairedList(clickedDoor);

                if(click_ret == false) {
                    //if not paired already, need to search discovered devices, this requires waiting if the discovered list is being used
                    try {
                        BT_devManage.discoveredList_sem.acquire(); //
                        Log.v("BT: ", "sem onCLick got semaphore, should be delayed print");
                        click_ret = BT_devManage.checkAndAttemptPair(clickedDoor);

                    } catch (InterruptedException e) {
                        Log.v("BT: ", "sem onClick, could not lock discovered list: " + e.toString());
                        BTadapterSem.release();
                    }
                    BT_devManage.discoveredList_sem.release();
                } else {
                    // device already paired so cancel discovery in case it is enabled
                    if(mBluetoothAdapter.isDiscovering()){
                        mBluetoothAdapter.cancelDiscovery();
                    }
                }

                if(click_ret == false){
                    // if still false after looking through the current discovered list, it tries to scan one more time
                    // attmept 1 refresh to see if it can discover it.
                    BT_devManage.BT_refresh();
                    //semaphore for the discovered list so we wait until its populated before looking through it
                    try {
                        BT_devManage.discoveredList_sem.acquire();
                        Log.v("Sem: ", "onCLick got semaphore, should be delayed print");
                        click_ret = BT_devManage.checkAndAttemptPair(clickedDoor); // look through discovered devices and attempt to pair if found.
                    } catch (InterruptedException e){
                        Log.v("Sem: ", "onClick, could not lock discovered list: " + e.toString());
                        BTadapterSem.release();
                    }
                    BT_devManage.discoveredList_sem.release();
                }

                // checks if device was paired and able to communicate.
                if(click_ret == true) {
                    Semaphore sem_response = new Semaphore(1, true); // FIFO
                    // can send message
                    BluetoothDevice sendToDev = mBluetoothAdapter.getRemoteDevice(devMAC);
                    setupComms(sendToDev, MY_UUID_SECURE, sem_response); //MY_UUID_SECURE

                    // create message
                    AccReq_CreateReqPacket o_AccessReq = new AccReq_CreateReqPacket(button_IP, devName, devPort, employeeCard);
                    byte[] sendPacket = o_AccessReq.getReqMsg();

                    // attmept to send, response in handler.
                    // todo, comment out to test connection to RPi before doing any writing

                    try {
                        BTconnectedSem.acquire();
                    } catch (InterruptedException e) {
                        Log.v("BT: ", "sem for sending was interrupted");
                        BTadapterSem.release();
                    }
                    if (mTransferService.getState() == mTransferService.STATE_CONNECTED) {
                        //String message = "yeeeet";
                        //byte[] send = message.getBytes();
                        sendMessage(sendPacket);

                    }
                    BTconnectedSem.release();


                    try {
                        sem_response.acquire();
                        byte[] b_response = mTransferService.get_b_Msg();

                        mTransferService.stop();
                        BTadapterSem.release(); // once response sem is received, then the next BT device can do their connection.

                        if(b_response != null){

                            Log.v("BT: ", "sem response " + b_response.toString());
                            BT_analyze_response(b_response, IB);
                            Log.v("BT: ", "currentState: " + mTransferService.getState());
                        }
                    } catch (InterruptedException e) {
                        Log.v("BT: ", "sem for check response was interrupted");
                        mTransferService.stop();
                        BTadapterSem.release();
                    }
                    sem_response.release();

                    /*
                    final Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.v("BT: ", "IN wait");
                            // Do something after 5s = 5000ms
                            // change door to closed icon
                            calling_FloorAct.iconClosedDoor(IB);
                            //BTcurrentDevSem.release();
                            //mTransferService.stop();
                        }
                    }, 5000);
                    */

                    // need to stop after finished

                    // for testing stop the service here by stopping all threads


                } else {
                    // failed to pair and probably cannot send message
                    BTadapterSem.release(); // release to allow next BT request
                    calling_FloorAct.iconClosedDoor(IB);
                }


                // bt test

                // bt test 2
        /*
        BluetoothAdapter mAdapter2 = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice bt_dev2 = BT_dev;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        this.setupComms(bt_dev2, MY_UUID_INSECURE); // todo, confirm connection with the 2 android devices

         */


                // BT CONNECT TEST OLD ----------
/*
        //String device_name = "Hwa Chan (Galaxy Tab4)";
        String device_name = "123";
        UUID currUUID = null;

// todo, NOW WORKS ?????? SO DO THE CONNECTION TEST SATURDAY
        // WTF COME ON

        Log.v("TASK: ", "WHAT IS HAPPENING");
        BluetoothDevice fndDev = PairedSet.log(device_name);
        BluetoothAdapter mmBA = BluetoothAdapter.getDefaultAdapter();
        currUUID = PairedSet.getUUID(fndDev, mmBA);

        Log.v("TASK: ", "FOUND IT: " + fndDev.getName() + " : " + currUUID.toString());

        this.setupComms(fndDev, currUUID);
        */
/*
        BluetoothDevice foundDevice = bt_test.getDevice(device_name);
        if(foundDevice != null){
            //currUUID = bt_test.getUUID(foundDevice);
        }

        Log.v("TASK: ", "UUID: " + foundDevice.getName().toString());
  */
        /*
        // Initialize service to perform the bluetooth connection
        mTransferService = new BlueTooth_service(currContext, mHandler); // The door image will be updated based on response of remote device

        // Connect to the remote device
        mTransferService.connect(mDevice, currUUID);
        */


/*
        // <Bluetooth connection>
        // get the device name
        mConnectedDeviceName = "";
        for (button_struct btn : placed_doors) {
            if (btn.getID() == IB.getId()) {
                //devName = btn.getDrName();
                // test, hard code device name
                mConnectedDeviceName = "123";
                break;
            }
        }

        // look through paired devices set to see if it was paired
        int foundpaired = 1;
        UUID currUUID = null;
        for (BluetoothDevice discoveredDev : bt_test.pairedDevices) {
            if(discoveredDev.getName() == mConnectedDeviceName){
                foundpaired = 0;
                // >> get UUID
                // https://stackoverflow.com/questions/14812326/android-bluetooth-get-uuids-of-discovered-devices
                // get the device
                mDevice = discoveredDev;

                // get the UUID
                currUUID = bt_test.getUUID(discoveredDev);
                break;
            }
        }

        if (currUUID != null){
            //

            if(foundpaired == 1){
                // can do a rescan, but may not want to
                Log.v("TASK: ", "foundpaired = 1");
            }

            if (!currUUID.equals("") && currUUID != null){
                // >> initialize thread for connection for the specific device
                Log.v("TASK: ", "uuid found: " + currUUID.toString());

                // Initialize service to perform the bluetooth connection
                mTransferService = new BlueTooth_service(currContext, mHandler); // The door image will be updated based on response of remote device

                // Connect to the remote device
                mTransferService.connect(mDevice, currUUID);

                // read response from remote device
                boolean openFlag;
                openFlag = mTransferService.checkOpenDoor();
                if (openFlag == true){
                    // Make icon green to indicate open door
                } else {
                    // turn to yellow to indicate connection problem or did not open
                }

            }
        } else {
            // total failure.
            Log.v("TASK: ", "Total failure");
        }
*/

                // </Bluetooth connection>

            }
        }
    }


    private void generic_button_click(View v ) {
        doorClickThread T1 = new doorClickThread( v);
        T1.start();
    }


    public void setDoorClosed_timed(ImageButton clickedBtn){
        final ImageButton imgB = clickedBtn;
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Do something after 5s = 5000ms
                // change door to closed icon
                Log.v("BT: ", "after wait");
                calling_FloorAct.iconClosedDoor(imgB);
                //BTcurrentDevSem.release();
            }
        }, 5000);


    }

    public String getFullImgPath(){
        return full_img_path;
    }


    public void setupComms(BluetoothDevice btD, UUID btUUID, Semaphore sem_resp){
        BTconnectedSem = new Semaphore(1, true); // FIFO
        if(mBluetoothAdapter == null) {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        if(!mBluetoothAdapter.isEnabled()){
            // message for BT is currently disabled
        } else {
            //mTransferService = new BlueTooth_service(currContext, mHandler);
            mTransferService = new BluetoothChatService(currContext, mHandler, BTconnectedSem, sem_resp);
            mTransferService.connect(btD, true);
        }
    }

    // Sends message to the remote device
    // @param message a string of text to send
    public void sendMessage(byte[] message){

        // check if connection is active
        if (mTransferService.getState() != BlueTooth_service.STATE_CONNECTED){
            Log.v("BluetoothChatService", "BT: SendMessage fail due to not connected");
            return;
        }

        // Check that there's actually something to send
        if(message.length > 0){
            Log.v("BluetoothChatService", "BT: SendMessage");
            // Get the message bytes and tell the Bluetooth_service to write
            //byte[] send = message.getBytes();
            mTransferService.write(message);

        }
    }


    // analyze response for door access command
    private void BT_analyze_response(byte[] b_msg, ImageButton clickedBtn){
        Log.v("BT: ", "ANALYZE");


        int packetLen = b_msg.length;
        // // packet.getData and packet.getLength
        AccReq_AnalyzeResponse AR_AR = new AccReq_AnalyzeResponse(b_msg, packetLen);
        //int cmdCode = AR_AR.checkCmdSegment();
        int cmdCode = 4096;

        Log.v("BT: ", "int cmd: " + cmdCode);

        AccReq_Body AR_B = new AccReq_Body(b_msg);

        switch(cmdCode) // based on the message command, create a return message and send it
        {
            case AccReq_packet_props.CMD_OK:    // cmd 4096
                Log.v("RESPONSE: ", "BT transfer CMD_OK received: " + AccReq_packet_props.unpackByteArr(AR_B.getBodyFromMsg(packetLen)) + ": with " + packetLen + " bytes.");
                // This command indicates request accepted and door open
                calling_FloorAct.iconOpenDoor(clickedBtn);
                setDoorClosed_timed(clickedBtn);
                break;
            //return 1;

            case AccReq_packet_props.CMD_ERROR_ID_IDCOMPL_NOT_MATCH:
                Log.v("RESPONSE: ", "BT transfer CMD_ERROR_REMOTE_ACCESS_DENIED received: " + AR_B.getBodyFromMsg(packetLen) + ": with " + packetLen + " bytes.");
                calling_FloorAct.iconErrorDoor(clickedBtn);
                setDoorClosed_timed(clickedBtn);
                break;
            case AccReq_packet_props.CMD_ERROR_REMOTE_ACCESS_DENIED:
                Log.v("RESPONSE: ", "BT transfer CMD_ERROR_REMOTE_ACCESS_DENIED received: " + AR_B.getBodyFromMsg(packetLen) + ": with " + packetLen + " bytes.");
                calling_FloorAct.iconErrorDoor(clickedBtn);
                setDoorClosed_timed(clickedBtn);
                break;
            default:
                // no valid code from the response header

                break;
        }

        
    }


    // Create Handler that gets information back from the Bluetooth_service
    private final Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case Bluetooth_constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BlueTooth_service.STATE_CONNECTED:
                            Log.v("BluetoothChatService", "BT: Connected to: " + mConnectedDeviceName);

                            //BT_responseFlag = Bluetooth_constants.BT_Connected; // set flag to indicate successful connection
                            break;
                        case BlueTooth_service.STATE_CONNECTING:
                            Log.v("BluetoothChatService", "BT: Connecting");
                            break;
                        case BlueTooth_service.STATE_LISTEN:
                            // check next case
                        case BlueTooth_service.STATE_NONE:

                            Log.v("BluetoothChatService", "BT: Not connected");
                            break;

                        default:
                            break;

                    }
                    break;
                case Bluetooth_constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes
                    String writeMessage = new String(writeBuf); // this is what this class writes to remote device
                    Log.d("BluetoothChatService", "Data_controller: sending msg of: " + writeMessage);
                    break;
                case Bluetooth_constants.MESSAGE_READ:
                    byte[] readBuf = (byte[])msg.obj;
                    // construct a string from the valid bytes in the buffer
                    // this is the response from the remote device
                    String readMessage = new String(readBuf, 0, msg.arg1);

                    Log.v("TASK: ", "BT: The response from the remote device is: " + readMessage);

                    // analyze the contents and determine what to do
                    //BT_analyze_response(readBuf);
                    break;
                case Bluetooth_constants.MESSAGE_DEVICE_NAME:
                    mConnectedDeviceName = msg.getData().getString(Bluetooth_constants.DEVICE_NAME);
                    Log.v("TASK: ", "BT: Device name: " + mConnectedDeviceName);
                    break;
                case Bluetooth_constants.MESSAGE_TOAST:
                    break;

            }
        }
    };

    public void onDestroy() {

        if (mTransferService != null) {
            mTransferService.stop();
        }
    }
}
