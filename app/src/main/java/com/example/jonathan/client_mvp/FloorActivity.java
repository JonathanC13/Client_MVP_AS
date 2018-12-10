package com.example.jonathan.client_mvp;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import java.io.File;

public class FloorActivity extends AppCompatActivity {

    private ImageView main_img;
    private Data_Controller dataPull;
    private ConstraintLayout grd_scr;

    private String cardID;

    Context context = FloorActivity.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_floor);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        final float scale = getResources().getDisplayMetrics().density;
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
        String intStorageDirectory = getFilesDir().toString();
        File folder = new File(intStorageDirectory, "images");
        folder.mkdirs();
        //Log.v("TASK: ", "internal " + intStorageDirectory);
        //Log.v("TASK: ", "new " + folder.getName());

        String save_img_folder = intStorageDirectory + "/images";
        File comon = new File(save_img_folder);
        //if(comon.exists()){
        //  Log.v("TASK: ", "initial: " + save_img_folder);
        //}

        // Object that controls the Data
        dataPull = new Data_Controller(intStorageDirectory, grd_scr, scale);


        // Pass object to DB_controller so it can save the data to it.
        // Pulls the door images, floors' information, and doors' information
        DB_Controller DB_con_con = new DB_Controller(this, dataPull);


        // Fill spinner, need to pass context to be able to fill it.
        dataPull.set_combobox_items(this, s_items, main_img, dnBtn); // <------- IN HERE, IMAGE SETTING
        // since starting index is 0, down button is not clickable
        dnBtn.setClickable(false);


        // spinner listener
        s_items.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                int max_ind = dataPull.flr_dr_class_list.size();

                if(position >= max_ind -1) {
                    upBtn.setClickable(false);
                    dnBtn.setClickable(true);
                } else if (position <= 0){
                    upBtn.setClickable(true);
                    dnBtn.setClickable(false);
                }

                spn_lvlsSelectionChanged(position);
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

                if (new_sel >= max_ind-1){
                    new_sel = max_ind-1;

                    upBtn.setClickable(false);
                }
                if (new_sel > 0){
                    // since increase, make button clickable
                    dnBtn.setClickable(true);
                }

                // set spinner item
                s_items.setSelection(new_sel);

                upBtn_onClick(new_sel);
            }
        });

        // Listener for Down Level button
        dnBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int cur_sel = s_items.getSelectedItemPosition();
                int new_sel = cur_sel - 1;
                int max_id = dataPull.flr_dr_class_list.size();

                if (new_sel <= 0){
                    new_sel = 0;
                    dnBtn.setClickable(false);

                }
                if (new_sel < max_id){
                    // since decrease, make button clickable
                    upBtn.setClickable(true);

                }
                // set spinner item
                s_items.setSelection(new_sel);

                dnBtn_onClick(new_sel);
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.signout){
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


}

