package com.example.jonathan.client_mvp;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class UDP_controller {
    private DatagramSocket socket;
    private byte[] buf;
    private InetAddress IPdst;

    public UDP_controller(String IP){
        try {
            IPdst = InetAddress.getByName(IP);
        } catch (UnknownHostException e){
            //
        }
    }

    public int send(){
        return 5;
    }

    public void close(){
        socket.close();
    }

}
