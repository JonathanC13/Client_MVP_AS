package com.example.jonathan.client_mvp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Semaphore;

public class FloorActivity extends AppCompatActivity {


    // Bluetooth discovery
    private String mConnectedDeviceName = null;
    private BlueTooth_service mTransferService = null;
    BluetoothAdapter mBluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices; // query list for paired bluetooth devices
    private Set<BluetoothDevice> discoveredDevices; // query list for discovered bluetooth devices
    public Semaphore discoveredList_sem;

    private ImageView main_img;
    private Data_Controller dataPull;
    private ConstraintLayout grd_scr;
    private float scale;

    private String cardID;
    private String intStorageDirectory;

    Context context = FloorActivity.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_floor);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        discoveredList_sem = new Semaphore(1,true); //FIFO
        dataPull = null;

        String s_fail = "fail";
        String s_copyEmpty = "fail";

        final SharedPreferences server_sharedPref = context.getSharedPreferences(context.getString(R.string.preference_server_key), Context.MODE_PRIVATE);
        String copyIP_flag = server_sharedPref.getString(getString(R.string.IPlabel), s_copyEmpty);
        String copyPt_flag = server_sharedPref.getString(getString(R.string.Portlabel), s_copyEmpty);

        // If any of them empty, means need to copy server info from sign in file
        if(copyIP_flag.equals(s_copyEmpty) || copyPt_flag.equals(s_copyEmpty)) {

            // copy server info from sign in shared preference to one meant for server configuration
            final SharedPreferences signin_sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            String sh_autoflag = signin_sharedPref.getString("auto_flag", s_fail);

            String signin_IP = signin_sharedPref.getString(context.getString(R.string.IPlabel), s_fail);
            String signin_Pt = signin_sharedPref.getString(context.getString(R.string.Portlabel), s_fail);


            SharedPreferences.Editor editor = server_sharedPref.edit();
            editor.putString(getString(R.string.IPlabel), signin_IP);
            editor.putString(getString(R.string.Portlabel), signin_Pt);
            editor.apply();
        }

        Toolbar floorHeader = (Toolbar) findViewById(R.id.toolbar);
        String curr_server = "Server: " + copyIP_flag + ":" + copyPt_flag;
        floorHeader.setTitle(curr_server);

        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */

        // set bluetooth adapter.
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // Register for broadcasts when a device is discovered.
        // <intent>
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);
        // </intent>

        scale = getResources().getDisplayMetrics().density;
        final Spinner s_items = (Spinner) findViewById(R.id.spn_lvls);
        final Button upBtn = (Button) findViewById(R.id.btn_lvlUp);
        final Button dnBtn = (Button) findViewById(R.id.btn_lvlDn);
        main_img = (ImageView) findViewById(R.id.img_floor);
        grd_scr = (ConstraintLayout) findViewById(R.id.grd_ScrollV); //grid for door margins to anchor

        // test
        //Button testBtn = (Button) findViewById(R.id.button);

        // ADDING CONSTRAINT ON EXISTING PLACED bUTtoN WORKS
        //ConstraintSet set = new ConstraintSet();
        //set.clone(grd_scr);

        // pin to the bottom of the container
        //set.connect(R.id.button, ConstraintSet.LEFT, R.id.grd_ScrollV, ConstraintSet.LEFT, 50);
        //set.connect(R.id.button, ConstraintSet.TOP, R.id.grd_ScrollV, ConstraintSet.TOP, 50);

        // Apply the changes
        //set.applyTo(grd_scr);
        //

        //testBtn.requestLayout();
        //((ViewGroup.MarginLayoutParams) testBtn.getLayoutParams()).leftMargin = 50;
        //((ViewGroup.MarginLayoutParams) testBtn.getLayoutParams()).topMargin = 50;


        // TEST 2
        /*
        Button btnTag = new Button(this);
        int newID = 100;
        btnTag.setId(newID);


        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        btnTag.setLayoutParams(params);
        // set size of button
        btnTag.requestLayout();
        btnTag.getLayoutParams().width = (int) (50 *scale);
        btnTag.getLayoutParams().height = (int) (50 *scale);
        btnTag.setBackgroundColor(Color.BLUE);

        // NEED TO SET CONSTRAINTS IN ORDER TO SET MARGINS
        ConstraintSet set1 = new ConstraintSet();
        set1.clone(grd_scr);    // get existing constraints into ConstraintSet

        //set1.constrainWidth(btnTag.getId(), ConstraintSet.WRAP_CONTENT);
        //set1.constrainHeight(btnTag.getId(), ConstraintSet.WRAP_CONTENT);

        set1.connect(btnTag.getId(), ConstraintSet.LEFT, R.id.grd_ScrollV, ConstraintSet.LEFT, 0);
        set1.connect(btnTag.getId(), ConstraintSet.TOP, R.id.grd_ScrollV, ConstraintSet.TOP, 0);
        //set1.center(btnTag.getId(), ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 0, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 0, 0.5f);
        //set1.center(btnTag.getId(), ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0, 0.5f);

        // Apply the changes
        //set1.applyTo(grd_scr);
        //

        //btnTag.requestLayout();
        //((ViewGroup.MarginLayoutParams) btnTag.getLayoutParams()).leftMargin = 0;
        //((ViewGroup.MarginLayoutParams) btnTag.getLayoutParams()).topMargin = 150;

        btnTag.setTranslationY(50 * scale);
        btnTag.setTranslationX(50 * scale); // ONLY THIS WORKS, doesnt need to set parent
        //add button to the layout
        grd_scr.addView(btnTag);
        */
        // ====

        // get directory of app in internal storage
        File main_dir = this.getFilesDir();
        String app_dir = main_dir.getPath();
        //Log.v("TASK: ", "appdir " + app_dir);
        //String app_dir = this.uri.toString();

        //create directory to save images
        intStorageDirectory = getFilesDir().toString();
        //String save_img_folder = intStorageDirectory + "/images/";
        //File folder = context.getDir(save_img_folder, Context.MODE_PRIVATE); // should create private directory.
        File folder = new File(intStorageDirectory, "images");
        folder.mkdirs();
        //Log.v("TASK: ", "internal " + intStorageDirectory);
        //Log.v("TASK: ", "new " + folder.getName());

        //String save_img_folder = intStorageDirectory + "/images";
        //File comon = new File(save_img_folder);
        //if(comon.exists()){
        //  Log.v("TASK: ", "initial: " + save_img_folder);
        //}

        // datapull Object that controls the Data
        this.refreshData();

        // Listeners below

        // spinner listener
        s_items.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                int max_ind = dataPull.flr_dr_class_list.size();

                if(max_ind > 0) {
                    if (position >= max_ind - 1) {
                        upBtn.setClickable(false);
                        dnBtn.setClickable(true);
                    } else if (position <= 0) {
                        upBtn.setClickable(true);
                        dnBtn.setClickable(false);
                    }

                    spn_lvlsSelectionChanged(position);
                } else {
                    upBtn.setClickable(false);
                    dnBtn.setClickable(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        // Listener for Up Level button
        upBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int cur_sel = s_items.getSelectedItemPosition();
                int new_sel = cur_sel + 1;

                int max_ind = dataPull.flr_dr_class_list.size();

                if(max_ind > 0) {
                    if (new_sel >= max_ind - 1) {
                        new_sel = max_ind - 1;

                        upBtn.setClickable(false);
                    }
                    if (new_sel > 0) {
                        // since increase, make button clickable
                        dnBtn.setClickable(true);
                    }

                    // set spinner item
                    s_items.setSelection(new_sel);

                    upBtn_onClick(new_sel);
                } else {
                    upBtn.setClickable(false);
                    dnBtn.setClickable(false);
                }
            }
        });

        // Listener for Down Level button
        dnBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int cur_sel = s_items.getSelectedItemPosition();
                int new_sel = cur_sel - 1;
                int max_id = dataPull.flr_dr_class_list.size();

                if(max_id > 0) {
                    if (new_sel <= 0) {
                        new_sel = 0;
                        dnBtn.setClickable(false);

                    }
                    if (new_sel < max_id) {
                        // since decrease, make button clickable
                        upBtn.setClickable(true);

                    }
                    // set spinner item
                    s_items.setSelection(new_sel);

                    dnBtn_onClick(new_sel);
                } else {
                    upBtn.setClickable(false);
                    dnBtn.setClickable(false);
                }
            }
        });



        // <Initial Bluetooth scan>
        this.BT_refresh();
        // </Initial Bluetooth scan>


    }


    public void refreshData(){
        // clear current doors to clean diaplay
        if(dataPull != null) {
            dataPull.clear_prev_doors(this);
        }

        dataPull = new Data_Controller(intStorageDirectory, grd_scr, scale, context, this);
        DB_Controller DB_con_con = new DB_Controller(this, dataPull);

        Spinner s_items1 = (Spinner) findViewById(R.id.spn_lvls);
        Button upBtn1 = (Button) findViewById(R.id.btn_lvlUp);
        Button dnBtn1 = (Button) findViewById(R.id.btn_lvlDn);

        // if empty, disable both buttons
        if (dataPull.flr_dr_class_list.size() > 0) {
            // Fill spinner, need to pass context to be able to fill it.
            dataPull.set_combobox_items(this, s_items1, main_img, upBtn1, dnBtn1); // <------- IN HERE, IMAGE SETTING
        } else {

            final Spinner s_items2 = (Spinner) findViewById(R.id.spn_lvls);
            dataPull.emptyInfo(context, main_img, s_items2);

            upBtn1.setClickable(false);
            dnBtn1.setClickable(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.config){

            final Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.dialog_changeserver);
            dialog.setTitle("Change server settings");

            Button btn_apply = (Button) dialog.findViewById(R.id.btn_apply);
            btn_apply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    EditText new_IP = (EditText) dialog.findViewById(R.id.et_defIP);
                    EditText new_Port = (EditText) dialog.findViewById(R.id.et_defPort);
                    String get_newIP = new_IP.getText().toString();
                    String get_newPort = new_Port.getText().toString();

                    String s_displayServer = "Server: " + get_newIP + ":" + get_newPort;
                    Toolbar tb_header = (Toolbar) findViewById(R.id.toolbar);
                    tb_header.setTitle(s_displayServer);

                    // Refresh data with new server data
                    // Write new info to shared preference file then execute refresh
                    final SharedPreferences server_sharedPref = context.getSharedPreferences(context.getString(R.string.preference_server_key), Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = server_sharedPref.edit();
                    editor.putString(getString(R.string.IPlabel), get_newIP);
                    editor.putString(getString(R.string.Portlabel), get_newPort);
                    editor.apply();

                    // refresh with new server
                    refreshData();
                    dialog.dismiss();

                }
            });

            Button btn_dismiss = (Button) dialog.findViewById(R.id.btn_dismiss);
            btn_dismiss.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });

            dialog.show();

        }
        else if(item.getItemId() == R.id.refresh){
            this.refreshData();
        }
        else if(item.getItemId() == R.id.signout){

            // On sign out may need to clear current Server IP and port for the floors and doors. Right now it keeps the previously set.

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage("Are you sure you would like to sign out?").setTitle("Sign out")
            // buttons
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // clear shared preference file
                    SharedPreferences sharedPref = context.getSharedPreferences(
                            getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.clear();
                    editor.commit();

                    // Open new login activity
                    Intent main_intent = new Intent(FloorActivity.this, LogIn_oneTIme.class);
                    // Need to pass Card number to next activity, since it is used to open doors
                    //main_intent.putExtra("CardID", em_card); // don't need if save card number in a shared preference.
                    startActivity(main_intent);
                    // Remove activity from back stack
                    finish(); // clear current data.
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();

        } else if(item.getItemId() == R.id.bt_test){
            this.BT_refresh();
        }

        return super.onOptionsItemSelected(item);
    }

    public void spn_lvlsSelectionChanged(int sel_index){
        dataPull.updateFloor(this, sel_index, main_img);
    }

    public void upBtn_onClick(int curr_sel){


        dataPull.updateFloor(this, curr_sel, main_img);


    }

    public void dnBtn_onClick(int curr_sel){
        dataPull.updateFloor(this, curr_sel, main_img);


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
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
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
        for (Data_Collection flr: dataPull.flr_dr_class_list){
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




    @Override
    protected void onDestroy(){
        super.onDestroy();

        // Must unregister the ACTION_FOUND receiver
        unregisterReceiver(mReceiver);
        dataPull.onDestroy(); // stop all BluetoothChatService threads
    }

    public void iconOpenDoor(ImageButton IB){
        final ImageButton currBtn = IB;
        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                currBtn.setImageResource(R.drawable.open_door);

            }
        });

    }

    public void iconErrorDoor(ImageButton IB){
        final ImageButton currBtn = IB;
        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                currBtn.setImageResource(R.drawable.error_door);

            }
        });

    }

    public void iconClosedDoor(ImageButton IB){

        final ImageButton currBtn = IB;
        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                currBtn.setImageResource(R.drawable.closed_door);

            }
        });

    }

}

