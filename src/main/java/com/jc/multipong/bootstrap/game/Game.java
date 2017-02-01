package com.jc.multipong.bootstrap.game;

import com.jc.multipong.bootstrap.entities.GameLogic;
import com.jc.multipong.bootstrap.entities.PaddleMovementRequest;
import com.jc.multipong.bootstrap.utils.SocketWrapper;

import java.util.List;
import java.util.Map;

/**
 * Created by jonataschagas on 25/01/17.
 */
public class Game {

    private GameLogic gameLogic;
    private Map<Short, SocketWrapper> clientSockets;
    private Map<Long, List<PaddleMovementRequest>> inputs;

    public GameLogic getGameLogic() {
        return gameLogic;
    }

    public void setGameLogic(GameLogic gameLogic) {
        this.gameLogic = gameLogic;
    }

    public Map<Short, SocketWrapper> getClientSockets() {
        return clientSockets;
    }

    public void setClientSockets(Map<Short, SocketWrapper> clientSockets) {
        this.clientSockets = clientSockets;
    }

    public Map<Long, List<PaddleMovementRequest>> getInputs() {
        return inputs;
    }

    public void setInputs(Map<Long, List<PaddleMovementRequest>> inputs) {
        this.inputs = inputs;
    }
}
