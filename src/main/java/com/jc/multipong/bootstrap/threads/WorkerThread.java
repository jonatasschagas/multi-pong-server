package com.jc.multipong.bootstrap.threads;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jc.multipong.bootstrap.entities.GameConnectionRequest;
import com.jc.multipong.bootstrap.entities.GetGameLogicRequest;
import com.jc.multipong.bootstrap.entities.PaddleMovementRequest;
import com.jc.multipong.bootstrap.entities.StartGameRequest;
import com.jc.multipong.bootstrap.game.GameManager;
import com.jc.multipong.bootstrap.utils.SocketWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;

/**
 * Created by jonataschagas on 24/01/17.
 */
public class WorkerThread implements Runnable {

    static final Logger logger = LoggerFactory.getLogger(WorkerThread.class);
    protected SocketWrapper clientSocket = null;
    private GameManager gameManager;

    public WorkerThread(Socket clientSocket, GameManager gameManager) {
        this.clientSocket = new SocketWrapper(clientSocket);
        this.gameManager = gameManager;
    }

    public void run() {
        int nullMessages = 0;
        while (clientSocket.isConnected()) {
            String message = clientSocket.readMessage();
            if(message != null) {
                nullMessages = 0;
                processMessage(message);
            } else {
                nullMessages++;
            }

            if(nullMessages > 10) {
                logger.info("Closing connection due to inactivity");
                break;
            }

            try {
                Thread.sleep((long)Math.pow(2, nullMessages));
            } catch (InterruptedException e) {
                logger.error("sleep interrupted",e);
            }
        }
        clientSocket.closeConnection();
    }

    private void processMessage(String message) {
        Gson gson = new GsonBuilder().create();

        if (message.contains("GameConnectionRequest")) {
            GameConnectionRequest gameConnectionRequest = gson.fromJson(message, GameConnectionRequest.class);
            clientSocket.writeMessage(
                    gameManager.connect(clientSocket, gameConnectionRequest)
            );
        } else if(message.contains("StartGameRequest")) {
            StartGameRequest startGameRequest = gson.fromJson(message, StartGameRequest.class);
            clientSocket.writeMessage(
                    gameManager.startGameSign(startGameRequest)
            );
        } else if(message.contains("PaddleMovementRequest")) {
            PaddleMovementRequest paddleMovementRequest = gson.fromJson(message, PaddleMovementRequest.class);
            gameManager.registerInput(paddleMovementRequest);
        } else if(message.contains("GetGameLogicRequest")) {
            GetGameLogicRequest getGameLogicRequest = gson.fromJson(message, GetGameLogicRequest.class);
            clientSocket.writeMessage(
                    gameManager.getGameState(getGameLogicRequest.gameId, getGameLogicRequest.clientTick)
            );
        } else {
            throw new RuntimeException("Message: " + message + " not recognized.");
        }
    }

}
