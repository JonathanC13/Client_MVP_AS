package com.example.jonathan.client_mvp;

public class door_struct {

    private String name; //Door_1, Used to match Bluetooth device name, must be accurate in Database

    // Button attributes
    private double[] door_margin = new double[4]; // margin L,T,R,B - Default HorizontalAlignment = Left, VerticalAlignment = Top
    // Default set Height ="50" Width="50" Background="Red" FontSize="14" Foreground="White"
    // Click="btn_Door_1_Click" created based on name
    // Button IP
    private String door_IP;

    public door_struct(String dr_name, double[] dr_margin, String dr_IP){
        this.name = dr_name;
        this.door_margin = dr_margin;
        this.door_IP = dr_IP;
    }

    // <Getters>
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
    // </Getters>

}
