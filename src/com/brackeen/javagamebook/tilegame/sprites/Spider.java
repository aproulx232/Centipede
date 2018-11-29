package com.brackeen.javagamebook.tilegame.sprites;

import com.brackeen.javagamebook.graphics.Animation;

/**
    A Spider is a Creature that moves in a diagnal.
*/
public class Spider extends Creature {

    public Spider(Animation left, Animation right,
                  Animation deadLeft, Animation deadRight)
    {
        super(left, right, deadLeft, deadRight, 2, 600);

    }


    public float getMaxSpeed() {
        return 0.2f;
    }

    /**
     Wakes up the creature when the Creature first appears
     on screen. Normally, the creature starts moving anim1.
     */
    public void wakeUp() {
        if (getState() == STATE_NORMAL && getVelocityX() == 0) {
            setVelocityX(-getMaxSpeed());
            setVelocityY(getMaxSpeed());
        }
    }

    /**
     Called before update() if the creature collided with a
     tile horizontally.
     */
    public void collideHorizontal() {
        setVelocityX(-getVelocityX());
    }


    /**
     Called before update() if the creature collided with a
     tile vertically.
     */
    public void collideVertical()
    {
        setVelocityY(-getVelocityY());
    }

}
