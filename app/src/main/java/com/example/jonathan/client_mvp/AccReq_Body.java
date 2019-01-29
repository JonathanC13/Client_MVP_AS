package com.example.jonathan.client_mvp;

import android.os.health.SystemHealthManager;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class AccReq_Body {

    private int body_randNum_SIZE = 4;
    private int body_devName_SIZE = 128;
    private int body_cardNum_SIZE = 64;
    private int body_1comp_cardNum_SIZE = 64;

    private int i_MAX_MSG_BODY_SIZE;
    private int currentBody_SIZE = 0;

    private byte[] bodyBuffer;
    private byte[] b_randNum;
    private byte[] b_devName;
    private byte[] b_cardNum;
    private byte[] b_1comp_cardNum;

    //queue buffer index for copying.
    // Set in case that the packet form needs to be changed
    private int i_IDX_RN;
    private int i_IDX_DN;
    private int i_IDX_CN;
    private int i_IDX_1CN;


    public AccReq_Body(int MAX_MSG_BODY_SIZE){
        i_MAX_MSG_BODY_SIZE = MAX_MSG_BODY_SIZE;
        this.bodyBuffer = new byte[MAX_MSG_BODY_SIZE];
    }

    public void zeroBody(){
        Arrays.fill(bodyBuffer, (byte) 0);
    }

    public byte[] getBodyBuffer(){
        return bodyBuffer;
    }

    // todo do this cause we dont know final static msg? Ask about sizing variables
    public void compactBody(){
        if(currentBody_SIZE > 0){
            byte[] tmp = new byte[currentBody_SIZE];
            System.arraycopy(bodyBuffer, 0, tmp, 0, currentBody_SIZE);
            bodyBuffer = tmp;
        }
    }

    public int getBodySize(){
        return currentBody_SIZE;
    }

    // todo for all set methods could add current size checks against max size
    public void setRandomNum(int IDX_RN){
        this.i_IDX_RN = IDX_RN;

        String randomHex = "12345678";

        b_randNum = changeEndian(hexStringToByteArray(randomHex));
        System.arraycopy(b_randNum, 0, bodyBuffer, IDX_RN, b_randNum.length);

        currentBody_SIZE += body_randNum_SIZE;
    }

    public byte[] getRandomNum(){
        byte[] ret_RN = new byte[body_randNum_SIZE];
        System.arraycopy(bodyBuffer, i_IDX_RN, ret_RN, 0, body_randNum_SIZE);
        return ret_RN;
    }

    public void setDevName(int IDX_DN, String devName){
        i_IDX_DN = IDX_DN;

        //byte[] sTob = changeEndian(devName.getBytes());
        byte[] sTob = devName.getBytes();
        b_devName = sTob;

        int sTob_len = sTob.length;
        if(sTob_len >= body_devName_SIZE) {
            // copy array MAX_SIZE to body buffer
            System.arraycopy(sTob, 0, bodyBuffer, IDX_DN, body_devName_SIZE);
        } else {
            // copy sTob.length to body buffer
            System.arraycopy(sTob, 0, bodyBuffer, IDX_DN, sTob_len);
        }

        b_devName = new byte[body_devName_SIZE];
        System.arraycopy(bodyBuffer, IDX_DN, b_devName, 0, body_devName_SIZE);

        currentBody_SIZE += body_devName_SIZE;
    }

    public byte[] getDevName(){
        byte[] ret_DN = new byte[body_devName_SIZE];
        System.arraycopy(bodyBuffer, i_IDX_DN, ret_DN, 0, body_devName_SIZE);

        return ret_DN;
    }

    public void setCardNum(int IDX_CN, String s_cardNum){
        i_IDX_CN = IDX_CN;

        //byte[] sTob = changeEndian(s_cardNum.getBytes());
        byte[] sTob = s_cardNum.getBytes();

        int sTob_len = sTob.length;
        if(sTob_len >= body_cardNum_SIZE) {
            // copy array MAX_SIZE to body buffer
            System.arraycopy(sTob, 0, bodyBuffer, IDX_CN, body_cardNum_SIZE);
        } else {
            // copy sTob.length to body buffer
            System.arraycopy(sTob, 0, bodyBuffer, IDX_CN, sTob_len);
        }
        b_cardNum = new byte[body_cardNum_SIZE];
        System.arraycopy(bodyBuffer, IDX_CN, b_cardNum, 0, body_cardNum_SIZE);

        currentBody_SIZE += body_cardNum_SIZE;
    }

    public byte[] getCardNum(){
        byte[] ret_CN = new byte[body_cardNum_SIZE];
        System.arraycopy(bodyBuffer, i_IDX_CN, ret_CN, 0, body_cardNum_SIZE);
        return ret_CN;
    }

    public void set1CompCardNum(int IDX_1CN){
        i_IDX_1CN = IDX_1CN;
        byte[] og_CN = new byte[body_cardNum_SIZE];
        System.arraycopy(bodyBuffer,i_IDX_CN, og_CN,0,body_cardNum_SIZE);

        byte[] oneCompCN = xor1Comp(og_CN);
        b_1comp_cardNum = oneCompCN;
        System.arraycopy(oneCompCN, 0, bodyBuffer, IDX_1CN, body_1comp_cardNum_SIZE);

        currentBody_SIZE += body_1comp_cardNum_SIZE;
    }

    public byte[] get1CompCardNum(){
        byte[] ret_1Comp = new byte[body_1comp_cardNum_SIZE];
        System.arraycopy(bodyBuffer, i_IDX_1CN, ret_1Comp, 0, body_1comp_cardNum_SIZE);
        return ret_1Comp;
    }

    public void printBody(){
        Log.v("PACKET: ", "Body start");

        Log.v("PACKET: ", "RAND NUM: " + unpackByteArr(this.getRandomNum()));
        Log.v("PACKET: ", "DEVICE NAME: " + unpackByteArr(this.getDevName()));
        Log.v("PACKET: ", "CARD NUM: " + unpackByteArr(this.getCardNum()));
        Log.v("PACKET: ", "1COMP CARD: " + unpackByteArr(this.get1CompCardNum()));

        Log.v("PACKET: ", "WHOLE HEADER: " + unpackByteArr(this.getBodyBuffer()));
        Log.v("PACKET: ", "Body end");
    }

    private static String unpackByteArr(byte[] b_arr){
        String s_complete = "";
        for(int i = 0; i < b_arr.length; i ++) {
            //byte p_byte = (byte) (msgb2[i]);
            //System.out.println("The byte conversion is: " + p_byte);
            String s1 = String.format("%8s", Integer.toBinaryString(b_arr[i] & 0xFF)).replace(' ', '0');
            s_complete += " " + s1;
        }
        return s_complete;
    }


    public byte[] xor1Comp(byte[] a){
        byte[] ret_xored;
        int a_len = a.length;

        ret_xored = new byte[a_len];

        for (int i = 0; i < a_len; i ++){
            ret_xored[i] = (byte) ((0xff)^a[i]);
        }
        return ret_xored;
    }


    public static byte[] changeEndian(byte[] b_arr){
        int in_len = b_arr.length;
        byte[] ret_arr = new byte[in_len];

        for(int i = 0; i < in_len; i ++){
            ret_arr[i] = b_arr[in_len - i -1];
        }

        return ret_arr;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static byte[] convertIntToByteArr(int i, int buff_size){
        ByteBuffer b = ByteBuffer.allocate(buff_size);
        b.order(ByteOrder.LITTLE_ENDIAN);
        b.putInt(i);
        byte[] b_msg = b.array();

        return b_msg;
    }

}
