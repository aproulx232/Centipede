package com.brackeen.javagamebook.tilegame.sprites;

import java.lang.reflect.Constructor;
import com.brackeen.javagamebook.graphics.*;

/**
    A Creature is a Sprite that is affected by gravity and can
    die. It has four Animations: moving anim1, moving anim2,
    dying on the anim1, and dying on the anim2.
*/
public abstract class Creature extends Sprite {

    /**
        Amount of time to go from STATE_DYING to STATE_DEAD.
    */
    private static final int DIE_TIME = 1000;

    public static final int STATE_NORMAL = 0;
    public static final int STATE_DYING = 1;
    public static final int STATE_DEAD = 2;

    private Animation anim1;
    private Animation anim2;
    private Animation deadLeft;
    private Animation deadRight;
    private int state;
    private long stateTime;
    private int health;

    /**
        Creates a new Creature with the specified Animations.
    */
    public Creature(Animation anim1, Animation anim2,
                    Animation deadLeft, Animation deadRight, int health)
    {
        super(anim2);
        this.anim1 = anim1;
        this.anim2 = anim2;
        this.deadLeft = deadLeft;
        this.deadRight = deadRight;
        this.health = health;
        state = STATE_NORMAL;
    }


    public Object clone() {
        // use reflection to create the correct subclass
        Constructor constructor = getClass().getConstructors()[0];
        try {
            return constructor.newInstance(new Object[] {
                (Animation) anim1.clone(),
                (Animation) anim2.clone(),
                (Animation)deadLeft.clone(),
                (Animation)deadRight.clone()
            });
        }
        catch (Exception ex) {
            // should never happen
            ex.printStackTrace();
            return null;
        }
    }


    /**
        Gets the maximum speed of this Creature.
    */
    public float getMaxSpeed() {
        return 0;
    }


    /**
        Wakes up the creature when the Creature first appears
        on screen. Normally, the creature starts moving anim1.
    */
    public void wakeUp() {
        if (getState() == STATE_NORMAL && getVelocityX() == 0) {
            setVelocityX(-getMaxSpeed());
        }
    }


    /**
        Gets the state of this Creature. The state is either
        STATE_NORMAL, STATE_DYING, or STATE_DEAD.
    */
    public int getState() {
        return state;
    }


    /**
        Sets the state of this Creature to STATE_NORMAL,
        STATE_DYING, or STATE_DEAD.
    */
    public void setState(int state) {
        if (this.state != state) {
            this.state = state;
            stateTime = 0;
            if (state == STATE_DYING) {
                setVelocityX(0);
                setVelocityY(0);
            }
        }
    }


    /**
        Checks if this creature is alive.
    */
    public boolean isAlive() {
        return (state == STATE_NORMAL);
    }


    /**
        Checks if this creature is flying.
    */
    public boolean isFlying() {
        return false;
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
    public void collideVertical() {
        setVelocityY(0);
    }


    /**
        Updates the animaton for this creature.
    */
    public void update(long elapsedTime) {
        // select the correct Animation
        Animation newAnim = anim;
        if (getVelocityX() < 0) {
            newAnim = anim1;
        }
        else if (getVelocityX() > 0) {
            newAnim = anim2;
        }
        if (state == STATE_DYING && newAnim == anim1) {
            newAnim = deadLeft;
        }
        else if (state == STATE_DYING && newAnim == anim2) {
            newAnim = deadRight;
        }

        // update the Animation
        if (anim != newAnim) {
            anim = newAnim;
            anim.start();
        }
        else {
            anim.update(elapsedTime);
        }

        // update to "dead" state
        stateTime += elapsedTime;
        if (state == STATE_DYING && stateTime >= DIE_TIME) {
            setState(STATE_DEAD);
        }
    }

    public int getHealth(){
        return this.health;
    }
    public void setHealth(int health){
        this.health = health;
        if(this.health == 0){
            setState(STATE_DYING);
        }
    }

    public Animation getAnim1(){
        return anim1;
    }
    public Animation getAnim2(){
        return anim2;
    }
    public  Animation getDeadLeft(){
        return deadLeft;
    }
    public Animation getDeadRight(){
        return deadRight;
    }

}
