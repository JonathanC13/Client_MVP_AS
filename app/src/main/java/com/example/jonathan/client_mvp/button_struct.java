package com.example.jonathan.client_mvp;

import android.widget.Button;
import android.widget.ImageButton;


// This object to hold door properties for each button that is placed currently
public class button_struct {

    private ImageButton btn_door;
    private String IP_door;
    private int ID;

    private String name;
    private String dr_UUID;


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
