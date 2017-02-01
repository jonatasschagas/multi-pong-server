package com.jc.multipong.bootstrap.game;

import com.jc.multipong.bootstrap.entities.*;
import com.jc.multipong.bootstrap.utils.SocketWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by jonataschagas on 25/01/17.
 */
public class GameManager {

    static final Logger logger = LoggerFactory.getLogger(GameManager.class);
    private Map<String, Game> gameSessions = new HashMap<String, Game>();
    private Queue<String> openedMatches = new LinkedBlockingQueue<String>();


    public GameConnectionResponse connect(SocketWrapper clientSocket, GameConnectionRequest gameConnectionRequest) {
        String gameId = openedMatches.poll();
        boolean newMatch = false;
        if (gameId == null) {
            gameId = UUID.randomUUID().toString();
            newMatch = true;
        }
        Game game = gameSessions.get(gameId);
        if (game == null) {
            // creates new gameSession
            game = new Game();
            Map<Short, SocketWrapper> socketMap = new HashMap<Short, SocketWrapper>();
            game.setClientSockets(socketMap);
            game.setInputs(new HashMap<>());
            game.setGameLogic(new GameLogic());
        }
        GameConnectionResponse connectResponse = new GameConnectionResponse();
        connectResponse.gameId = gameId;
        short paddleNumber = -1;
        if (newMatch) {
            paddleNumber = (short) 1;
        } else {
            paddleNumber = (short) 2;
        }
        game.getClientSockets().put(paddleNumber, clientSocket);
        connectResponse.playerNumber = paddleNumber;
        gameSessions.put(gameId, game);
        logger.info("connect: game: " + gameId);
        if (newMatch) {
            openedMatches.add(gameId);
        } else {
            game.getGameLogic().player1Connected = 1;
            game.getGameLogic().player2Connected = 1;
        }
        return connectResponse;
    }

    public void registerInput(final PaddleMovementRequest paddleMovement) {
        // send the  paddle movement to the other player
        Game game = gameSessions.get(paddleMovement.gameId);
        if (game != null) {
            // queuing the inputs
            List<PaddleMovementRequest> tickInputsList = game.getInputs().get(paddleMovement.tick);
            if (tickInputsList == null) {
                tickInputsList = new ArrayList<>();
            }
            tickInputsList.add(paddleMovement);
            game.getInputs().put(paddleMovement.tick, tickInputsList);
            // pass the paddle movement to the other player
            // broadcastMessageToOtherPlayers(paddleMovement.playerNumber, paddleMovement, game);
        }
    }

    public GameLogic getGameState(String gameId, long currentClientTick) {
        Game game = gameSessions.get(gameId);
        if (game == null) {
            throw new RuntimeException("cannot find game with gameId: " + gameId);
        }
        GameLogic serverState = game.getGameLogic();
        long serverStateCurrentTick = serverState.currentTick;

        serverState.paddle1Replay = getReplay(game, (short)1);
        serverState.paddle2Replay = getReplay(game, (short)2);

        if (currentClientTick < serverStateCurrentTick) {
            // forwards the client to the current position
            return serverState;
        } else {
            // fast forward to the client's tick
            for (long tick = serverStateCurrentTick; tick <= currentClientTick; tick++) {
                // process inputs for this tick if any
                List<PaddleMovementRequest> inputsFromTick = game.getInputs().get(tick);
                if (inputsFromTick != null) {
                    for (PaddleMovementRequest paddleMovementRequest : inputsFromTick) {
                        if (paddleMovementRequest.playerNumber == 1) {
                            serverState.MovePaddle1(paddleMovementRequest.paddle.x, paddleMovementRequest.paddle.y);
                        } else {
                            serverState.MovePaddle2(paddleMovementRequest.paddle.x, paddleMovementRequest.paddle.y);
                        }
                    }
                }
                // advance the game
                serverState.MoveBall();
            }
            return serverState;
        }
    }

    private PaddleMovementRequest[] getReplay(Game game, short paddleNumber) {
        Map<Long, List<PaddleMovementRequest>> inputs = game.getInputs();
        long serverStateCurrentTick = game.getGameLogic().currentTick;
        List<PaddleMovementRequest> opponentReplay = new ArrayList<>();
        for(long tick = serverStateCurrentTick - 5; tick < serverStateCurrentTick; tick++) {
            List<PaddleMovementRequest> inputsPerTicks = inputs.get(tick);
            if(inputsPerTicks != null) {
                for(PaddleMovementRequest paddleMovementRequest : inputsPerTicks) {
                    if(paddleMovementRequest.playerNumber == paddleNumber) {
                        opponentReplay.add(paddleMovementRequest);
                    }
                }
            }
        }
        PaddleMovementRequest[] opponentReplayArr = new PaddleMovementRequest[opponentReplay.size()];
        return opponentReplay.toArray(opponentReplayArr);
    }

    public StartGameResponse startGameSign(StartGameRequest startGameRequest) {
        Game game = gameSessions.get(startGameRequest.gameId);
        game.getGameLogic().StartGame();
        if (game == null) {
            throw new RuntimeException("cannot find game with gameId: " + startGameRequest.gameId);
        }
        StartGameResponse startGameResponse = new StartGameResponse();
        // send the start message to the other players
        broadcastMessageToOtherPlayers(startGameRequest.playerNumber, startGameResponse, game);

        return startGameResponse;
    }

    private void broadcastMessageToOtherPlayers(short senderPlayerNumber, Object message, Game game) {
        game.getClientSockets().keySet().stream().forEach(playerNumber -> {
            if (playerNumber != senderPlayerNumber) {
                game.getClientSockets().get(playerNumber).writeMessage(message);
            }
        });
    }

}
