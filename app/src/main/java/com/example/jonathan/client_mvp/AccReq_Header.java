package com.example.jonathan.client_mvp;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Calendar;

public class AccReq_Header {

    private final static int header_CMD_SIZE = 4;
    private final static int header_SDA_SIZE = 4;
    private final static int header_DDA_SIZE = 4;
    private final static int header_MSQ_SIZE = 4;
    private final static int header_MSE_SIZE = 4;
    private final static int header_TMS_SIZE = 4;
    private final static int header_MBC_SIZE = 4;

    private byte[] headerBuffer;
    private byte[] b_cmd;
    private AccReq_net_info o_senderAddr;
    private AccReq_net_info o_DestAddr;
    private byte[] b_seqNum;
    private byte[] b_sign;
    private byte[] b_timeStamp;
    private byte[] b_bodyByteCount;

    //queue buffer index for copying.
    // Set in case that the packet form needs to be changed
    private int i_IDX_CMD;
    private int i_IDX_SRCBRD;
    private int i_IDX_DESTBRD;
    private int i_IDX_SEQ;
    private int i_IDX_SIGN;
    private int i_IDX_TSTAMP;
    private int i_IDX_BCNT;



    public AccReq_Header(int MSG_HEADER_SIZE){
        headerBuffer = new byte[MSG_HEADER_SIZE];
    }

    public void zeroHeader(){
        Arrays.fill(this.headerBuffer, (byte) 0);
    }

    public byte[] getHeaderBuffer(){
        return headerBuffer;
    }

    // public static void arraycopy(Object src, int srcPos, Object dest, int destPos, int length)
    // size = 4
    public void setCmd(int IDX_CMD, int UNICAST_REQ_CMD){
        i_IDX_CMD = IDX_CMD;
        // convert command to bytes then copy to header
        b_cmd = convertIntToByteArr(UNICAST_REQ_CMD, header_CMD_SIZE);
        System.arraycopy(b_cmd, 0, headerBuffer, IDX_CMD, b_cmd.length);
    }

    // get from buffer instead of global var
    public byte[] getCmd(){

        byte[] ret_CD = new byte[header_CMD_SIZE];
        System.arraycopy(headerBuffer, i_IDX_CMD, ret_CD, 0, header_CMD_SIZE);
        return ret_CD;

    }

    public void setSenderDeviceAddr(int IDX_SRCBRD, String s_ipv4, int senderPort){
        i_IDX_SRCBRD = IDX_SRCBRD;
        o_senderAddr = new AccReq_net_info(header_SDA_SIZE);
        o_senderAddr.zeroAll();
        o_senderAddr.setServer_ipv4(s_ipv4);
        o_senderAddr.setServer_port(senderPort);

        // just copy the ip
        System.arraycopy(o_senderAddr.getServer_ipv4(), 0, headerBuffer, IDX_SRCBRD, o_senderAddr.getServerIP_Len());
    }

    // parse header
    public byte[] getSenderDeviceAddr(){
        byte[] ret_SA = new byte[header_SDA_SIZE];
        System.arraycopy(headerBuffer, i_IDX_SRCBRD, ret_SA, 0, header_SDA_SIZE);
        return ret_SA;
    }

    public void setDestDeviceAddr(int IDX_DESTBRD, String s_ipv4, int destPort) {
        i_IDX_DESTBRD = IDX_DESTBRD;
        o_DestAddr = new AccReq_net_info(header_DDA_SIZE);
        o_DestAddr.zeroAll();
        o_DestAddr.setServer_ipv4(s_ipv4);
        o_DestAddr.setServer_port(destPort);

        // just copy the ip
        System.arraycopy(o_DestAddr.getServer_ipv4(), 0, headerBuffer, IDX_DESTBRD, o_DestAddr.getServerIP_Len());
    }

    public byte[] getDestDeviceAddr(){
        byte[] ret_DA = new byte[header_DDA_SIZE];
        System.arraycopy(headerBuffer, i_IDX_DESTBRD, ret_DA, 0, header_DDA_SIZE);
        return ret_DA;
    }

    public void setMsgSequenceNum(int IDX_SEQ, int seqNum){
        i_IDX_SEQ = IDX_SEQ;
        b_seqNum = convertIntToByteArr(seqNum, header_MSQ_SIZE);
        System.arraycopy(b_seqNum, 0, headerBuffer, IDX_SEQ, b_seqNum.length);
    }

    public byte[] getMsgSeqNum(){
        //int i_seqNum = java.nio.ByteBuffer.wrap(b_seqNum).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
        //return i_seqNum;
        byte[] ret_SN = new byte[header_MSQ_SIZE];
        System.arraycopy(headerBuffer, i_IDX_SEQ, ret_SN, 0, header_MSQ_SIZE);
        return ret_SN;
    }

    // dont know if sign type
    public void setMsgSignature(int IDX_SIGN){
        i_IDX_SIGN = IDX_SIGN;
        b_sign = new byte[header_MSE_SIZE];
        Arrays.fill(b_sign, (byte) 0);
        System.arraycopy(b_sign, 0, headerBuffer, IDX_SIGN, b_sign.length);
    }

    public byte[] getMsgSignature(){
        byte[] ret_MS = new byte[header_MSE_SIZE];
        System.arraycopy(headerBuffer, i_IDX_SIGN, ret_MS, 0, header_MSE_SIZE);
        return ret_MS;
    }

    public void setTimeStamp(int IDX_TSTAMP){
        i_IDX_TSTAMP = IDX_TSTAMP;
        Calendar rightNow = Calendar.getInstance();
        long secondsSinceEpoch = rightNow.getTimeInMillis() / 1000L;

        ByteBuffer b2 = ByteBuffer.allocate(8);
        b2.order(ByteOrder.LITTLE_ENDIAN);
        b2.putLong(secondsSinceEpoch);

        byte[] b_timeSec = b2.array();

        b_timeStamp = new byte[header_TMS_SIZE];
        System.arraycopy(b_timeSec, 0, b_timeStamp, 0, b_timeStamp.length);

        System.arraycopy(b_timeStamp,0,headerBuffer,IDX_TSTAMP,4);

    }

    public byte[] getTimeStamp(){
        byte[] ret_TS = new byte[header_TMS_SIZE];
        System.arraycopy(headerBuffer, i_IDX_TSTAMP, ret_TS, 0, header_TMS_SIZE);
        return ret_TS;

    }

    public void setBodyByteCount(int IDX_BCNT, int byteCount){
        i_IDX_BCNT = IDX_BCNT;
        b_bodyByteCount = convertIntToByteArr(byteCount, header_MBC_SIZE);
        System.arraycopy(b_bodyByteCount,0,headerBuffer,IDX_BCNT, b_bodyByteCount.length);
    }

    // dont order out of little endian just return
    public byte[] getBodyByteCount(){
        byte[] ret_BBC = new byte[header_MBC_SIZE];
        System.arraycopy(headerBuffer, i_IDX_BCNT, ret_BBC, 0, header_MBC_SIZE);
        return ret_BBC;
    }

    public void printHeader(){
        Log.v("PACKET: ", "Header start");

        Log.v("PACKET: ", "CMD: " + unpackByteArr(this.getCmd()));
        Log.v("PACKET: ", "SENDER DEV ADDR: " + unpackByteArr(this.getSenderDeviceAddr()));
        Log.v("PACKET: ", "DEST DEV ADDR: " + unpackByteArr(this.getDestDeviceAddr()));
        Log.v("PACKET: ", "MSG SEQ: " + unpackByteArr(this.getMsgSeqNum()));
        Log.v("PACKET: ", "MSG SIG: " + unpackByteArr(this.getMsgSignature()));
        Log.v("PACKET: ", "TIME STAMP: " + unpackByteArr(this.getTimeStamp()));
        Log.v("PACKET: ", "BODY BYTE COUNT: " + unpackByteArr(this.getBodyByteCount()));

        //Log.v("PACKET: ", "WHOLE HEADER: " + unpackByteArr(this.getHeaderBuffer()));
        Log.v("PACKET: ", "Header end");
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

    public static byte[] convertIntToByteArr(int i, int buff_size){
        ByteBuffer b = ByteBuffer.allocate(buff_size);
        b.order(ByteOrder.LITTLE_ENDIAN);
        b.putInt(i);
        byte[] b_msg = b.array();

        return b_msg;
    }
}
