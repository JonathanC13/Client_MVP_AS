package com.example.jonathan.client_mvp;

import android.app.DownloadManager;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

public class AccessRequest {

    private static final int MAX_MSG_BODY_SIZE = 768;
    private static final int IPV4_LEN = 15;

    private static final int SIM_ID_BYTE_SIZE = 64;
    private static final int SIM_DEV_NAME_SIZE = 128;

    // --- cmd numbers ---
    // unicast commands
    private static final int UNICAST_BASE_CMD = 4096;

    private static final int CMD_DEV_SEND_PLAIN_ID = (3 + UNICAST_BASE_CMD);

    // -- message --
    private static final int MAX_UDP_MSGSIZE = 1024;

    private static final int MSG_HEADER_SIZE = 28;
    private static final int MAX_QU_MSG_BODY_SIZE = (MAX_UDP_MSGSIZE - MSG_HEADER_SIZE);

    //queue buffer index for copying.
    private static final int IDX_CMD = 0;
    private static final int IDX_SRCBRD = 4;
    private static final int IDX_DESTBRD = 8;
    private static final int IDX_SEQ = 12;
    private static final int IDX_SIGN = 16;
    private static final int IDX_TSTAMP = 20;
    private static final int IDX_BCNT = 24;
    private static final int IDX_BODY = 28;

    public AccessRequest(){

    }

    // csys_unicast_qdata: needed
        // csys_msg
            // msg_header

            // char body[]. string?

    // body_send_plain_id: needed
        // all

    public int send_request(char[] p_IPv4, char[] p_port, char[] p_device_name, char[] p_card_id){
        AR_csys_unicast_qdata uc_qdata = new AR_csys_unicast_qdata(MAX_MSG_BODY_SIZE, IPV4_LEN);
        AR_csys_unicast_qdata ret_qdata = new AR_csys_unicast_qdata(MAX_MSG_BODY_SIZE, IPV4_LEN);

        AR_body_send_plain_id msg_body = new AR_body_send_plain_id(SIM_DEV_NAME_SIZE, SIM_ID_BYTE_SIZE);
        // bzero(&msg_body, sizeof(body_send_plain_id);
        // assuming 0'ing all data structures inside struct, so i'll initialize all with 0
        msg_body.zeroAll();

        msg_body.setRandom_num(0x12345678);
        msg_body.setDevice_name(p_device_name, SIM_DEV_NAME_SIZE);

        char[] p_dst_straight = msg_body.getData_straight_text();
        char[] p_dst_compl = msg_body.getData_complement_text();
        int j = 0;
        for(int i = 0; i<SIM_ID_BYTE_SIZE; i++){
            if(p_card_id[i] != '\0'){
                p_dst_straight[j] = p_card_id[i];
                p_dst_compl[j] = (char) (~(p_card_id[i]));
                j ++;
            }
        }

        uc_qdata.zeroAll();
        ret_qdata.zeroAll();

        uc_qdata.setCmd(CMD_DEV_SEND_PLAIN_ID);
        uc_qdata.setMbrd_addr(1);
        uc_qdata.setDest_addr(2);
        uc_qdata.setSequence(0);
        uc_qdata.setSignature(0);

        uc_qdata.setTimestamp(0);
        // assuming byte count at this point:
        int byteCount = msg_body.getSizeof();
        System.arraycopy(msg_body, 0, uc_qdata, 0, byteCount);

        // control attribute

        //IP address
        uc_qdata.setServer_ipv4(p_IPv4, IPV4_LEN + 1);
        String s_port = new String(p_port);
        uc_qdata.setServer_port(Integer.parseInt(s_port));

        return unicast_udp_send(uc_qdata, ret_qdata, 10);

    }

    public int unicast_udp_send(AR_csys_unicast_qdata in_qdata, AR_csys_unicast_qdata out_qdata, int timedout_sec){
        int sock = 0;
        InetAddress myaddr, remoteaddr;
        DatagramSocket transferSocket;
        char[] udp_buf = new char[MAX_UDP_MSGSIZE + 1];

        try {
            // Connect to a 'free' socket as the local TID.
            transferSocket = new DatagramSocket();
            // set timedout for socket
            transferSocket.setSoTimeout(timedout_sec);

            int remote_port = in_qdata.getServer_port();
            char[] remoteIP = in_qdata.getServer_ipv4();
            String s_remoteIP = new String(remoteIP);
            remoteaddr = InetAddress.getByName(s_remoteIP);

            // size of msg_header
            int msg_headerSize = 7 * (Integer.BYTES);

            // length of data to send
            int msg_len = msg_headerSize + in_qdata.getBody_bytecount();

            // omits padding in the structure
            //bzero(udp_buf, sizeof(udp_buf));
            Arrays.fill(udp_buf, '0');
            int ch_cnt = ;

            transferSocket.send()

        } catch (SocketException e){
            // could not open a socket
            return -1;
        } catch (UnknownHostException e){
            // host not valid
            return -2;
        }

        return 0;
    }

    public int to_char_buf(char[] qu_msg, AR_csys_unicast_qdata udp_msg){

        System.arraycopy(udp_msg.getCmd(), 0, qu_msg, IDX_CMD, 4);
        System.arraycopy(udp_msg.getMbrd_addr(), 0, qu_msg, IDX_SRCBRD, 4);
        System.arraycopy(udp_msg.getDest_addr(), 0, qu_msg, IDX_DESTBRD, 4);
        System.arraycopy(udp_msg.getSequence(), 0, qu_msg, IDX_SEQ, 4);
        System.arraycopy(udp_msg.getSignature(), 0, qu_msg, IDX_SIGN, 4);
        System.arraycopy(udp_msg.getTimestamp(), 0, qu_msg, IDX_TSTAMP, 4);

        if(udp_msg.getBody_bytecount() < MAX_MSG_BODY_SIZE){
            System.arraycopy(udp_msg.getBody_bytecount(),0, qu_msg, IDX_BCNT, 4);
            System.arraycopy(udp_msg.getBody(), 0, qu_msg, IDX_BODY, udp_msg.getBody_bytecount());
            return (MSG_HEADER_SIZE + udp_msg.getBody_bytecount());

        } else {
            qu_msg[IDX_BCNT] = MAX_MSG_BODY_SIZE;
            System.arraycopy(udp_msg.getBody(), 0, qu_msg, IDX_BODY, MAX_MSG_BODY_SIZE);
            return 0;
        }
    }
}

