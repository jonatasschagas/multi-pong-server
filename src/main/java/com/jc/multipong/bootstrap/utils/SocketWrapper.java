package com.jc.multipong.bootstrap.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

/**
 * Created by jonataschagas on 25/01/17.
 */
public class SocketWrapper {

    static final Logger logger = LoggerFactory.getLogger(SocketWrapper.class);

    private static final String MESSAGE_DELIMITER = "|";
    private Socket clientSocket;
    private InputStream input;
    private OutputStream output;


    public SocketWrapper(Socket clientSocket) {
        this.clientSocket = clientSocket;
        try {
            this.input = clientSocket.getInputStream();
            this.output = clientSocket.getOutputStream();
        } catch (IOException e) {
            logger.error("error opening input and output stream.", e);
        }
    }

    public boolean isConnected() {
        return clientSocket.isConnected();
    }

    public String readMessage() {
        try {
            BufferedReader messageReader = new BufferedReader(new InputStreamReader(input));
            String messageFromClient = messageReader.readLine();
            logger.debug("client says: " + messageFromClient);
            return messageFromClient;
        } catch (IOException e) {
            logger.error("error reading incoming message.", e);
        }
        return null;
    }

    public void writeMessage(Object messageObj) {
        Gson gson = new GsonBuilder().create();
        try {
            output.write((gson.toJson(messageObj) + MESSAGE_DELIMITER).getBytes());
        } catch (IOException e) {
            logger.error("error writing to stream", e);
        }
    }

    public void closeConnection() {
        try {
            output.close();
            input.close();
        } catch (IOException e) {
            logger.error("error closing connection.", e);
        }
    }

}
