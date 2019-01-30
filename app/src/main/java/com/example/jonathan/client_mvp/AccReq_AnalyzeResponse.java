package com.example.jonathan.client_mvp;

import java.net.DatagramPacket;

public class AccReq_AnalyzeResponse {

    byte[] packet_rec;
    int packetLen;

    AccReq_packet_props packet_props;

    public AccReq_AnalyzeResponse(byte[] pack_rec_buff, int pack_len){
        packet_props = new AccReq_packet_props();
        packetLen = pack_len;
        packet_rec = pack_rec_buff;


        // Don't need to confirm TID of server since one transaction response

    }

    // At this point only checking cmd segment
    protected int checkCmdSegment(){
        AccReq_Header AR_header = new AccReq_Header(packet_rec, packet_props);
        if(packetLen >= packet_props.MSG_HEADER_SIZE){
            // received message is well formed, has a valid header
            byte[] b_recCmd = AR_header.getCmd();
            int i_cmd = java.nio.ByteBuffer.wrap(b_recCmd).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
            return i_cmd;
        }
        return -1;
    }

}
