package com.example.jonathan.client_mvp;

import android.app.DownloadManager;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class AccessRequest {

    private static final int MAX_MSG_BODY_SIZE = 768;
    private static final int IPV4_LEN = 15;

    private static final int SIM_ID_BYTE_SIZE = 64;
    private static final int SIM_DEV_NAME_SIZE = 128;

    // --- cmd numbers ---
    // unicast commands
    private static final int UNICAST_BASE_CMD = 4096;
    private static final int CMD_OK = (0 + UNICAST_BASE_CMD);

    private static final int CMD_DEV_SEND_PLAIN_ID = (3 + UNICAST_BASE_CMD);
    private static final int CMD_DEV_SEND_ENCRYPTED_ID = (4 + UNICAST_BASE_CMD);
    private static final int CMD_RPI_SEND_ENCRYPTED_ID = (5 + UNICAST_BASE_CMD);
    private static final int CMD_ENCRYPT_WITH_DEFAULT = (6 + UNICAST_BASE_CMD);

    // error sub commands
    private static final int ERROR_BASE_CMD = 8192;
    private static final int CMD_ERROR = (0 + ERROR_BASE_CMD);
    private static final int CMD_ERROR_ID_IDCOMPL_NOT_MATCH = (1 + ERROR_BASE_CMD);
    private static final int CMD_ERROR_REMOTE_ACCESS_DENIED = (2 + ERROR_BASE_CMD);

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

        uc_qdata.msg.head.setCmd(CMD_DEV_SEND_PLAIN_ID);
        uc_qdata.msg.head.setMbrd_addr(1);
        uc_qdata.msg.head.setDest_addr(2);
        uc_qdata.msg.head.setSequence(0);
        uc_qdata.msg.head.setSignature(0);

        uc_qdata.msg.head.setTimestamp(0);
        // assuming byte count at this point:
        int byteCount = msg_body.getSizeof();
        System.arraycopy(msg_body, 0, uc_qdata.msg.getBody(), 0, byteCount);

        // control attribute
        uc_qdata.send_tinf.setContinueRun(0); // true
        char[] acrq = new char[] {'a', 'c', 'r', 'q'};
        uc_qdata.send_tinf.setName_abbrev(acrq, 0, 4);
        char[] nu =  new char[] {'\0'};
        uc_qdata.send_tinf.setName_abbrev(nu, 4, 1);
        uc_qdata.send_tinf.setThread_id(0);

        //IP address
        uc_qdata.svr_net_info.setServer_ipv4(p_IPv4, IPV4_LEN + 1);
        String s_port = new String(p_port);
        uc_qdata.svr_net_info.setServer_port(Integer.parseInt(s_port));

        return unicast_udp_send(uc_qdata, ret_qdata, 10);

    }

    public int unicast_udp_send(AR_csys_unicast_qdata in_qdata, AR_csys_unicast_qdata out_qdata, int timedout_sec){
        int sock = 0;
        InetAddress myaddr, remoteaddr;
        DatagramSocket transferSocket;
        //char[] udp_buf = new char[MAX_UDP_MSGSIZE + 1];
        byte[] udp_buf = new byte[MAX_UDP_MSGSIZE + 1];

        out_qdata.zeroAll();

        try {
            // Connect to a 'free' socket as the local TID.
            transferSocket = new DatagramSocket();
            // set timedout for socket
            transferSocket.setSoTimeout(timedout_sec);

            int remote_port = in_qdata.svr_net_info.getServer_port();
            char[] remoteIP = in_qdata.svr_net_info.getServer_ipv4();
            String s_remoteIP = new String(remoteIP);
            remoteaddr = InetAddress.getByName(s_remoteIP);

            // size of msg_header
            int msg_headerSize = 7 * (Integer.BYTES);

            // length of data to send
            int msg_len = out_qdata.msg.head.getSizeof() + in_qdata.msg.head.getBody_bytecount();

            // omits padding in the structure
            //bzero(udp_buf, sizeof(udp_buf));
            Arrays.fill(udp_buf, (byte) 0);
            int ch_cnt = to_char_buf(udp_buf, in_qdata.msg);
            if (ch_cnt == 0) {
                Log.v("TASK: ", "unicast_udp_send. to_char_buf() failed.");
                transferSocket.close();
                return -9;
            }

            // debug dump


            DatagramPacket packet = new DatagramPacket(udp_buf, ch_cnt, remoteaddr, remote_port);
            try {
                transferSocket.send(packet);
            } catch (IOException e) {
                // send failed
                return -5;
            }

            Log.v("TASK: ", "unicast_udp_send. send() OK. msg_len= " + msg_len + ", sent_bytes= ");


            // wait for recieve
            Arrays.fill(udp_buf, (byte) 0);
            DatagramPacket reqPacket = new DatagramPacket(udp_buf, MAX_UDP_MSGSIZE);
            try {
                transferSocket.receive(reqPacket);
            } catch (SocketTimeoutException e){
                // timed out
                return -6;
            }catch (IOException e){
                // receive failed
                return -7;
            }
            int received_bytes = reqPacket.getLength();
            Log.v("TASK: ", "unicast_udp_send. receive source IP/Port= " + reqPacket.getAddress() + "/ " + reqPacket.getPort() + ", received_bytes= " + received_bytes);

            // check if receive message is well formed.
            if(received_bytes >= MSG_HEADER_SIZE){

                out_qdata.msg.zeroAll();
                int ch_cnt2 = to_csys_msg(out_qdata.msg, udp_buf);

                if(ch_cnt2 != 0){
                    out_qdata.send_tinf.setRet_value(0);
                    char[] p = out_qdata.msg.getBody();
                    p[out_qdata.msg.head.getBody_bytecount()] = 0; // terminate the message body with NULL in case it is used in string manipulation

                    Log.v("TASK: ", "unicast_udp_send. Receive OK. cmd= " + out_qdata.msg.head.getCmd() + ", bytes= " + received_bytes);

                    switch(out_qdata.msg.head.getCmd()) // based on the message command, create a return message and send it
                    {
                        case CMD_OK:    // cmd 0
                            Log.v("TASK: ", "udp_buf CMD_OK received: " + out_qdata.msg.getBody() + ": with " + received_bytes + " bytes.");
                            break;
                        case CMD_ERROR_ID_IDCOMPL_NOT_MATCH:
                            Log.v("TASK: ", "udp_buf CMD_ERROR_REMOTE_ACCESS_DENIED received: " + out_qdata.msg.getBody() + ": with " + received_bytes + " bytes.");
                            break;
                        case CMD_ERROR_REMOTE_ACCESS_DENIED:
                            Log.v("TASK: ", "udp_buf CMD_ERROR_REMOTE_ACCESS_DENIED received: " + out_qdata.msg.getBody() + ": with " + received_bytes + " bytes.");
                            break;
                        default:
                            break;
                    }
                } else {
                    Log.v("TASK: ", "unicast_udp_send. to_csys_msg() failed.");
                }

            } else { // error in format
                Log.v("TASK: ", "unicast_udp_send. receive less. bytes= " + received_bytes);
                transferSocket.close();
                out_qdata.send_tinf.setRet_value(-8);
                return out_qdata.send_tinf.getRet_value();
            }

        } catch (SocketException e){
            // could not open a socket
            return -1;
        } catch (UnknownHostException e){
            // host not valid

            return -2;
        }
        transferSocket.close();
        return 0;
    }

    public int to_char_buf(byte[] qu_msg, AR_csys_msg udp_msg){

        StringBuilder buffer = new StringBuilder();
        for(int i = 0; i < qu_msg.length;i++){
            buffer.append(qu_msg[i]);
        }

        //get char array
        char[] ch = buffer.toString().toCharArray();

        System.arraycopy(udp_msg.head.getCmd(), 0, ch, IDX_CMD, 4);
        System.arraycopy(udp_msg.head.getMbrd_addr(), 0, ch, IDX_SRCBRD, 4);
        System.arraycopy(udp_msg.head.getDest_addr(), 0, ch, IDX_DESTBRD, 4);
        System.arraycopy(udp_msg.head.getSequence(), 0, ch, IDX_SEQ, 4);
        System.arraycopy(udp_msg.head.getSignature(), 0, ch, IDX_SIGN, 4);
        System.arraycopy(udp_msg.head.getTimestamp(), 0, ch, IDX_TSTAMP, 4);

        if(udp_msg.head.getBody_bytecount() < MAX_MSG_BODY_SIZE){
            System.arraycopy(udp_msg.head.getBody_bytecount(),0, ch, IDX_BCNT, 4);
            System.arraycopy(udp_msg.getBody(), 0, ch, IDX_BODY, udp_msg.head.getBody_bytecount());
            qu_msg = new String(ch).getBytes();
            return (MSG_HEADER_SIZE + udp_msg.head.getBody_bytecount());

        } else {
            ch[IDX_BCNT] = MAX_MSG_BODY_SIZE;
            System.arraycopy(udp_msg.getBody(), 0, ch, IDX_BODY, MAX_MSG_BODY_SIZE);
            qu_msg = new String(ch).getBytes();
            return 0;
        }
    }

    public int to_csys_msg(AR_csys_msg udp_msg, byte[] qu_msg){

        StringBuilder buffer = new StringBuilder();
        for(int i = 0; i < qu_msg.length;i++){
            buffer.append(qu_msg[i]);
        }

        //get char array
        char[] ch = buffer.toString().toCharArray();

        udp_msg.head.setCmd(ch[IDX_CMD]);
        udp_msg.head.setMbrd_addr(ch[IDX_SRCBRD]);
        udp_msg.head.setDest_addr(ch[IDX_DESTBRD]);
        udp_msg.head.setSequence(ch[IDX_SEQ]);
        udp_msg.head.setTimestamp(ch[IDX_TSTAMP]);
        udp_msg.head.setBody_bytecount(ch[IDX_BCNT]);

        if(udp_msg.head.getBody_bytecount() < MAX_MSG_BODY_SIZE){
            udp_msg.setBody(ch, IDX_BODY, 0, udp_msg.head.getBody_bytecount());
            udp_msg.setIndBody(udp_msg.head.getBody_bytecount(), '\0');
            //qu_msg = new String(ch).getBytes();
            return (MSG_HEADER_SIZE + udp_msg.head.getBody_bytecount());
        } else {
            udp_msg.head.setBody_bytecount(MAX_MSG_BODY_SIZE);
            udp_msg.setBody(ch, IDX_BODY, 0, MAX_MSG_BODY_SIZE);
            //qu_msg = new String(ch).getBytes();
            return 0;
        }
    }
}

