package com.example.jonathan.client_mvp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;

public class UDP_controller {

    private InetAddress remoteAddress;
    private int remotePort;

    public int success = -1;
    public UDP_controller(String IP){
        try {
            this.remoteAddress = InetAddress.getByName(IP);

        } catch (UnknownHostException e){

        }
    }

    public int executeUDP(){
        try {
            Object obj = new ReqPi().execute().get(); // this thread waits for response
        } catch (Exception e){

        }
        // then returns response
        return success;
    }

    private class ReqPi extends AsyncTask<Void, Void, Void> {

        public void ReqPi() {
        }

        protected Void doInBackground(Void... args) {

            AccessRequest AR = new AccessRequest();
            AR.send_request();
            return null;
        }
        protected void onPostExecute(String file_url) {

        }
    }
/*
    public int executeUDP(){
        try {
            Object obj = new ReqPi().execute().get(); // this thread waits for response
        } catch (Exception e){

        }
        // then returns response
        return success;
    }

    private class ReqPi extends AsyncTask<Void, Void, Void> {

        // REEEEEEEEE
        Request rq;
        rq.send_request;

        // transfer socket
        private DatagramSocket transferSocket;

        private DatagramSocket socket;

        // set amount of bytes to send
        private byte[] sendBuff = new byte[??];

        // limit on bytes to receive
        private byte[] recBuff = new byte[??];
        private byte[] dataBuff;

        public void ReqPi() {
        }

        protected Void doInBackground(Void... args) {
            int success = 0;
            try {
                // Connect to a socket, Given a 'free port' as the local TID (transfer ID)
                transferSocket = new DatagramSocket();

                // fill message
                sendBuff = ??;

                // create datagram to send
                DatagramPacket packet = new DatagramPacket(sendBuff, sendBuff.length, remoteAddress, remotePort);
                transferSocket.send(packet); // send datagram

                // this thread will wait for response.
                DatagramPacket reqPacket = new DatagramPacket(recBuff, recBuff.length);
                transferSocket.setSoTimeout(5000); // Timeout of 5 seconds
                transferSocket.receive(reqPacket); // Blocks here to wait for response from Pi.
                // get data from received packet
                dataBuff = reqPacket.getData();

                // Do checking if the packet is of expected form and then check its contents
                // First maybe check size
                    // check contents, if all correct, success = 1. Which means the Pi has unlocked the door
                        success = 1;


            } catch (SocketException e){

            } catch (IOException e){

            } finally {
                transferSocket.close();
            }
            return null;
        }
        protected void onPostExecute(String file_url) {

        }
    }

    public int send(){
        return 5;
    }

    public void close(){
        socket.close();
    }
*/
}
