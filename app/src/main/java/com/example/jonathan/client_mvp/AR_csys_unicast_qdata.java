package com.example.jonathan.client_mvp;

import java.util.Arrays;

public class AR_csys_unicast_qdata {

    // msg_header
    private int cmd;
    private int mbrd_addr;
    private int dest_addr;
    private int sequence;
    private int signature;
    private int timestamp;
    private int body_bytecount;

    // contents
    private char[] body;

    // Net_info
    private int connected;
    private int server_port;
    private char[] server_ipv4;

    public AR_csys_unicast_qdata(int MAX_MSG_BODY_SIZE, int IPV4_LEN){

        this.body = new char[MAX_MSG_BODY_SIZE + 1];
        this.server_ipv4 = new char[IPV4_LEN + 1];
    }

    public void zeroAll(){
        // msg_header
        cmd = 0;
        mbrd_addr = 0;
        dest_addr = 0;
        sequence = 0;
        signature = 0;
        timestamp = 0;
        body_bytecount = 0;

        // contents
        Arrays.fill(this.body, '0');

        // Net_info
        connected = 0;
        server_port = 0;
        Arrays.fill(this.server_ipv4, '0');
    }

    public void setCmd(int cmd) {
        this.cmd = cmd;
    }

    public int getCmd() {
        return cmd;
    }

    public void setMbrd_addr(int mbrd_addr) {
        this.mbrd_addr = mbrd_addr;
    }

    public int getMbrd_addr() {
        return mbrd_addr;
    }

    public void setDest_addr(int dest_addr) {
        this.dest_addr = dest_addr;
    }

    public int getDest_addr() {
        return dest_addr;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSignature(int signature) {
        this.signature = signature;
    }

    public int getSignature() {
        return signature;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setBody_bytecount(int body_bytecount) {
        this.body_bytecount = body_bytecount;
    }

    public int getBody_bytecount() {
        return body_bytecount;
    }

    public void setBody(char[] body) {
        this.body = body;
    }

    public char[] getBody() {
        return body;
    }

    public void setConnected(int connected) {
        this.connected = connected;
    }

    public int getConnected() {
        return connected;
    }

    public void setServer_port(int server_port) {
        this.server_port = server_port;
    }

    public int getServer_port() {
        return server_port;
    }

    public void setServer_ipv4(char[] server_ipv4, int IPV4_LEN) {
        this.server_ipv4 = Arrays.copyOf(server_ipv4, IPV4_LEN);
    }

    public char[] getServer_ipv4() {
        return server_ipv4;
    }
}
