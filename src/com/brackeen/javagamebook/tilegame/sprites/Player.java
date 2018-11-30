package com.brackeen.javagamebook.tilegame.sprites;

import com.brackeen.javagamebook.graphics.Animation;
import com.brackeen.javagamebook.tilegame.GameManager;
import com.brackeen.javagamebook.tilegame.ResourceManager;

/**
    The Player.
*/
public class Player extends Creature {

    private static final float JUMP_SPEED = -.95f;

    private boolean onGround;

    public Player(Animation left, Animation right,
        Animation deadLeft, Animation deadRight)
    {
        super(left, right, deadLeft, deadRight,3,0);
    }


    public void collideHorizontal() {
        setVelocityX(0);
    }


    public void collideVertical() {

        setVelocityY(0);
    }

    public void wakeUp() {
        // do nothing
    }


    /**
        Makes the player shoot a laser
    */
    public void shoot() {

    }


    public float getMaxSpeed() {
        return 2f;
    }

}
