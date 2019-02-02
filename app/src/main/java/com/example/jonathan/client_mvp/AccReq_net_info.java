package com.example.jonathan.client_mvp;

import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class AccReq_net_info {

    private int connected;
    private int server_port;
    private byte[] server_ipv4;
    private int IPV4_LEN;

    public AccReq_net_info(int IPV4_BYTES_LEN){
        this.IPV4_LEN = IPV4_BYTES_LEN;
        this.server_ipv4 = new byte[IPV4_BYTES_LEN];
    }

    public void zeroAll(){
        connected = 0;
        this.server_port = 0;
        Arrays.fill(this.server_ipv4, (byte) 0);
    }

    public void setServer_port(int p_port){

        this.server_port = p_port;
    }

    public int getServer_port(){

        return server_port;
    }

    public void setServer_ipv4(String p_IPV4){

        InetAddress inet_ip;
        try {
            inet_ip = InetAddress.getByName(p_IPV4);
            byte[] bytes = inet_ip.getAddress();

            int b_len = bytes.length;

            // to little endian
            byte[] rev = new byte[b_len];
            for(int i = 0; i < b_len; i ++){
                if(i < IPV4_LEN){
                    rev[i] = bytes[b_len - i - 1];
                }
            }

            if(rev.length > IPV4_LEN){
                // if rev size is greater than the IPV4 segment in the header, then trim to segment size
                try {
                    System.arraycopy(rev, 0, server_ipv4, 0, IPV4_LEN);
                } catch (Exception e){
                    Log.v("PACKET: ", "setServerIp, ip length > IPV4 segment length, attempted to trim to segment but err: " + e.toString());
                }
            } else {
                try {
                    System.arraycopy(rev, 0, server_ipv4, 0, rev.length);
                } catch (Exception e){
                    Log.v("PACKET: ", "setServerIp, attempted to copy to segment but err: " + e.toString());
                }
            }


        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public byte[] getServer_ipv4(){
        return server_ipv4;
    }

    public int getServerIP_Len(){
        return server_ipv4.length;
    }

}
