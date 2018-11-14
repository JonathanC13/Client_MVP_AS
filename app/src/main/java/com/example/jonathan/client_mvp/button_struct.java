package com.example.jonathan.client_mvp;

import android.widget.Button;
import android.widget.ImageButton;

public class button_struct {

    private ImageButton btn_door;
    private String IP_door;
    private int ID;

    public button_struct(ImageButton btn, String IP, int i){
        btn_door = btn;
        IP_door = IP;
        ID = i;
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

}
