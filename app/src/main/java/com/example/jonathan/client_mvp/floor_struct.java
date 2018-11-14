package com.example.jonathan.client_mvp;

public class floor_struct {

    private String og_flr_name; // original name

    private String floor_num; // floor number

    private String name; // display name

    private String floor_image; // Exclusively for floor table, get the path to the downloaded image.

    private String door_ID; // door ID to know which doors in the door table that are relevant.

    // constructor
    public floor_struct(String floor_name, String floor_number, String display_name, String img_dir, String doorID){
        this.og_flr_name = floor_name;
        this.floor_num = floor_number;
        this.name = display_name;
        this.floor_image = img_dir;
        this.door_ID = doorID;
    }

    // <Getters/Setters>
    // Get original name
    public String getOg_flr_name(){
        return og_flr_name;
    }

    // Get display name
    public String getDisplayName()
    {
        return name;
    }

    public String getImgDir(){
        return floor_image;
    }

    public String getDoorID()
    {
        return door_ID;
    }

    public String getFloor_num() {
        return floor_num;
    }
    // </Getters/Setters>

}
