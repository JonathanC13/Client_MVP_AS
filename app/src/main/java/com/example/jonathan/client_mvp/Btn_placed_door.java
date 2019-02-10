package com.example.jonathan.client_mvp;

import android.media.Image;
import android.widget.ImageButton;

public class Btn_placed_door {

    private ImageButton btn_door;
    private int btn_ID;

    public Btn_placed_door(ImageButton imgBtnDoor, int imgBtnID){
        this.btn_door = imgBtnDoor;
        this.btn_ID = imgBtnID;
    }

    public ImageButton getBtn_door(){
        return btn_door;
    }

    public int getBtn_ID(){
        return btn_ID;
    }

}
