package com.example.jonathan.client_mvp;

import java.util.Arrays;

public class AR_body_send_plain_id {

    private char[] random_num; // for integrity check, 4-byte integer in text
    private char[] device_name;
    private char[] data_straight_text;
    private char[] data_complement_text;

    SizeOf getSize;

    public AR_body_send_plain_id(int SIM_DEV_NAME_SIZE, int SIM_ID_BYTE_SIZE){
        SizeOf getSize = new SizeOf();
        this.device_name = new char[SIM_DEV_NAME_SIZE];
        this.data_straight_text = new char[SIM_ID_BYTE_SIZE];
        this.data_complement_text = new char[SIM_ID_BYTE_SIZE];
    }

    public void zeroAll(){
        //this.random_num = 0;

        Arrays.fill(this.device_name, '0');
        Arrays.fill(this.data_straight_text, '0');
        Arrays.fill(this.data_complement_text, '0');
    }

    public int getSizeof(){
        int total = 0;
        //int intSize = String.valueOf(random_num).length() * Integer.BYTES;
        //int intSize = this.random_num.length * Character.BYTES;
        //total = intSize + (this.device_name.length * Character.BYTES) + (this.data_straight_text.length * Character.BYTES) + (this.data_complement_text.length * Character.BYTES);
        total = this.random_num.length + (this.device_name.length) + (this.data_straight_text.length) + (this.data_complement_text.length);
        return total;
    }

    public void setRandom_num(int i_random_num) {
        random_num = ("" + i_random_num).toCharArray();

    }

    public char[] getRandom_num() {
        return random_num;
    }

    public int getRandom_numSize(){
        //return Integer.BYTES;
        //int intSize = String.valueOf(random_num).length() * Integer.BYTES;
        //return (random_num.length * Character.BYTES);
        return (random_num.length);

    }
    public int getDevice_nameSize(){
        //return device_name.length * Character.BYTES;
        //return (device_name.length * getSize.get_sizeOf(char.class));

        //return (device_name.length * Character.BYTES);
        return (device_name.length);
    }
    public int getDataStraightSize(){
        //return data_straight_text.length * Character.BYTES;
        //return (data_straight_text.length * getSize.get_sizeOf(char.class));
        //return (data_straight_text.length * Character.BYTES);
        return (data_straight_text.length);
    }
    public int getDataComplSize(){
        //return data_complement_text.length * Character.BYTES;
        //return (data_complement_text.length * getSize.get_sizeOf(char.class));
        //return (data_complement_text.length * Character.BYTES);
        return (data_complement_text.length);
    }

    public void setDevice_name(char[] device_name, int SIM_DEV_NAME_SIZE) {
        System.arraycopy(device_name, 0, this.device_name, 0, SIM_DEV_NAME_SIZE);
    }

    public char[] getDevice_name() {
        return device_name;
    }

    public void setData_straight_text(char[] data_straight_text) {
        this.data_straight_text = data_straight_text;
    }

    public char[] getData_straight_text() {
        return data_straight_text;
    }

    public void setData_complement_text(char[] data_complement_text) {
        this.data_complement_text = data_complement_text;
    }

    public char[] getData_complement_text() {
        return data_complement_text;
    }
}
