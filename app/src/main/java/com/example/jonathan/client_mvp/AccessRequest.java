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

//    private static final int MAX_MSG_BODY_SIZE = 768;
    private static final int IPV4_LEN = 15;

    private static final int SIM_ID_BYTE_SIZE = 64;
    private static final int SIM_DEV_NAME_SIZE = 128;

    // --- cmd numbers ---
    // unicast commands
    /*private static final int UNICAST_BASE_CMD = 4096;
    private static final int UNICAST_ACC_REQ_CMD = 4099;
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
    */
    // -- message --
    private static final int MAX_UDP_MSGSIZE = 1024;

    //private static final int MSG_HEADER_SIZE = 28;
    //private static final int MAX_QU_MSG_BODY_SIZE = (MAX_UDP_MSGSIZE - MSG_HEADER_SIZE);

    //queue buffer index for copying to header.
    /*
    private static final int IDX_CMD = 0;
    private static final int IDX_SRCBRD = 4;
    private static final int IDX_DESTBRD = 8;
    private static final int IDX_SEQ = 12;
    private static final int IDX_SIGN = 16;
    private static final int IDX_TSTAMP = 20;
    private static final int IDX_BCNT = 24;
    private static final int IDX_BODY = 28;

    //queue buffer index for copying to body.
    private static final int IDX_RN = 0;
    private static final int IDX_DN = 4;
    private static final int IDX_CN = 132;
    private static final int IDX_1CN = 196;
    */

    private static final int CMD_OK = 4096;
    private static final int CMD_ERROR_ID_IDCOMPL_NOT_MATCH = 8193;
    private static final int CMD_ERROR_REMOTE_ACCESS_DENIED = 8194;

    DatagramSocket transferSocket;

    public AccessRequest(){

    }

    // csys_unicast_qdata: needed
        // csys_msg
            // msg_header

            // char body[]. string?

    // body_send_plain_id: needed
        // all

    public int test_request(String dest_ip, String dest_device_name, int dest_port, String card_id){


        AccReq_CreateReqPacket o_AccessReq = new AccReq_CreateReqPacket(dest_ip, dest_device_name, dest_port, card_id);
        byte[] accessRequestMsg = o_AccessReq.getReqMsg();

        // excluded right nnow is the thread info
        return this.unicast_udp_AR_transfer(accessRequestMsg, dest_ip, dest_port, 10);


    }

    public int unicast_udp_AR_transfer(byte[] sendPacket, String dest_ip, int dest_port, int timedout_sec) {

        InetAddress myaddr, remoteaddr;

        String herdcodedip = "192.168.2.21";
        int hardcodedport= 65000;
        try {
            remoteaddr = InetAddress.getByName(herdcodedip);
            transferSocket = new DatagramSocket();

            DatagramPacket packet_send = new DatagramPacket(sendPacket, sendPacket.length, remoteaddr, hardcodedport);

            //Log.v("PACEKT:", "sending " + AccReq_packet_props.unpackByteArr(sendPacket));

            transferSocket.send(packet_send); //for test comment out
            //transferSocket.close();
        } catch (IOException e){
            transferSocket.close();
            Log.v("ACCESS_REQ: ", "SEND IOException: " + e.toString());
            return -1;
        }

        // for receive
        byte[] receiveBuffer = new byte[MAX_UDP_MSGSIZE];
        Arrays.fill(receiveBuffer, (byte) 0);
        try {
            DatagramPacket packet_receive = new DatagramPacket(receiveBuffer, MAX_UDP_MSGSIZE);
            transferSocket.receive(packet_receive);
            int packetLen = packet_receive.getLength();
            // // packet.getData and packet.getLength
            AccReq_AnalyzeResponse AR_AR = new AccReq_AnalyzeResponse(receiveBuffer, packetLen);
            int cmdCode = AR_AR.checkCmdSegment();

            AccReq_Body AR_B = new AccReq_Body(receiveBuffer);

            switch(cmdCode) // based on the message command, create a return message and send it
            {
                case CMD_OK:    // cmd 4096
                    Log.v("RESPONSE: ", "udp_buf CMD_OK received: " + AccReq_packet_props.unpackByteArr(AR_B.getBodyFromMsg(packetLen)) + ": with " + packetLen + " bytes.");
                    // THis command meaning request accepted and door open?
                    transferSocket.close();
                    return 1;

                case CMD_ERROR_ID_IDCOMPL_NOT_MATCH:
                    Log.v("RESPONSE: ", "udp_buf CMD_ERROR_REMOTE_ACCESS_DENIED received: " + AR_B.getBodyFromMsg(packetLen) + ": with " + packetLen + " bytes.");
                    break;
                case CMD_ERROR_REMOTE_ACCESS_DENIED:
                    Log.v("RESPONSE: ", "udp_buf CMD_ERROR_REMOTE_ACCESS_DENIED received: " + AR_B.getBodyFromMsg(packetLen) + ": with " + packetLen + " bytes.");
                    break;
                default:

                    break;
            }
            transferSocket.close();

        } catch (SocketTimeoutException e){
            transferSocket.close();
            Log.v("ACCESS_REQ: ", "RECEIVE Timeout: " + e.toString());
            return -1;
        } catch (IOException e){
            transferSocket.close();
            Log.v("ACCESS_REQ: ", "RECEIVE IOException: " + e.toString());
            return -1;
        }

        return -1;
    }

/*
    public int send_request(char[] p_IPv4, char[] p_port, char[] p_device_name, char[] p_card_id){

        Log.v("TASK: ", "send_request: Starting ===");
        Log.v("TASK: ", "send_request: <parameters> ---");
        Log.v("TASK: ", "send_request: p_IPv4: " + p_IPv4.toString());
        Log.v("TASK: ", "send_request: p_port: " + p_port.toString());
        Log.v("TASK: ", "send_request: p_device_name: " + p_device_name.toString());
        Log.v("TASK: ", "send_request: p_card_id: " + p_card_id.toString());
        Log.v("TASK: ", "send_request: </parameters> ---");

        AR_csys_unicast_qdata uc_qdata = new AR_csys_unicast_qdata(MAX_MSG_BODY_SIZE, IPV4_LEN);
        AR_csys_unicast_qdata ret_qdata = new AR_csys_unicast_qdata(MAX_MSG_BODY_SIZE, IPV4_LEN);

        AR_body_send_plain_id msg_body = new AR_body_send_plain_id(SIM_DEV_NAME_SIZE, SIM_ID_BYTE_SIZE);
        // bzero(&msg_body, sizeof(body_send_plain_id);
        // assuming 0'ing all data structures inside struct, so i'll initialize all with 0
        msg_body.zeroAll();

        msg_body.setRandom_num(0x12345678);
        msg_body.setDevice_name(p_device_name, SIM_DEV_NAME_SIZE); //SIM_DEV_NAME_SIZE

        char[] p_dst_straight = msg_body.getData_straight_text();
        char[] p_dst_compl = msg_body.getData_complement_text();
        int j = 0;
        for(int i = 0; i<SIM_ID_BYTE_SIZE; i++){
            if(p_card_id[i] != '\0'){
                p_dst_straight[j] = p_card_id[i];
                p_dst_compl[j] = (char) (~(p_card_id[i]));
                j ++;
            } else {
                break;
            }
        }

        Log.v("TASK: ", "send_request: <copy> ---");
        Log.v("TASK: ", "send_request: p_dst_straight: " + p_dst_straight.toString());
        Log.v("TASK: ", "send_request: p_dst_compl: " + p_dst_compl.toString());
        Log.v("TASK: ", "send_request: </copy> ---");

        uc_qdata.zeroAll();
        ret_qdata.zeroAll();

        uc_qdata.msg.head.setCmd(CMD_DEV_SEND_PLAIN_ID);
        uc_qdata.msg.head.setMbrd_addr(1);
        uc_qdata.msg.head.setDest_addr(2);
        uc_qdata.msg.head.setSequence(0);
        uc_qdata.msg.head.setSignature(0);

        uc_qdata.msg.head.setTimestamp(0); // find out how to get current time
        // assuming byte count at this point:
        int byteCount = msg_body.getSizeof();
        uc_qdata.msg.head.setBody_bytecount(byteCount);
        uc_qdata.msg.set_plainToBody(msg_body, byteCount);

        Log.v("TASK: ", "send_request: <uc_qdata> ---");
        Log.v("TASK: ", "send_request: msg_body size: " + byteCount);
        String s_body = new String(uc_qdata.msg.getBody());
        Log.v("TASK: ", "send_request: cys_msg body: " + s_body);


        // control attribute
        uc_qdata.send_tinf.setContinueRun(0); // true
        char[] acrq = new char[] {'a', 'c', 'r', 'q'};
        uc_qdata.send_tinf.setName_abbrev(acrq, 0, 4);
        char[] nu =  new char[] {'\0'};
        uc_qdata.send_tinf.setName_abbrev(nu, 4, 1);
        uc_qdata.send_tinf.setThread_id(0);

        //IP address
        int IPLEN = p_IPv4.length;
        uc_qdata.svr_net_info.setServer_ipv4(p_IPv4, IPLEN); // IPV4_LEN + 1
        String s_port = new String(p_port);
        uc_qdata.svr_net_info.setServer_port(Integer.parseInt(s_port));

        String s_server = new String(uc_qdata.svr_net_info.getServer_ipv4());

        Log.v("TASK: ", "send_request: svr_net_info server: " + s_server); // ignore trailing 0
        Log.v("TASK: ", "send_request: svr_net_info port: " + uc_qdata.svr_net_info.getServer_port());
        Log.v("TASK: ", "send_request: </uc_qdata> ---");

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

            // length of data to send
            int msg_len = out_qdata.msg.head.getSizeof() + in_qdata.msg.head.getBody_bytecount();

            Log.v("TASK: ", "unicast_udp_send: <out_qdata> ---" );
            Log.v("TASK: ", "unicast_udp_send: msg_len: " + msg_len);

            // omits padding in the structure
            //bzero(udp_buf, sizeof(udp_buf));
            Arrays.fill(udp_buf, (byte) 0);
            int ch_cnt = to_char_buf(udp_buf, in_qdata.msg);
            if (ch_cnt == 0) {
                Log.v("TASK: ", "unicast_udp_send. to_char_buf() failed.");
                out_qdata.send_tinf.setRet_value(-9);
                transferSocket.close();
                return -9;
            }

            Log.v("UDPSEND: ", "unicast_udp_send: </out_qdata> ---" );

            // debug dump
            Log.v("UDPSEND ", "Packet length of: " + ch_cnt);
            Log.v("UDPSEND: ", udp_buf.toString());

            /*
            DatagramPacket packet = new DatagramPacket(udp_buf, ch_cnt, remoteaddr, remote_port);
            try {
                transferSocket.send(packet);
            } catch (IOException e) {
                // send failed
                Log.v("TASK: ", "unicast_udp_send. send() failed. msg_len= " + msg_len + ", errno= " + e.toString());
                return -5;
            }

            Log.v("TASK: ", "unicast_udp_send. send() OK. msg_len= " + msg_len + ", sent_bytes= " + packet.getLength());


            // wait for recieve
            Arrays.fill(udp_buf, (byte) 0);
            DatagramPacket reqPacket = new DatagramPacket(udp_buf, MAX_UDP_MSGSIZE);
            try {
                transferSocket.receive(reqPacket);
            } catch (SocketTimeoutException e){
                // timed out
                Log.v("TASK: ", "unicast_udp_send. Receive timed out. errno= " + e.toString());
                out_qdata.send_tinf.setRet_value(-6);
                transferSocket.close();
                return -6;
            }catch (IOException e){
                // receive failed
                Log.v("TASK: ", "unicast_udp_send. Receive failed. errno= " + e.toString());
                out_qdata.send_tinf.setRet_value(-7);
                transferSocket.close();
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

                    Log.v("TASK: ", "unicast_udp_send. Receive OK. out_qdata msg head cmd= " + out_qdata.msg.head.getCmd() + ", bytes= " + received_bytes);

                    switch(out_qdata.msg.head.getCmd()) // based on the message command, create a return message and send it
                    {
                        case CMD_OK:    // cmd 0
                            Log.v("TASK: ", "udp_buf CMD_OK received: " + out_qdata.msg.getBody() + ": with " + received_bytes + " bytes.");
                            // THis command meaning request accepted and door open?
                            //transferSocket.close();
                            // return 0;
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
            out_qdata.send_tinf.setRet_value(-1);
            Log.v("UDPSEND: ", "could not open socket : " + e);
            return -1;
            // return -3;
        } catch (UnknownHostException e){
            // host not valid
            Log.v("UDPSEND: ", "unknown host: " + e);
            return -11;
        }
        transferSocket.close();
        return 1; // message received had no error but request rejected?
    }

    public int to_char_buf(byte[] qu_msg, AR_csys_msg udp_msg){

        Log.v("TASK: ", "<to_char_buf> --- ");

        StringBuilder buffer = new StringBuilder();
        for(int i = 0; i < qu_msg.length;i++){
            buffer.append(qu_msg[i]);
        }

        //get char array
        char[] ch = buffer.toString().toCharArray();

        System.arraycopy(udp_msg.head.getCmd(), 0, ch, IDX_CMD, udp_msg.head.getCmd().length);     // i2: 4
        System.arraycopy(udp_msg.head.getMbrd_addr(), 0, ch, IDX_SRCBRD, udp_msg.head.getMbrd_addr().length);
        System.arraycopy(udp_msg.head.getDest_addr(), 0, ch, IDX_DESTBRD, udp_msg.head.getDest_addr().length);
        System.arraycopy(udp_msg.head.getSequence(), 0, ch, IDX_SEQ, udp_msg.head.getSequence().length);
        System.arraycopy(udp_msg.head.getSignature(), 0, ch, IDX_SIGN, udp_msg.head.getSignature().length);
        System.arraycopy(udp_msg.head.getTimestamp(), 0, ch, IDX_TSTAMP, udp_msg.head.getTimestamp().length);

        if(udp_msg.head.getBody_bytecount() < MAX_MSG_BODY_SIZE){
            System.arraycopy(udp_msg.head.getBody_bytecountCHAR(),0, ch, IDX_BCNT, udp_msg.head.getBody_bytecountCHAR().length); //i2: 4
            System.arraycopy(udp_msg.getBody(), 0, ch, IDX_BODY, udp_msg.head.getBody_bytecount());

            String s_ch = new String(ch);
            Log.v("TASK: ", "to_char_buf: qu_msg: <MAX_MSG_BODY_SIZE. " + s_ch);
            Log.v("TASK: ", "to_char_buf: qu_msg: return. " + (MSG_HEADER_SIZE + udp_msg.head.getBody_bytecount()));

            Log.v("TASK: ", "</to_char_buf> --- ");
            qu_msg = new String(ch).getBytes();
            return (MSG_HEADER_SIZE + udp_msg.head.getBody_bytecount());

        } else {
            ch[IDX_BCNT] = MAX_MSG_BODY_SIZE;
            System.arraycopy(udp_msg.getBody(), 0, ch, IDX_BODY, MAX_MSG_BODY_SIZE);

            String s_ch = new String(ch);
            Log.v("TASK: ", "to_char_buf: qu_msg: else. " + s_ch);
            Log.v("TASK: ", "</to_char_buf> --- ");

            qu_msg = new String(ch).getBytes();
            return 0;
        }
    }

    public int to_csys_msg(AR_csys_msg udp_msg, byte[] qu_msg){

        Log.v("TASK: ", "<to_csys_msg> ---" );

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

            String s_body = new String(udp_msg.getBody());
            Log.v("TASK: ", "to_csys_msg: udp_msg body: <MAX_MSG_BODY_SIZE. " + s_body);
            Log.v("TASK: ", "to_csys_msg: return size: " + MSG_HEADER_SIZE + udp_msg.head.getBody_bytecount());

            //qu_msg = new String(ch).getBytes();
            return (MSG_HEADER_SIZE + udp_msg.head.getBody_bytecount());
        } else {
            udp_msg.head.setBody_bytecount(MAX_MSG_BODY_SIZE);
            udp_msg.setBody(ch, IDX_BODY, 0, MAX_MSG_BODY_SIZE);

            String s_body = new String(udp_msg.getBody());
            Log.v("TASK: ", "to_csys_msg: udp_msg body: else. " + s_body);

            //qu_msg = new String(ch).getBytes();
            return 0;
        }
    }
    */
}

