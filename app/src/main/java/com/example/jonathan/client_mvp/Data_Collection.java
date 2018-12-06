package com.example.jonathan.client_mvp;

import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class Data_Collection {

    public floor_struct st_floor;
    public List<door_struct> arr_doors;

    public Data_Collection()
    {
        arr_doors = new ArrayList<door_struct>();
    }
    
    public void newDoorList(){
        arr_doors = new ArrayList<door_struct>();   
    }

    // Initialize floor struct
    public void set_floor(String floor_name, String floor_number, String display_name, String img_dir, String doorID){
        st_floor = new floor_struct(floor_name, floor_number, display_name, img_dir, doorID);
    }

    // Set door and add
    public void addDoor(String dr_name, double[] dr_margin, String dr_IP){
        door_struct curr_door = new door_struct(dr_name, dr_margin, dr_IP);
        arr_doors.add(curr_door);
    }

}
