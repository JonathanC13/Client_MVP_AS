package com.example.jonathan.client_mvp;

import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class AccReq_CreateReqPacket {

    private byte[] b_req_msg;
    private int currPacket_SIZE = 0;

    AccReq_packet_props pack_props;


    public AccReq_CreateReqPacket(String dest_ip, String dest_device_name, int dest_port, String card_id){
        pack_props = new AccReq_packet_props();

        // <initialize buffer to max size the packet should be>
        b_req_msg = new byte[pack_props.MAX_UDP_MSGSIZE];
        // </>

        // <get this (sender) IP>
        String s_senderIP = "";
        try {
            s_senderIP = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e){
            Log.v("PACKET: ", "Could not get local IP address, err: " + e.toString());
        }
        // </>

        Log.v("PACKET: ", "LOCAL IP: " + s_senderIP);


        AccReq_Header AR_header = new AccReq_Header(pack_props);
        AR_header.setCmd(pack_props.UNICAST_ACC_REQ_CMD);
        AR_header.setSenderDeviceAddr(s_senderIP, 69);
        AR_header.setDestDeviceAddr("192.168.2.21", 65000);
        AR_header.setMsgSequenceNum( 0);
        AR_header.setMsgSignature();
        AR_header.setTimeStamp();
        //AR_header.setBodyByteCount(IDX_BCNT, 260);

        AccReq_Body AR_body = new AccReq_Body(pack_props);
        AR_body.setRandomNum();
        AR_body.setDevName( "RDR5, C6:I0:R2");
        AR_body.setCardNum("41165377");
        AR_body.set1CompCardNum();
        AR_body.compactBody();

        AR_body.printBody();

        // ==
        AR_header.setBodyByteCount( AR_body.getBodySize());
        AR_header.printHeader();

        // copy header into request packet
        System.arraycopy(AR_header.getHeaderBuffer(),0,b_req_msg,0,AR_header.getHeaderBufferSIZE());

        int bodyLen = AR_body.getBodySize();
        // copy body
        if(bodyLen > pack_props.MAX_MSG_BODY_SIZE){
            // need trim
            System.arraycopy(AR_body.getBodyBuffer(), 0, b_req_msg, pack_props.IDX_BODY, pack_props.MAX_MSG_BODY_SIZE);
            currPacket_SIZE += AR_header.getHeaderBufferSIZE() + pack_props.MAX_MSG_BODY_SIZE;
        } else {
            // direct copy
            System.arraycopy(AR_body.getBodyBuffer(), 0, b_req_msg, pack_props.IDX_BODY, bodyLen);
            currPacket_SIZE += AR_header.getHeaderBufferSIZE() + bodyLen;
            // need to compact
            this.compactMsg();
        }

        Log.v("PACKET: ", "ENTIRE PACKET " + AR_header.packet_props.unpackByteArr(b_req_msg));
        // packet ready to send

    }

    public byte[] getReqMsg(){
        return b_req_msg;
    }

    protected void compactMsg(){
        if(currPacket_SIZE > 0 && currPacket_SIZE < pack_props.MAX_WHOLE_MSG_SIZE){
            byte[] tmp = new byte[currPacket_SIZE];
            System.arraycopy(b_req_msg, 0, tmp, 0, currPacket_SIZE);
            b_req_msg = tmp;
        } // else already at max
    }

}
