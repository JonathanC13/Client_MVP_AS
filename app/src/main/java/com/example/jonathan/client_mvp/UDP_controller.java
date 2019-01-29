package com.example.jonathan.client_mvp;

import android.content.IntentFilter;
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
import java.util.Arrays;

public class UDP_controller {

    // IP to RPi
    private char[] remoteAddress = null;

    // Port open on that Pi, a constant that the developers need to know to hard code
    private char[] remotePort = null;

    // device name ?? . Need to set this
    //private char[] deviceName = null;
    private char [] deviceName = null;
    private static final int SIM_DEV_NAME_SIZE = 128;

    // card id of the employee;
    private char[] employee_id_toSend = null;


    String s_ip;
    String s_devName;
    int i_port;
    String s_id;

    public int success = -1;
    public UDP_controller(String IP, String em_id){

        this.remoteAddress = IP.toCharArray();
        String s_remotePort = "69";
        this.remotePort = s_remotePort.toCharArray();

        deviceName = new char[SIM_DEV_NAME_SIZE];
        String phone = "aPhone";
        //Arrays.fill(this.deviceName, '0');
        char[] c_deviceName = phone.toCharArray();
        int c_deviceNameLen = c_deviceName.length;

        if(c_deviceNameLen < SIM_DEV_NAME_SIZE){
            // arraycopy(Object src, int srcPos, Object dest, int destPos, int length)
            System.arraycopy(c_deviceName, 0, this.deviceName, 0, c_deviceNameLen);
            //fill(char[] a, int fromIndex, int toIndex, char val). From is inclusive and To in exclusive
            Arrays.fill(deviceName, c_deviceNameLen, SIM_DEV_NAME_SIZE, '0');

        } else {
            System.arraycopy(c_deviceName, 0, this.deviceName, 0, SIM_DEV_NAME_SIZE);
        }


        char[] c_employee_id = em_id.toCharArray();
        int c_idLen = c_employee_id.length;
        employee_id_toSend = new char[c_idLen+1];
        System.arraycopy(c_employee_id, 0, employee_id_toSend, 0, c_idLen);
        employee_id_toSend[c_idLen] = '\0';

    }

    public int executeUDP(){
        try {
            int ret_Succ = new ReqPi().execute().get(); // this thread waits for response
            success = ret_Succ;
        } catch (Exception e){

        }
        // then returns response
        return success;
    }

    private class ReqPi extends AsyncTask<Void, Void, Integer> {

        public void ReqPi() {
        }

        protected Integer doInBackground(Void... args) {

            AccessRequest AR = new AccessRequest();
            //int successflag = AR.send_request(remoteAddress, remotePort, deviceName, employee_id_toSend);
            // String ip, String device_name,int port, String card_id)
            int successflag = AR.test_request(s_ip, s_devName, i_port, s_id);
            return successflag;

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
