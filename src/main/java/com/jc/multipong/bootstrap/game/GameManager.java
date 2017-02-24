package com.jc.multipong.bootstrap.game;

import com.jc.multipong.bootstrap.entities.*;
import com.jc.multipong.bootstrap.nio.ServerThread;
import com.jc.multipong.bootstrap.threads.WorkerThread;
import com.jc.multipong.bootstrap.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Class responsible for handling the game commands and managing the game sessions.
 */
public class GameManager {

    static final Logger logger = LoggerFactory.getLogger(GameManager.class);
    private Map<String, Game> gameSessions = new HashMap<String, Game>();
    private Queue<String> openedMatches = new LinkedBlockingQueue<String>();

    /**
     * Connects a client to a game match if one already exists. Otherwise it creates a new match
     * and adds it to the "openedMatches" queue.
     *
     * @param clientSocket
     * @param gameConnectionRequest
     * @return
     */
    public GameConnectionResponse connect(SocketChannel clientSocket, GameConnectionRequest gameConnectionRequest) {
        String gameId = openedMatches.poll();
        Game game;
        if (gameId == null) {
            game = createGameMatch();
        } else {
            game = gameSessions.get(gameId);
        }

        gameId = game.getGameId();
        GameConnectionResponse connectResponse = new GameConnectionResponse();
        connectResponse.gameId = gameId;

        short paddleNumber;
        if (!game.getGameLogic().player1Connected) {
            paddleNumber = (short) 1;
            game.getGameLogic().player1Connected = true;
        } else {
            paddleNumber = (short) 2;
            game.getGameLogic().player2Connected = true;
        }
        game.getClientSockets().put(paddleNumber, clientSocket);
        connectResponse.playerNumber = paddleNumber;
        logger.info("connect: game: " + gameId + ", paddle number:" + paddleNumber);

        return connectResponse;
    }

    /**
     * Insures that a match will be created **only** if there isn't a match created already
     *
     * @return
     */
    private synchronized Game createGameMatch() {
        // makes lookup again in case another match has been created already
        String gameId = openedMatches.poll();
        Game game = null;
        if (gameId != null) {
            return gameSessions.get(gameId);
        }

        gameId = UUID.randomUUID().toString();
        game = new Game();
        Map<Short, SocketChannel> socketMap = new HashMap<Short, SocketChannel>();
        game.setClientSockets(socketMap);
        game.setInputs(new HashMap<>());
        game.setGameLogic(new GameLogic());
        game.setGameId(gameId);
        gameSessions.put(gameId, game);
        openedMatches.add(gameId);
        return game;
    }

    /**
     * Stores all the inputs from a given tick into a list and "moves" the paddle in the game
     *
     * @param input
     */
    public void registerInput(final PaddleMovementRequest input) {
        Game game = gameSessions.get(input.gameId);
        if (game == null) {
            logger.warn("Unable to find game object for id: " + input.gameId);
            return;
        }
        GameLogic gameLogic = game.getGameLogic();
        Map<Long, List<PaddleMovementRequest>> inputs = game.getInputs();
        SimpleGameObject paddle = input.paddle;

        List<PaddleMovementRequest> inputsFromThisTick = inputs.get(input.tick);
        if (inputsFromThisTick == null) {
            inputsFromThisTick = new ArrayList<>();
        }
        inputsFromThisTick.add(input);
        if (input.playerNumber == 1) {
            gameLogic.MovePaddle1(paddle.x, paddle.y);
        } else {
            gameLogic.MovePaddle2(paddle.x, paddle.y);
        }
    }

    /**
     * Advances the state of the game in the server by fast forwarding to the current tick of the game client. After
     * the state of the game in the server is in sync with the client tick, the state of the server is returned
     * to the client for synchronization. In case the game client is behind the server's current tick,
     * the current state of the server is returned.*
     *
     * @param gameId
     * @param currentClientTick
     * @return
     */
    public GameLogic getGameState(String gameId, long currentClientTick) {
        Game game = gameSessions.get(gameId);
        if (game == null) {
            logger.warn("cannot find game with gameId: " + gameId);
            return null;
        }
        GameLogic gameLogic = game.getGameLogic();
        long currentServerTick = gameLogic.currentTick;
        if (currentClientTick < currentServerTick) {
            // forwards the client to the current state of the game
            return gameLogic;
        } else {
            // gets the current tick again, in case another thread has advanced the game
            currentServerTick = gameLogic.currentTick;
            Map<Long, List<PaddleMovementRequest>> inputs = game.getInputs();
            // fast forward to the client's tick
            for (long tick = currentServerTick; tick <= currentClientTick; tick++) {
                // process inputs for this tick if any
                List<PaddleMovementRequest> inputsFromTick = inputs.get(tick);
                if (inputsFromTick != null) {
                    for (PaddleMovementRequest input : inputsFromTick) {
                        SimpleGameObject paddle = input.paddle;
                        if (input.playerNumber == 1) {
                            gameLogic.MovePaddle1(paddle.x, paddle.y);
                        } else {
                            gameLogic.MovePaddle2(paddle.x, paddle.y);
                        }
                    }
                }
                // advance the game
                gameLogic.Update();
            }
            return gameLogic;
        }
    }

    /**
     * Starts the game in the server and broadcasts to the clients a message that tells them to start the game.
     *
     * @param startGameRequest
     * @return
     */
    public StartGameResponse startGameSign(StartGameRequest startGameRequest) {
        Game game = gameSessions.get(startGameRequest.gameId);
        if (game == null) {
            logger.warn("cannot find game with gameId: " + startGameRequest.gameId);
            return null;
        }
        GameLogic gameLogic = game.getGameLogic();
        if (!gameLogic.hasStarted) {
            gameLogic.StartGame();
        }
        StartGameResponse startGameResponse = new StartGameResponse();
        // send the start message to the other players
        broadcastMessageToOtherPlayers(startGameRequest.playerNumber, startGameResponse, game);

        return startGameResponse;
    }

    private void broadcastMessageToOtherPlayers(short senderPlayerNumber, Object message, Game game) {
        game.getClientSockets().keySet().stream().forEach(playerNumber -> {
            if (playerNumber != senderPlayerNumber) {
                ServerThread.getInstance().send(
                        game.getClientSockets().get(playerNumber),
                        JsonUtils.toJson(message)
                );
            }
        });
    }

}
