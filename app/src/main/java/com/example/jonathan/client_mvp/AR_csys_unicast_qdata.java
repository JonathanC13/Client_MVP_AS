package com.example.jonathan.client_mvp;

import java.util.Arrays;

public class AR_csys_unicast_qdata {

    AR_csys_msg msg;
    AR_thread_info send_tinf;
    AR_net_info svr_net_info;

    public AR_csys_unicast_qdata(int MAX_MSG_BODY_SIZE, int IPV4_LEN){
        msg = new AR_csys_msg(MAX_MSG_BODY_SIZE + 1);
        send_tinf = new AR_thread_info();
        svr_net_info = new AR_net_info(IPV4_LEN);
    }

    public void zeroAll(){
        msg.zeroAll();
        send_tinf.zeroAll();
        svr_net_info.zeroAll();
    }

}
