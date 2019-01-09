package com.example.jonathan.client_mvp;

public interface Bluetooth_constants {

    /*
    Defines several constants used between the Data_controller and Bluetooth_service
     */

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // key names received from the Bluetooth_server handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Constants for caller
    public static final int BT_OTHER = 00;
    public static final int BT_Connected = 11;
    public static final int BT_DOOR_OPEN = 22;

}
