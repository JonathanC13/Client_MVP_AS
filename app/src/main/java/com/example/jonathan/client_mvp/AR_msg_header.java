package com.example.jonathan.client_mvp;

public class AR_msg_header {

    private int cmd;
    private int mbrd_addr;
    private int dest_addr;
    private int sequence;
    private int signature;
    private int timestamp;
    private int body_bytecount;

    AR_csys_msg msg;

    public AR_msg_header(){
        cmd = 0;
        mbrd_addr = 0;
        dest_addr = 0;
        sequence = 0;
        signature = 0;
        timestamp = 0;
        body_bytecount = 0;
    }

    public void zeroAll(){
        cmd = 0;
        mbrd_addr = 0;
        dest_addr = 0;
        sequence = 0;
        signature = 0;
        timestamp = 0;
        body_bytecount = 0;
    }

    public int getSizeof(){
        return (7 * Integer.BYTES);
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

}
