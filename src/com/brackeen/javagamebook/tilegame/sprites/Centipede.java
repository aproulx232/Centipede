package com.brackeen.javagamebook.tilegame.sprites;

import com.brackeen.javagamebook.graphics.Animation;

/**
    A Centipede is a Creature that moves slowly on the ground.
*/
public class Centipede extends Creature {

    public Centipede(Animation left, Animation right,
        Animation deadLeft, Animation deadRight)
    {
        super(left, right, deadLeft, deadRight, 2);

    }


    public float getMaxSpeed() {
        return 0.1f;
    }

    /**
     Called before update() if the creature collided with a
     tile horizontally.
     */
    public void collideHorizontal() {
        setY(getY()+20);
        setVelocityX(-getVelocityX());
    }


}
