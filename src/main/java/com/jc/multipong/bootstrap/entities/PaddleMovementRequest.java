package com.jc.multipong.bootstrap.entities;

/**
 * Created by jonataschagas on 25/01/17.
 */
public class PaddleMovementRequest {

    public String type = "PaddleMovementRequest";
    public SimpleGameObject paddle;
    public short playerNumber;
    public String gameId;
    public long tick;

}
