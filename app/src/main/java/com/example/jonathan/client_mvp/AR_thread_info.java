package com.example.jonathan.client_mvp;

import java.util.Arrays;

public class AR_thread_info {

    //private Thread thread_id;
    private int thread_id;
    private int thread_num;
    private int continueRun;
    private int run_state;
    private char[] name_abbrev;
    private int dev_index;
    private int ret_value;
    private int piface_hw_addr;

    public AR_thread_info(){

        name_abbrev = new char[16];
    }

    public void zeroAll(){
        //Thread thread_id;
        thread_id = 0;
        thread_num = 0;
        continueRun = 0;
        run_state = 0;
        Arrays.fill(this.name_abbrev, '0');
        dev_index = 0;
        ret_value = 0;
        piface_hw_addr = 0;
    }

    public void setContinueRun(int continueRun) {
        this.continueRun = continueRun;
    }

    public int getContinueRun() {
        return continueRun;
    }

    public void setName_abbrev(char[] arr, int pos, int size){
        System.arraycopy(arr, 0, this.name_abbrev, pos, size);
    }

    public void setThread_id(int thread_id) {
        this.thread_id = thread_id;
    }

    public int getThread_id() {
        return thread_id;
    }

    public void setRet_value(int ret_value) {
        this.ret_value = ret_value;
    }

    public int getRet_value() {
        return ret_value;
    }
}
