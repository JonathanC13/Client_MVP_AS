package com.example.jonathan.client_mvp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import android.widget.RelativeLayout.LayoutParams;

public class Data_Controller {

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

    private float scale;

    // List of floor/doors/button objects
    List<Data_Collection> flr_dr_class_list = new ArrayList<Data_Collection>();

    // pure string array to set the combobox(spinner) items
    private List<String> arr_flr_name = new ArrayList<String>();

    // List of placed doors
    private List<button_struct> placed_doors = new ArrayList<button_struct>();

    // ScrollView internal layout
    ConstraintLayout grd_scr;

    public Data_Controller(String appDIR, ConstraintLayout grd_s, float IV_scale){
        grd_scr = grd_s;
        scale = IV_scale;

        //curr_app_dir = appDIR;
        image_folder = "/images/";
        full_img_path = appDIR;

        //closed_door = "/closed_door.png";
        //open_door = "/open_door.png";

        //locked_door_image = full_img_path + closed_door;
        //open_door_path = full_img_path + open_door;

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

        for(button_struct dr : placed_doors){
            ImageButton curr_btn = (ImageButton) dr.getBtn();
            grd_scr.removeView(curr_btn);
        }
    }

    private void place_curr_doors(int floor_sel, Context main){

        placed_doors = new ArrayList<button_struct>();

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

                //add button to the layout
                grd_scr.addView(btn_new);

                // generic button click
                btn_new.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {

                        generic_button_click(btn_new);
                    }
                });

                // </Set button attributes>

                button_struct btn_cr = new button_struct(btn_new, dr.getDrIP(), i);
                placed_doors.add(btn_cr);

                i++;
            }
        }

    }


    private void generic_button_click(View v )
    {
        final ImageButton IB = (ImageButton)v;
        String button_IP = "";

        // search button_struct placed_doors() List for the door and get its IP
        for (button_struct btn : placed_doors)
        {

            if (IB.getId() == btn.getID()) {

                button_IP = btn.getIP();
                break;
            }
        }

        Log.v("TASK: ", button_IP);

        // udp send to open door and wait for receive message
        //UDP_controller udpTask = new UDP_controller(button_IP);
        //int response = udpTask.executeUDP(); // Starts async task for udp operation

        // if ack receive that door is opened, change color of door to green for "opened" time
        //if(response == 1){
            IB.setImageResource(R.drawable.open_door);
        //} else { B.setImageResource(R.drawable.bluetooth_fail); }

        // Depending on how we lock the door change this. This wait causes an error, crashes the app.
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Do something after 5s = 5000ms
                // change door to closed icon
                IB.setImageResource(R.drawable.closed_door);
            }
        }, 5000);



    }


    public String getFullImgPath(){
        return full_img_path;
    }

}
