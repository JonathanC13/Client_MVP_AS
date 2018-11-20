package com.example.jonathan.client_mvp;

import java.util.Arrays;

public class AR_csys_msg {

    AR_msg_header head;
    private char[] body;

    public AR_csys_msg(int MAX_MSG_BODY_SIZE){
        head = new AR_msg_header();
        body = new char[MAX_MSG_BODY_SIZE];
    }

    public void zeroAll(){
        head.zeroAll();
        Arrays.fill(this.body, '0');
    }

    public void set_plainToBody(AR_body_send_plain_id in, int byteC){

        body = new char[byteC];

        int offset = 0;

        System.arraycopy(in.getRandom_num(), 0, body, offset, in.getRandom_numSize());
        offset += in.getRandom_numSize();

        System.arraycopy(in.getDevice_name(), 0, body, offset, in.getDevice_nameSize());
        offset += in.getDevice_nameSize();

        System.arraycopy(in.getData_straight_text(), 0, body, offset, in.getDataStraightSize());
        offset += in.getDataStraightSize();

        System.arraycopy(in.getData_complement_text(), 0, body, offset, in.getDataComplSize());

    }

    public void setBody(char[] arr, int src_pos, int dst_pos, int size) {
        System.arraycopy(arr, src_pos, body, dst_pos, size);
    }

    public void setIndBody(int pos, char in){
        body[pos] = in;
    }

    public char[] getBody() {
        return body;
    }
}
