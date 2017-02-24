package com.jc.multipong.bootstrap.game;

import com.jc.multipong.bootstrap.entities.GameLogic;
import com.jc.multipong.bootstrap.entities.PaddleMovementRequest;

import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Map;

/**
 * Class responsible for gathering the objects and the data related to a game session.
 */
public class Game {

    private String gameId;
    private GameLogic gameLogic;
    private Map<Short, SocketChannel> clientSockets;
    private Map<Long, List<PaddleMovementRequest>> inputs;

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public GameLogic getGameLogic() {
        return gameLogic;
    }

    public void setGameLogic(GameLogic gameLogic) {
        this.gameLogic = gameLogic;
    }

    public Map<Short, SocketChannel> getClientSockets() {
        return clientSockets;
    }

    public void setClientSockets(Map<Short, SocketChannel> clientSockets) {
        this.clientSockets = clientSockets;
    }

    public Map<Long, List<PaddleMovementRequest>> getInputs() {
        return inputs;
    }

    public void setInputs(Map<Long, List<PaddleMovementRequest>> inputs) {
        this.inputs = inputs;
    }
}
