package com.jc.multipong.bootstrap;

import com.jc.multipong.bootstrap.game.GameManager;
import com.jc.multipong.bootstrap.threads.WorkerThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by jonataschagas on 23/01/17.
 */
public class GameServer implements Runnable {

    public static final int SERVER_PORT = 9090;
    static final Logger logger = LoggerFactory.getLogger(GameServer.class);
    private ServerSocket serverSocket = null;
    private boolean isStopped = false;
    private Thread runningThread = null;

    public static void main(String args[]) {
        logger.info("Starting server. PORT: " + SERVER_PORT);
        GameServer server = new GameServer();
        Thread serverThread = new Thread(server);
        serverThread.start();
        while (serverThread.isAlive()) {
        }
        logger.info("Stopping Server");
        server.stop();
    }

    public void run() {
        synchronized (this) {
            this.runningThread = Thread.currentThread();
        }
        GameManager gameManager = new GameManager();
        openServerSocket();
        while (!isStopped()) {
            Socket clientSocket = null;
            try {
                clientSocket = this.serverSocket.accept();
            } catch (IOException e) {
                if (isStopped()) {
                    logger.info("Server Stopped.");
                    return;
                }
                throw new RuntimeException(
                        "Error accepting client connection", e);
            }
            new Thread(new WorkerThread(clientSocket, gameManager))
                    .start();
        }
        logger.info("Server Stopped.");
    }

    private synchronized boolean isStopped() {
        return this.isStopped;
    }

    public synchronized void stop() {
        this.isStopped = true;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    private void openServerSocket() {
        try {
            this.serverSocket = new ServerSocket(SERVER_PORT);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open port " + SERVER_PORT, e);
        }
    }

}
