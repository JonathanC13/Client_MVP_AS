package com.example.jonathan.client_mvp;


import android.bluetooth.BluetoothDevice;
import android.media.Image;
import android.widget.ImageButton;

// object to hold all the properties for each door from the DB
public class door_struct {

    private String name;

    // Button attributes
    private double[] door_margin = new double[4]; // margin L,T,R,B - Default HorizontalAlignment = Left, VerticalAlignment = Top
    // Default set Height ="50" Width="50" Background="Red" FontSize="14" Foreground="White"
    // Click="btn_Door_1_Click" created based on name
    // Button IP
    private String door_IP;

    private int door_Port;
    private String dev_remoteName;
    private String dev_MAC;

    private Btn_placed_door placed_btn = null;

    private BluetoothDevice bt_dev = null;

    public door_struct(String dr_name, double[] dr_margin, String dr_IP, int dr_Port, String dev_remoteName, String dev_MAC){
        this.name = dr_name;
        this.door_margin = dr_margin;
        this.door_IP = dr_IP;
        this.door_Port = dr_Port;
        this.dev_remoteName = dev_remoteName;
        this.dev_MAC = dev_MAC;
    }

    public void setBT_device(BluetoothDevice bt_device){
        bt_dev = bt_device;
    }

    public void setImageButton(ImageButton imgBtn, int btnID){
        placed_btn = new Btn_placed_door(imgBtn, btnID);
    }

    public void removeImageButtonObj(){
        placed_btn = null;
    }

    // <Getters>
    public BluetoothDevice getBt_dev() {
        return bt_dev;
    }

    public ImageButton getImgBtn(){
        if(placed_btn != null) {
            return placed_btn.getBtn_door();
        } else {
            return null;
        }
    }

    public int getBtnID(){
        if(placed_btn != null) {
            return placed_btn.getBtn_ID();
        } else {
            return -1;
        }
    }

    public String getDrName(){
        return name;
    }

    public double[] getDrMargin(){
        return door_margin;
    }

    public double getLeft(){
        return door_margin[0];
    }

    public double getTop(){
        return door_margin[1];
    }

    public double getRight(){
        return door_margin[2];
    }

    public double getBot(){
        return door_margin[3];
    }

    public String getDrIP(){
        return door_IP;
    }

    public int getDoor_Port() { return door_Port; }

    public String getDev_remoteName() { return dev_remoteName; }

    public String getDev_MAC() { return dev_MAC; };
    // </Getters>

}
