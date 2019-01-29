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
        SizeOf getSizeOf = new SizeOf();
        return (7 * getSizeOf.get_sizeOf(int.class));
    }

    public void setCmd(int cmd) {
        this.cmd = cmd;
    }

    public char[] getCmd() {

        char[] c_cmd = ("" + cmd).toCharArray();

        return c_cmd;
    }

    public void setMbrd_addr(int mbrd_addr) {
        this.mbrd_addr = mbrd_addr;
    }

    public char[] getMbrd_addr() {
        char[] c_mbrd_addr = ("" + mbrd_addr).toCharArray();

        return c_mbrd_addr;
    }

    public void setDest_addr(int dest_addr) {
        this.dest_addr = dest_addr;
    }

    public char[] getDest_addr() {
        char[] c_dest_addr = ("" + dest_addr).toCharArray();

        return c_dest_addr;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public char[] getSequence() {
        char[] c_sequence = ("" + sequence).toCharArray();

        return c_sequence;

    }

    public void setSignature(int signature) {
        this.signature = signature;
    }

    public char[] getSignature() {
        char[] c_signature = ("" + signature).toCharArray();

        return c_signature;

    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public char[] getTimestamp() {
        char[] c_timestamp = ("" + timestamp).toCharArray();

        return c_timestamp;
    }

    public void setBody_bytecount(int body_bytecount) {
        this.body_bytecount = body_bytecount;
    }

    public int getBody_bytecount() {
        return body_bytecount;
    }

    public char[] getBody_bytecountCHAR(){
        char[] c_body_bytecount = ("" + body_bytecount).toCharArray();

        return c_body_bytecount;
    }

}
