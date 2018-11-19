package com.example.jonathan.client_mvp;

import java.util.Arrays;

public class AR_net_info {

    private int connected;
    private int server_port;
    private char[] server_ipv4;

    public AR_net_info(int IPV4_LEN){
        this.server_ipv4 = new char[IPV4_LEN + 1];
    }

    public void zeroAll(){
        connected = 0;
        server_port = 0;
        Arrays.fill(this.server_ipv4, '0');
    }

    public void setServer_port(int p_port){
        this.server_port = p_port;
    }

    public int getServer_port(){
        return server_port;
    }

    public void setServer_ipv4(char[] p_IPV4, int size){
        System.arraycopy(p_IPV4, 0, server_ipv4, 0, size);
    }

    public char[] getServer_ipv4(){
        return server_ipv4;
    }
}
