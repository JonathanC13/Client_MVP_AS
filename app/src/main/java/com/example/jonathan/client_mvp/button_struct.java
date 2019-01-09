package com.example.jonathan.client_mvp;

import android.widget.Button;
import android.widget.ImageButton;

public class button_struct {

    private ImageButton btn_door;
    private String IP_door;
    private int ID;

    private String name; //Door_1, Used to match Bluetooth device name, must be accurate in Database
    private String dr_UUID; // Need to match the device UUID connecting to, must be accurate in database.


    public button_struct(ImageButton btn, String IP, int i, String dr_name){
        btn_door = btn;
        IP_door = IP;
        ID = i;
        name = dr_name;
    }

    public ImageButton getBtn(){
        return btn_door;
    }

    public String getIP(){
        return IP_door;
    }

    public int getID(){
        return ID;
    }

    public String getDrName(){
        return name;
    }


}
