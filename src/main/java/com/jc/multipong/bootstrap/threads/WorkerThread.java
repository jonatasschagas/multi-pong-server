package com.jc.multipong.bootstrap.threads;

import com.google.gson.JsonSyntaxException;
import com.jc.multipong.bootstrap.entities.GameConnectionRequest;
import com.jc.multipong.bootstrap.entities.GetGameLogicRequest;
import com.jc.multipong.bootstrap.entities.PaddleMovementRequest;
import com.jc.multipong.bootstrap.entities.StartGameRequest;
import com.jc.multipong.bootstrap.game.GameManager;
import com.jc.multipong.bootstrap.nio.ClientMessage;
import com.jc.multipong.bootstrap.nio.ServerThread;
import com.jc.multipong.bootstrap.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class is responsible for:
 * - queueing the socket activities received from the client
 * - going through the queue and executing the activities
 * - sending the response back to the client
 * *  *  *  *
 */
public class WorkerThread implements Runnable {

    static final Logger logger = LoggerFactory.getLogger(WorkerThread.class);
    private GameManager gameManager;
    private ServerThread serverThread;
    private BlockingQueue<ClientMessage> workingQueue;

    private static WorkerThread instance;

    public static WorkerThread getInstance() {
        if (instance == null) {
            synchronized (WorkerThread.class) {
                if (instance == null) {
                    instance = new WorkerThread();
                }
            }
        }
        return instance;
    }

    public WorkerThread() {
        this.gameManager = new GameManager();
        this.workingQueue = new LinkedBlockingQueue<>();
    }

    public void run() {
        logger.info("starting working thread...");
        while (true) {
            if (workingQueue.isEmpty()) {
                continue;
            }
            while (!workingQueue.isEmpty()) {
                executeTask(workingQueue.poll());
            }
        }
    }

    /**
     * Enqueues a message for the worker thread
     *
     * @param socket
     * @param data
     * @param count
     */
    public void submitTask(ServerThread serverThread, SocketChannel socket, byte[] data, int count) {
        if (this.serverThread == null) {
            this.serverThread = serverThread;
        }
        byte[] dataCopy = new byte[count];
        System.arraycopy(data, 0, dataCopy, 0, count);
        workingQueue.add(new ClientMessage(socket, new String(dataCopy)));
    }

    /**
     * Handles the socket activity from the client
     *
     * @param clientMessage
     */
    private void executeTask(ClientMessage clientMessage) {

        String rawMessage = clientMessage.getMessage();
        SocketChannel clientSocket = clientMessage.getSocket();

        if (rawMessage != null) {
            logger.debug("processing message(s): " + rawMessage);
            String[] messages = rawMessage.split("\\r?\\n");
            for (String message : messages) {
                try {
                    Object responseMessage = null;
                    if (message.contains("GameConnectionRequest")) {
                        GameConnectionRequest gameConnectionRequest = JsonUtils.fromJson(message, GameConnectionRequest.class);
                        responseMessage = gameManager.connect(clientSocket, gameConnectionRequest);
                    } else if (message.contains("StartGameRequest")) {
                        StartGameRequest startGameRequest = JsonUtils.fromJson(message, StartGameRequest.class);
                        responseMessage = gameManager.startGameSign(startGameRequest);
                    } else if (message.contains("PaddleMovementRequest")) {
                        PaddleMovementRequest paddleMovementRequest = JsonUtils.fromJson(message, PaddleMovementRequest.class);
                        gameManager.registerInput(paddleMovementRequest);
                    } else if (message.contains("GetGameLogicRequest")) {
                        GetGameLogicRequest getGameLogicRequest = JsonUtils.fromJson(message, GetGameLogicRequest.class);
                        responseMessage = gameManager.getGameState(
                                getGameLogicRequest.gameId,
                                getGameLogicRequest.clientTick
                        );
                    } else {
                        logger.warn("message: " + message + " not recognized.");
                    }

                    if (responseMessage != null) {
                        serverThread.send(clientSocket, JsonUtils.toJson(responseMessage));
                    }
                } catch (JsonSyntaxException mal) {
                    logger.error("Error reading json message: " + message, mal);
                }
            }
        }
    }

}
