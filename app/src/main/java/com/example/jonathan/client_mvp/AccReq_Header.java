package com.example.jonathan.client_mvp;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Calendar;

public class AccReq_Header {


    private int i_MSG_HEADER_SIZE;

    //private int currentHeader_SIZE = 0;

    private byte[] headerBuffer;
    private byte[] b_cmd;
    private AccReq_net_info o_senderAddr;
    private AccReq_net_info o_DestAddr;
    private byte[] b_seqNum;
    private byte[] b_sign;
    private byte[] b_timeStamp;
    private byte[] b_bodyByteCount;

    AccReq_packet_props packet_props;

    // existing packet
    public AccReq_Header(byte[] existingBuffer, AccReq_packet_props pack_props){
        this.packet_props = pack_props;
        // just set the whole buffer (header + body) to headerBuffer. Just need to analyze header segments
        headerBuffer = existingBuffer;
        i_MSG_HEADER_SIZE = packet_props.MSG_HEADER_SIZE;
    }

    // completely new packet
    public AccReq_Header(AccReq_packet_props pack_props){
        this.packet_props = pack_props;
        i_MSG_HEADER_SIZE = packet_props.MSG_HEADER_SIZE;
        headerBuffer = new byte[i_MSG_HEADER_SIZE];
        this.zeroHeader();
    }

    public void zeroHeader(){
        Arrays.fill(this.headerBuffer, (byte) 0);
    }

    public byte[] getHeaderBuffer(){
        return headerBuffer;
    }

    // get header from packet. do i need?

    public int getHeaderBufferSIZE(){
        return headerBuffer.length;
    }

    // public static void arraycopy(Object src, int srcPos, Object dest, int destPos, int length)
    /// little endian
    // size = 4 // int UNICAST_REQ_CMD
    public void setCmd(int UNICAST_REQ_CMD){

        // convert command to bytes then copy to header
        b_cmd = packet_props.convertIntToByteArr(UNICAST_REQ_CMD, packet_props.header_CMD_SIZE);

        /* Don't need this, just override section.
        if((currentHeader_SIZE + packet_props.header_CMD_SIZE) > i_MSG_HEADER_SIZE) {
            // if adding the cmd segment will overflow the header buffer
            // fill the space that is left, if any, with the cmd bytes. This mostly likely will lead to malformed packet and error response from destination
            System.arraycopy(b_cmd, 0, headerBuffer, packet_props.IDX_CMD, i_MSG_HEADER_SIZE - currentHeader_SIZE);
        }else if(b_cmd.length > packet_props.header_CMD_SIZE){
            // if cmd size is greater than the cmd segment in the header
            System.arraycopy(b_cmd, 0, headerBuffer, packet_props.IDX_CMD, packet_props.header_CMD_SIZE);
        } else {
            System.arraycopy(b_cmd, 0, headerBuffer, packet_props.IDX_CMD, b_cmd.length);
        }
        currentHeader_SIZE += packet_props.header_CMD_SIZE;
        */

        if(b_cmd.length > packet_props.header_CMD_SIZE){
            // if cmd size is greater than the cmd segment in the header, then trim to segment size
            try {
                System.arraycopy(b_cmd, 0, headerBuffer, packet_props.IDX_CMD, packet_props.header_CMD_SIZE);
            } catch (Exception e){
                Log.v("PACKET: ", "setCmd, cmd length > Cmd segment length, attempted to trim to segment but err: " + e.toString());
            }
        } else {
            try {
                System.arraycopy(b_cmd, 0, headerBuffer, packet_props.IDX_CMD, b_cmd.length);
            } catch (Exception e){
                Log.v("PACKET: ", "setCmd, attempted to copy to segment but err: " + e.toString());
            }
        }

    }

    // get from buffer instead of global var
    public byte[] getCmd(){

        byte[] ret_CD = new byte[packet_props.header_CMD_SIZE];
        System.arraycopy(headerBuffer, packet_props.IDX_CMD, ret_CD, 0, packet_props.header_CMD_SIZE);
        return ret_CD;

    }

    // unknown endian, so currently set to little endian
    public void setSenderDeviceAddr(String s_ipv4, int senderPort){

        o_senderAddr = new AccReq_net_info(packet_props.header_SDA_SIZE);
        o_senderAddr.zeroAll();
        o_senderAddr.setServer_ipv4(s_ipv4);
        o_senderAddr.setServer_port(senderPort);

        /*
        if((currentHeader_SIZE + packet_props.header_SDA_SIZE) > i_MSG_HEADER_SIZE || (o_senderAddr.getServerIP_Len() > packet_props.header_SDA_SIZE)) {
            System.arraycopy(o_senderAddr.getServer_ipv4(), 0, headerBuffer, packet_props.IDX_SRCBRD, i_MSG_HEADER_SIZE - currentHeader_SIZE);
        } else {
            // just copy the ip
            System.arraycopy(o_senderAddr.getServer_ipv4(), 0, headerBuffer, packet_props.IDX_SRCBRD, o_senderAddr.getServerIP_Len());
        }
        currentHeader_SIZE += packet_props.header_SDA_SIZE;
        */

        if(o_senderAddr.getServerIP_Len() > packet_props.header_SDA_SIZE) {
            try{
            System.arraycopy(o_senderAddr.getServer_ipv4(), 0, headerBuffer, packet_props.IDX_SRCBRD, packet_props.header_CMD_SIZE);
            } catch (Exception e){
                Log.v("PACKET: ", "setSender, senderIPV4 length > senderIPV4 segment length, attempted to trim to segment but err: " + e.toString());
            }
        } else {
            // just copy the ip
            try {
                System.arraycopy(o_senderAddr.getServer_ipv4(), 0, headerBuffer, packet_props.IDX_SRCBRD, o_senderAddr.getServerIP_Len());
            } catch (Exception e){
                Log.v("PACKET: ", "setSender, senderIPV4, attempted to copy to segment but err: " + e.toString());
            }
        }
    }

    // parse header
    public byte[] getSenderDeviceAddr(){
        byte[] ret_SA = new byte[packet_props.header_SDA_SIZE];
        System.arraycopy(headerBuffer, packet_props.IDX_SRCBRD, ret_SA, 0, packet_props.header_SDA_SIZE);
        return ret_SA;
    }

    // unknown endian, so currently set to little endian
    public void setDestDeviceAddr(String s_ipv4, int destPort) {

        o_DestAddr = new AccReq_net_info(packet_props.header_DDA_SIZE);
        o_DestAddr.zeroAll();
        o_DestAddr.setServer_ipv4(s_ipv4);
        o_DestAddr.setServer_port(destPort);

        /*
        if((currentHeader_SIZE + packet_props.header_DDA_SIZE) > i_MSG_HEADER_SIZE || (o_DestAddr.getServerIP_Len() > packet_props.header_DDA_SIZE)) {
            System.arraycopy(o_DestAddr.getServer_ipv4(), 0, headerBuffer, packet_props.IDX_DESTBRD, i_MSG_HEADER_SIZE - currentHeader_SIZE);
        } else {
            // just copy the ip
            System.arraycopy(o_DestAddr.getServer_ipv4(), 0, headerBuffer, packet_props.IDX_DESTBRD, o_DestAddr.getServerIP_Len());
        }
        currentHeader_SIZE += packet_props.header_DDA_SIZE;
        */

        if(o_DestAddr.getServerIP_Len() > packet_props.header_DDA_SIZE) {
            try {
                System.arraycopy(o_DestAddr.getServer_ipv4(), 0, headerBuffer, packet_props.IDX_DESTBRD, packet_props.header_DDA_SIZE);
            } catch (Exception e){
                Log.v("PACKET: ", "setDest, destIPV4 length > destIPV4 segment length, attempted to trim to segment but err: " + e.toString());
            }
        } else {
            // just copy the ip
            try {
                System.arraycopy(o_DestAddr.getServer_ipv4(), 0, headerBuffer, packet_props.IDX_DESTBRD, o_DestAddr.getServerIP_Len());
            } catch (Exception e){
                Log.v("PACKET: ", "setDest, destIPV4, attempted to copy to segment but err: " + e.toString());
            }
        }
    }

    public byte[] getDestDeviceAddr(){
        byte[] ret_DA = new byte[packet_props.header_DDA_SIZE];
        System.arraycopy(headerBuffer, packet_props.IDX_DESTBRD, ret_DA, 0, packet_props.header_DDA_SIZE);
        return ret_DA;
    }

    // unknown endian, so set to little
    public void setMsgSequenceNum(int seqNum){

        b_seqNum = packet_props.convertIntToByteArr(seqNum, packet_props.header_MSQ_SIZE);

        /*
        if((currentHeader_SIZE + packet_props.header_MSQ_SIZE) > i_MSG_HEADER_SIZE || (b_seqNum.length > packet_props.header_MSQ_SIZE)) {
            System.arraycopy(b_seqNum, 0, headerBuffer, packet_props.IDX_SEQ, i_MSG_HEADER_SIZE - currentHeader_SIZE);
        } else {
            System.arraycopy(b_seqNum, 0, headerBuffer, packet_props.IDX_SEQ, b_seqNum.length);
        }
        currentHeader_SIZE += packet_props.header_MSQ_SIZE;
        */

        if(b_seqNum.length > packet_props.header_MSQ_SIZE) {
            // need trim to segment size
            try{
                System.arraycopy(b_seqNum, 0, headerBuffer, packet_props.IDX_SEQ, packet_props.header_MSQ_SIZE);
            } catch (Exception e){
                Log.v("PACKET: ", "setSeqNum, seqNum length > seqNum segment length, attempted to trim to segment but err: " + e.toString());
            }
        } else {
            try{
                System.arraycopy(b_seqNum, 0, headerBuffer, packet_props.IDX_SEQ, b_seqNum.length);
            } catch (Exception e){
                Log.v("PACKET: ", "setSeqNum, attempted to copy to segment but err: " + e.toString());
            }
        }
    }

    public byte[] getMsgSeqNum(){
        //int i_seqNum = java.nio.ByteBuffer.wrap(b_seqNum).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
        //return i_seqNum;
        byte[] ret_SN = new byte[packet_props.header_MSQ_SIZE];
        System.arraycopy(headerBuffer, packet_props.IDX_SEQ, ret_SN, 0, packet_props.header_MSQ_SIZE);
        return ret_SN;
    }

    // dont know if sign type
    // unknown endian, so set to little
    public void setMsgSignature(){

        b_sign = new byte[packet_props.header_MSE_SIZE];
        Arrays.fill(b_sign, (byte) 0);

        /*
        if((currentHeader_SIZE + packet_props.header_MSE_SIZE) > i_MSG_HEADER_SIZE || (b_sign.length > packet_props.header_MSE_SIZE)) {
            System.arraycopy(b_sign, 0, headerBuffer, packet_props.IDX_SIGN, i_MSG_HEADER_SIZE - currentHeader_SIZE);
        } else {
            System.arraycopy(b_sign, 0, headerBuffer, packet_props.IDX_SIGN, b_sign.length);
        }
        currentHeader_SIZE += packet_props.header_MSE_SIZE;
        */

        if(b_sign.length > packet_props.header_MSE_SIZE) {
            try {
                System.arraycopy(b_sign, 0, headerBuffer, packet_props.IDX_SIGN, packet_props.header_MSE_SIZE);
            } catch (Exception e){
                Log.v("PACKET: ", "setMsgSig, MsgSig length > MsgSig segment length, attempted to trim to segment but err: " + e.toString());
            }
        } else {
            try{
                System.arraycopy(b_sign, 0, headerBuffer, packet_props.IDX_SIGN, b_sign.length);
            } catch (Exception e){
                Log.v("PACKET: ", "setMsgSig, attempted to copy to segment but err: " + e.toString());
            }
        }
    }

    public byte[] getMsgSignature(){
        byte[] ret_MS = new byte[packet_props.header_MSE_SIZE];
        System.arraycopy(headerBuffer, packet_props.IDX_SIGN, ret_MS, 0, packet_props.header_MSE_SIZE);
        return ret_MS;
    }

    // little endian
    public void setTimeStamp(){

        Calendar rightNow = Calendar.getInstance();
        long secondsSinceEpoch = rightNow.getTimeInMillis() / 1000L;

        ByteBuffer b2 = ByteBuffer.allocate(8);
        b2.order(ByteOrder.LITTLE_ENDIAN);
        b2.putLong(secondsSinceEpoch);

        byte[] b_timeSec = b2.array();

        b_timeStamp = new byte[packet_props.header_TMS_SIZE];
        Arrays.fill(b_timeStamp, (byte) '0');

        System.arraycopy(b_timeSec, 0, b_timeStamp, 0, b_timeStamp.length);

        /*
        if((currentHeader_SIZE + packet_props.header_TMS_SIZE) > i_MSG_HEADER_SIZE || (b_timeStamp.length > packet_props.header_TMS_SIZE)) {
            System.arraycopy(b_timeStamp, 0, headerBuffer, packet_props.IDX_TSTAMP, i_MSG_HEADER_SIZE - currentHeader_SIZE);
        } else {
            System.arraycopy(b_timeStamp, 0, headerBuffer, packet_props.IDX_TSTAMP, 4);
        }
        currentHeader_SIZE += packet_props.header_TMS_SIZE;
        */
        if(b_timeStamp.length > packet_props.header_TMS_SIZE) {
            try {
                System.arraycopy(b_timeStamp, 0, headerBuffer, packet_props.IDX_TSTAMP, packet_props.header_TMS_SIZE);
            } catch (Exception e){
                Log.v("PACKET: ", "setTimeStamp, timeStamp length > timeStamp segment length, attempted to trim to segment but err: " + e.toString());
            }
        } else {
            try {
                System.arraycopy(b_timeStamp, 0, headerBuffer, packet_props.IDX_TSTAMP, b_timeStamp.length);
            } catch (Exception e){
                Log.v("PACKET: ", "setTimeStamp, attempted to copy to segment but err: " + e.toString());
            }
        }

    }

    public byte[] getTimeStamp(){
        byte[] ret_TS = new byte[packet_props.header_TMS_SIZE];
        System.arraycopy(headerBuffer, packet_props.IDX_TSTAMP, ret_TS, 0, packet_props.header_TMS_SIZE);
        return ret_TS;

    }

    // little endian
    public void setBodyByteCount(int byteCount){

        b_bodyByteCount = packet_props.convertIntToByteArr(byteCount, packet_props.header_MBC_SIZE);

        /*
        if((currentHeader_SIZE + packet_props.header_MBC_SIZE) > i_MSG_HEADER_SIZE || (b_bodyByteCount.length > packet_props.header_MBC_SIZE)) {
            System.arraycopy(b_bodyByteCount, 0, headerBuffer, packet_props.IDX_BCNT, i_MSG_HEADER_SIZE - currentHeader_SIZE);
        } else {
            System.arraycopy(b_bodyByteCount, 0, headerBuffer, packet_props.IDX_BCNT, b_bodyByteCount.length);
        }
        currentHeader_SIZE += packet_props.header_MBC_SIZE;
        */

        if(b_bodyByteCount.length > packet_props.header_MBC_SIZE) {
            try {
                System.arraycopy(b_bodyByteCount, 0, headerBuffer, packet_props.IDX_BCNT, packet_props.header_MBC_SIZE);
            } catch (Exception e){
                Log.v("PACKET: ", "setBodyByteCount, bodybyteCount length > bodybyteCount segment length, attempted to trim to segment but err: " + e.toString());
            }
        } else {
            try {
                System.arraycopy(b_bodyByteCount, 0, headerBuffer, packet_props.IDX_BCNT, b_bodyByteCount.length);
            }  catch (Exception e){
                Log.v("PACKET: ", "setBodyByteCount, attempted to copy to segment but err: " + e.toString());
            }

        }
    }

    // dont order out of little endian just return
    public byte[] getBodyByteCount(){
        byte[] ret_BBC = new byte[packet_props.header_MBC_SIZE];
        System.arraycopy(headerBuffer, packet_props.IDX_BCNT, ret_BBC, 0, packet_props.header_MBC_SIZE);
        return ret_BBC;
    }

    public void printHeader(){
        Log.v("PACKET: ", "Header start");

        Log.v("PACKET: ", "CMD: " + packet_props.unpackByteArr(this.getCmd()));
        Log.v("PACKET: ", "SENDER DEV ADDR: " + packet_props.unpackByteArr(this.getSenderDeviceAddr()));
        Log.v("PACKET: ", "DEST DEV ADDR: " + packet_props.unpackByteArr(this.getDestDeviceAddr()));
        Log.v("PACKET: ", "MSG SEQ: " + packet_props.unpackByteArr(this.getMsgSeqNum()));
        Log.v("PACKET: ", "MSG SIG: " + packet_props.unpackByteArr(this.getMsgSignature()));
        Log.v("PACKET: ", "TIME STAMP: " + packet_props.unpackByteArr(this.getTimeStamp()));
        Log.v("PACKET: ", "BODY BYTE COUNT: " + packet_props.unpackByteArr(this.getBodyByteCount()));

        //Log.v("PACKET: ", "WHOLE HEADER: " + unpackByteArr(this.getHeaderBuffer()));
        Log.v("PACKET: ", "Header end");
    }

}
