package com.jc.multipong.bootstrap.entities;

import com.jc.multipong.bootstrap.utils.MathUtils;

public class GameLogic {

    public String type = "GameLogic";
    public SimpleGameObject ball;
    public SimpleGameObject paddle1;
    public SimpleGameObject paddle2;

    // bounds
    public int lowerBoundGridX = 0;
    public int higherBoundGridX = 10;
    public int lowerBoundGridY = 0;
    public int higherBoundGridY = 10;

    // fake int booleans
    public boolean hasStarted = false;
    public boolean gameOver = false;
    public boolean incrementX = true;
    public boolean incrementY = true;

    public boolean player1Connected = false;
    public boolean player2Connected = false;

    // winner == 1 -> paddle 1 won
    // winner == 2 -> paddle 2 won
    public int winner;

    public long currentTick = 0;
    public static int FPS = 60;

    public GameLogic() {
        ball = new SimpleGameObject ();
        paddle1 = new SimpleGameObject ();
        paddle2 = new SimpleGameObject ();
        incrementX = true;
        incrementY = true;
        hasStarted = false;
        paddle1.x = higherBoundGridX/2;
        paddle1.y = 0;
        ball.x = higherBoundGridX/2;
        ball.y = 0;
        paddle2.x = higherBoundGridX/2;
        paddle2.y = higherBoundGridY;
        currentTick = 0;
    }

    public void ManuallyMoveBall(int x, int y){
        if 	(gameOver) {
            return;
        }
        ball.x = x;
        ball.y = y;
    }

    public void MovePaddle1(int x, int y){
        if (gameOver) {
            return;
        }
        paddle1.x = x;
        paddle1.y = y;
        if (hasStarted == false) {
            ManuallyMoveBall (x, y);
        }
    }

    public void MovePaddle2(int x, int y){
        if (gameOver) {
            return;
        }
        paddle2.x = x;
        paddle2.y = y;
    }

    public SimpleGameObject GetPaddleByPlayerNumber(short playerNumber) {
        if (playerNumber == 1) {
            return paddle1;
        } else {
            return paddle2;
        }
    }

    public void SetPaddleByPlayerNumber(short playerNumber, SimpleGameObject paddle) {
        if (playerNumber == 1) {
            this.paddle1 = paddle;
        } else {
            this.paddle2 = paddle;
        }
    }

    public void StartGame() {
        hasStarted = true;
    }

    public void Update() {

        currentTick++;

        // if one second has passed
        if(currentTick % FPS == 0) {

            if (ball.x >= higherBoundGridX) {
                incrementX = false;
            } else if(ball.x <= lowerBoundGridX) {
                incrementX = true;
            }

            if (ball.y >= higherBoundGridY) {
                incrementY = false;
				/*if (HasCollided(paddle2.x, ball.x) == TRUE) {
				//currentAngle = MathUtils.GetRandom (10f, 60f);
				currentAngle = 45f;
			} else {
				// paddle didnt hit the ball
				gameOver = TRUE;
				winner = 1;
				return;
			}*/

            } else if(ball.y <= lowerBoundGridY) {
                incrementY = true;
                // paddle hit the ball
				/*if (HasCollided(paddle1.x, ball.x) == TRUE) {
				//currentAngle = MathUtils.GetRandom (10f, 60f);
				currentAngle = 45f;
			} else {
				// paddle didnt hit the ball
				gameOver = TRUE;
				winner = 2;
				return;
			}*/

            }

            int prevX = ball.x;
            if (incrementX) {
                ball.x += 1;
            } else {
                ball.x -= 1;
            }

            int deltaX = (int)MathUtils.Abs(ball.x,prevX);
            if (incrementY) {
                ball.y+= (int)MathUtils.Tan (MathUtils.DegreesToRad(45f)) * deltaX;
            } else {
                ball.y-= (int)MathUtils.Tan (MathUtils.DegreesToRad(45f)) * deltaX;
            }
        }

    }

    boolean HasCollided(float xPaddle, float xBall) {
        return xBall >= xPaddle && xBall <= xPaddle + 1 || xBall + 1 >= xPaddle && xBall + 1 <= xPaddle + 1 ? true : false;
    }

}
