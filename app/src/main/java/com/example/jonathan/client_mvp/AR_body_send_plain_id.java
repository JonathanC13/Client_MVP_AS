package com.example.jonathan.client_mvp;

import java.util.Arrays;

public class AR_body_send_plain_id {

    private int random_num; // for integrity check, 4-byte integer in text
    private char[] device_name;
    private char[] data_straight_text;
    private char[] data_complement_text;

    public AR_body_send_plain_id(int SIM_DEV_NAME_SIZE, int SIM_ID_BYTE_SIZE){
        this.device_name = new char[SIM_DEV_NAME_SIZE];
        this.data_straight_text = new char[SIM_ID_BYTE_SIZE];
        this.data_complement_text = new char[SIM_ID_BYTE_SIZE];
    }

    public void zeroAll(){
        this.random_num = 0;
        Arrays.fill(this.device_name, '0');
        Arrays.fill(this.data_straight_text, '0');
        Arrays.fill(this.data_complement_text, '0');
    }

    public int getSizeof(){
        int total = 0;
        int intSize = random_num * Integer.BYTES;
        total = intSize + (this.device_name.length * Character.BYTES) + (this.data_straight_text.length * Character.BYTES) + (this.data_complement_text.length * Character.BYTES);
        return total;
    }

    public void setRandom_num(int random_num) {
        this.random_num = random_num;
    }

    public int getRandom_num() {
        return random_num;
    }

    public void setDevice_name(char[] device_name, int SIM_DEV_NAME_SIZE) {
        this.device_name = Arrays.copyOf(device_name, SIM_DEV_NAME_SIZE);

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
