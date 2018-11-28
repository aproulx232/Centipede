package com.brackeen.javagamebook.tilegame.sprites;

import com.brackeen.javagamebook.graphics.Animation;

/**
 A Laser is a object that moves up from the ship
 */
public class Mushroom extends Creature {

    public Mushroom(Animation left, Animation right,
                 Animation deadLeft, Animation deadRight)
    {
        super(left, right, deadLeft, deadRight,3, 5);
    }


    public float getMaxSpeed() {
        return 0.0f;
    }


    public boolean isFlying() {
        return isAlive();
    }

    public void wakeUp() {
        /*
        if (getState() == STATE_NORMAL && getVelocityY() == 0) {
            setVelocityY(-getMaxSpeed());
        }
        */
    }

    /**
     Updates the animaton for this creature.
     */
    public void update(long elapsedTime) {

        Animation newAnim = anim;
        // Full health
        if(getHealth() == 3){
            newAnim = getAnim1();
        }
        else if(getHealth() == 2){
            newAnim = getAnim2();
        }
        else if(getHealth() == 1){
            newAnim = getDeadLeft();
        }
        else {
            setState(STATE_DEAD);
        }
        // update the Animation
        if (anim != newAnim) {
            anim = newAnim;
            anim.start();
        }
        else {
            anim.update(elapsedTime);
        }
        /*
        // update to "dead" state
        stateTime += elapsedTime;
        if (state == STATE_DYING && stateTime >= DIE_TIME) {
            setState(STATE_DEAD);
        }
        */
        //super.update(elapsedTime);
    }

}
