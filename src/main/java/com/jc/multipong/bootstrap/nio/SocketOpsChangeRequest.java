package com.jc.multipong.bootstrap.nio;

import java.nio.channels.SocketChannel;

/**
 * Created by jchagas on 22/02/2017.
 */
public class SocketOpsChangeRequest {

    public static final int REGISTER = 1;
    public static final int CHANGEOPS = 2;

    public SocketChannel socket;
    public int type;
    public int ops;

    public SocketOpsChangeRequest(SocketChannel socket, int type, int ops) {
        this.socket = socket;
        this.type = type;
        this.ops = ops;
    }

}
