package com.brackeen.javagamebook.tilegame.sprites;

import com.brackeen.javagamebook.graphics.Animation;

/**
    A Laser is a object that moves up from the ship
*/
public class Laser extends Creature {

    public Laser(Animation left, Animation right,
        Animation deadLeft, Animation deadRight)
    {
        super(left, right, deadLeft, deadRight,-1, 0);
    }


    public float getMaxSpeed() {
        return 0.5f;
    }


    public boolean isFlying() {
        return isAlive();
    }

    public void wakeUp() {
        if (getState() == STATE_NORMAL && getVelocityY() == 0) {
            setVelocityY(-getMaxSpeed());
        }
    }

}
