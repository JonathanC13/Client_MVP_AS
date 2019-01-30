package com.example.jonathan.client_mvp;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class AccReq_packet_props {

    // --- cmd numbers ---
    // unicast commands
    protected static final int UNICAST_BASE_CMD = 4096;
    protected static final int UNICAST_ACC_REQ_CMD = 4099;

    protected static final int MSG_HEADER_SIZE = 28;
    protected static final int MAX_MSG_BODY_SIZE = 768;
    protected static final int MAX_WHOLE_MSG_SIZE = MSG_HEADER_SIZE + MAX_MSG_BODY_SIZE;
    protected static final int MAX_UDP_MSGSIZE = 1024;

    //sizes for the header segments
    protected final static int header_CMD_SIZE = 4;
    protected final static int header_SDA_SIZE = 4;
    protected final static int header_DDA_SIZE = 4;
    protected final static int header_MSQ_SIZE = 4;
    protected final static int header_MSE_SIZE = 4;
    protected final static int header_TMS_SIZE = 4;
    protected final static int header_MBC_SIZE = 4;

    //sizes for the body segments
    protected int body_randNum_SIZE = 4;
    protected int body_devName_SIZE = 128;
    protected int body_cardNum_SIZE = 64;
    protected int body_1comp_cardNum_SIZE = 64;

    //queue buffer index for copying to header.
    protected static final int IDX_CMD = 0;
    protected static final int IDX_SRCBRD = 4;
    protected static final int IDX_DESTBRD = 8;
    protected static final int IDX_SEQ = 12;
    protected static final int IDX_SIGN = 16;
    protected static final int IDX_TSTAMP = 20;
    protected static final int IDX_BCNT = 24;
    protected static final int IDX_BODY = 28;

    //queue buffer index for copying to body.
    protected static final int IDX_RN = 0;
    protected static final int IDX_DN = 4;
    protected static final int IDX_CN = 132;
    protected static final int IDX_1CN = 196;

    public AccReq_packet_props(){


    }

    public static String unpackByteArr(byte[] b_arr){
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

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public void zeroSection(byte[] destArr, int index, int length){
        byte[] z = new byte[length];
        Arrays.fill(z, (byte) 0);
        System.arraycopy(z, 0, destArr, index, length);
    }


    public static byte[] changeEndian(byte[] b_arr){
        int in_len = b_arr.length;
        byte[] ret_arr = new byte[in_len];

        for(int i = 0; i < in_len; i ++){
            ret_arr[i] = b_arr[in_len - i -1];
        }

        return ret_arr;
    }

}
