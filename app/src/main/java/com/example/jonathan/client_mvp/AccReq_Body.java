package com.example.jonathan.client_mvp;

import android.os.health.SystemHealthManager;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class AccReq_Body {

    private int i_MAX_MSG_BODY_SIZE;
    private int currentBody_SIZE = 0;

    private byte[] bodyBuffer;

    // Raw data.
    private byte[] b_randNum;
    private byte[] b_devName;
    private byte[] b_cardNum;
    private byte[] b_1comp_cardNum;

    AccReq_packet_props packet_props = new AccReq_packet_props();

    // existing body
    public AccReq_Body(byte[] existingBuffer){
        packet_props = new AccReq_packet_props();

        this.bodyBuffer = existingBuffer;
    }


    // create new body
    public AccReq_Body(AccReq_packet_props pack_props){
        currentBody_SIZE = 0;
        this.packet_props = pack_props;
        i_MAX_MSG_BODY_SIZE = packet_props.MAX_MSG_BODY_SIZE;
        this.bodyBuffer = new byte[i_MAX_MSG_BODY_SIZE];
        this.zeroBody();
    }

    public void zeroBody(){
        Arrays.fill(bodyBuffer, (byte) 0);
    }

    public byte[] getBodyFromMsg(int lengthPacket){
        //byte[] ret_BD = new byte[packet_props.MAX_MSG_BODY_SIZE];
        int bodySize = lengthPacket - packet_props.MSG_HEADER_SIZE;
        byte[] ret_BD = new byte[bodySize];
        System.arraycopy(bodyBuffer, packet_props.IDX_BODY, ret_BD, 0, bodySize);
        return ret_BD;
    }

    public byte[] getBodyBuffer(){
        return bodyBuffer;
    }

    // todo do this cause we dont know final static msg? Ask about sizing variables
    public void compactBody(){
        if(currentBody_SIZE > 0 && currentBody_SIZE < i_MAX_MSG_BODY_SIZE){
            byte[] tmp = new byte[currentBody_SIZE];
            System.arraycopy(bodyBuffer, 0, tmp, 0, currentBody_SIZE);
            bodyBuffer = tmp;
        } // else already at max
    }

    public int getBodySize(){
        return currentBody_SIZE;
    }

    // little endian

    public void setRandomNum(){


        String randomHex = "12345678";

        b_randNum = packet_props.changeEndian(packet_props.hexStringToByteArray(randomHex));

        if(b_randNum.length > packet_props.body_randNum_SIZE){
            // if adding causes over limit copy to limit, need trim
            System.arraycopy(b_randNum, 0, bodyBuffer, packet_props.IDX_RN, packet_props.body_randNum_SIZE);
        } else {
            // has sufficient space for this segment
            System.arraycopy(b_randNum, 0, bodyBuffer, packet_props.IDX_RN, b_randNum.length);
        }
        currentBody_SIZE += packet_props.body_randNum_SIZE;
        //Log.v("PACKET: ", "RN size = " + currentBody_SIZE);
    }

    /*
    public int changeRandomNum(String changeRN){

        if(bodyBuffer.length > 0) {
            // zero previous section. Just zero the whole segment
            packet_props.zeroSection(bodyBuffer, packet_props.IDX_RN, packet_props.body_randNum_SIZE); // b_randNum.length
            // new buffer
            b_randNum = packet_props.changeEndian(packet_props.hexStringToByteArray(changeRN));

            if(b_randNum.length > packet_props.body_randNum_SIZE) {
                // copy to the header segment, if too big TRIM
                System.arraycopy(b_randNum, 0, bodyBuffer, packet_props.IDX_RN, i_MAX_MSG_BODY_SIZE - currentBody_SIZE);
            } else {
                System.arraycopy(b_randNum, 0, bodyBuffer, packet_props.IDX_RN, b_randNum.length);
            }
            return 1;
        } // else not set yet
        return -1;
    }
    */

    public byte[] getRandomNum(){
        byte[] ret_RN = new byte[packet_props.body_randNum_SIZE];
        System.arraycopy(bodyBuffer, packet_props.IDX_RN, ret_RN, 0, packet_props.body_randNum_SIZE);
        return ret_RN;
    }

    // big endian
    public void setDevName(String devName){


        //byte[] sTob = changeEndian(devName.getBytes());
        byte[] sTob = devName.getBytes();
        b_devName = sTob;

        int sTob_len = sTob.length;
        if(sTob_len > packet_props.body_devName_SIZE) {
            // copy array MAX_SIZE to body buffer
            System.arraycopy(sTob, 0, bodyBuffer, packet_props.IDX_DN, packet_props.body_devName_SIZE);
        } else {
            // copy sTob.length to body buffer
            System.arraycopy(sTob, 0, bodyBuffer, packet_props.IDX_DN, sTob_len);
        }

        // set local, dont need to
        //b_devName = new byte[body_devName_SIZE];
        //System.arraycopy(bodyBuffer, IDX_DN, b_devName, 0, body_devName_SIZE);

        currentBody_SIZE += packet_props.body_devName_SIZE;
        //Log.v("PACKET: ", "DN size = " + currentBody_SIZE);

    }

    public byte[] getDevName(){
        byte[] ret_DN = new byte[packet_props.body_devName_SIZE];
        System.arraycopy(bodyBuffer, packet_props.IDX_DN, ret_DN, 0, packet_props.body_devName_SIZE);

        return ret_DN;
    }

    // big endian
    public void setCardNum(String s_cardNum){

        //byte[] sTob = changeEndian(s_cardNum.getBytes());
        byte[] sTob = s_cardNum.getBytes();
        b_cardNum = sTob;

        int sTob_len = sTob.length;
        if(sTob_len > packet_props.body_cardNum_SIZE) {
            // copy array MAX_SIZE to body buffer
            System.arraycopy(sTob, 0, bodyBuffer, packet_props.IDX_CN, packet_props.body_cardNum_SIZE);
        } else {
            // copy sTob.length to body buffer
            System.arraycopy(sTob, 0, bodyBuffer, packet_props.IDX_CN, sTob_len);
        }

        currentBody_SIZE += packet_props.body_cardNum_SIZE;

        // set local, dont need to
        //b_cardNum = new byte[body_cardNum_SIZE];
        //System.arraycopy(bodyBuffer, IDX_CN, b_cardNum, 0, body_cardNum_SIZE);


    }

    public byte[] getCardNum(){
        byte[] ret_CN = new byte[packet_props.body_cardNum_SIZE];
        System.arraycopy(bodyBuffer, packet_props.IDX_CN, ret_CN, 0, packet_props.body_cardNum_SIZE);
        return ret_CN;
    }

    // big endian
    public void set1CompCardNum(){

        byte[] og_CN = new byte[b_cardNum.length];
        System.arraycopy(bodyBuffer, packet_props.IDX_CN, og_CN, 0, b_cardNum.length);

        byte[] oneCompCN = xor1Comp(og_CN);
        b_1comp_cardNum = oneCompCN;

        if(oneCompCN.length > packet_props.body_1comp_cardNum_SIZE) {
            System.arraycopy(oneCompCN, 0, bodyBuffer, packet_props.IDX_1CN, packet_props.body_1comp_cardNum_SIZE);
        } else{
            System.arraycopy(oneCompCN, 0, bodyBuffer, packet_props.IDX_1CN, oneCompCN.length);
        }

        currentBody_SIZE += packet_props.body_1comp_cardNum_SIZE;

        // set local, dont need to
        //b_1comp_cardNum = new byte[body_cardNum_SIZE];
        //System.arraycopy(bodyBuffer, IDX_1CN, b_1comp_cardNum, 0, body_1comp_cardNum_SIZE);

    }

    public byte[] get1CompCardNum(){
        byte[] ret_1Comp = new byte[packet_props.body_1comp_cardNum_SIZE];
        System.arraycopy(bodyBuffer, packet_props.IDX_1CN, ret_1Comp, 0, packet_props.body_1comp_cardNum_SIZE);
        return ret_1Comp;
    }

    public void printBody(){
        Log.v("PACKET: ", "Body start");

        Log.v("PACKET: ", "RAND NUM: " + packet_props.unpackByteArr(this.getRandomNum()));
        Log.v("PACKET: ", "DEVICE NAME: " + packet_props.unpackByteArr(this.getDevName()));
        Log.v("PACKET: ", "CARD NUM: " + packet_props.unpackByteArr(this.getCardNum()));
        Log.v("PACKET: ", "1COMP CARD: " + packet_props.unpackByteArr(this.get1CompCardNum()));

        Log.v("PACKET: ", "WHOLE BODY: " + packet_props.unpackByteArr(this.getBodyBuffer()));
        Log.v("PACKET: ", "Body end");
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




}
