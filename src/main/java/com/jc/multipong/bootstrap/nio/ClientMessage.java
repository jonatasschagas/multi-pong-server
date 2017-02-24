package com.jc.multipong.bootstrap.nio;

import java.nio.channels.SocketChannel;

/**
 * Created by jchagas on 22/02/2017.
 */
public class ClientMessage {

    private SocketChannel socket;
    private String message;

    public ClientMessage(SocketChannel socket, String message) {
        this.socket = socket;
        this.message = message;
    }

    public SocketChannel getSocket() {
        return socket;
    }

    public void setSocket(SocketChannel socket) {
        this.socket = socket;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
