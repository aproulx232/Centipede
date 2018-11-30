package com.brackeen.javagamebook.tilegame;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Random;

import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.sampled.AudioFormat;

import com.brackeen.javagamebook.graphics.*;
import com.brackeen.javagamebook.sound.*;
import com.brackeen.javagamebook.input.*;
import com.brackeen.javagamebook.test.GameCore;
import com.brackeen.javagamebook.tilegame.sprites.*;

/**
    GameManager manages all parts of the game.
*/
public class GameManager extends GameCore {

    public static void main(String[] args) {
        new GameManager().run();
    }

    // uncompressed, 44100Hz, 16-bit, mono, signed, little-endian
    private static final AudioFormat PLAYBACK_FORMAT =
        new AudioFormat(44100, 16, 1, true, false);

    private static final int DRUM_TRACK = 1;

    public static final float GRAVITY = 0.000f;

    // Constant used to control number of mushrooms spawned
    // Percent chance mushroom gets placed at valid location
    public static final float SPAWN_RATE = 60;

    private Point pointCache = new Point();
    public TileMap map;
    private MidiPlayer midiPlayer;
    private SoundManager soundManager;
    private ResourceManager resourceManager;
    private Sound prizeSound;
    private Sound boopSound;
    private SimpleSoundPlayer simpleSoundManager;
    private InputStream simpleStream;
    private InputManager inputManager;
    private TileMapRenderer renderer;

    private GameAction moveLeft;
    private GameAction moveRight;
    private GameAction moveUp;
    private GameAction moveDown;
    private GameAction shoot;
    private GameAction exit;

    private Random rand;


    public void init() {
        super.init();

        // set up input manager
        initInput();

        // start resource manager
        resourceManager = new ResourceManager(
        screen.getFullScreenWindow().getGraphicsConfiguration());

        // load resources
        renderer = new TileMapRenderer();
        renderer.setBackground(
            resourceManager.loadImage("background.png"));

        // load first map
        map = resourceManager.loadNextMap();

        // load sounds
        soundManager = new SoundManager(PLAYBACK_FORMAT);
        prizeSound = soundManager.getSound("sounds/prize.wav");
        boopSound = soundManager.getSound("sounds/boop2.wav");

        simpleSoundManager =  new SimpleSoundPlayer("sounds/boop2.wav");
        simpleStream = new ByteArrayInputStream(simpleSoundManager.getSamples());

        // start music

        midiPlayer = new MidiPlayer();
        Sequence sequence =
            midiPlayer.getSequence("sounds/music.midi");
        //midiPlayer.play(sequence, true);
        toggleDrumPlayback();

        //set random number for shroom spawning
        rand = new Random();

        //spawn centipede
        spawnNewCentipede();
    }


    /**
        Closes any resurces used by the GameManager.
    */
    public void stop() {
        super.stop();
        midiPlayer.close();
        soundManager.close();
    }


    private void initInput() {
        moveLeft = new GameAction("moveLeft");
        moveRight = new GameAction("moveRight");
        moveUp = new GameAction("moveUp");
        moveDown = new GameAction("moveDown");
        shoot = new GameAction("shoot",
            GameAction.DETECT_INITAL_PRESS_ONLY);
        exit = new GameAction("exit",
            GameAction.DETECT_INITAL_PRESS_ONLY);

        inputManager = new InputManager(
            screen.getFullScreenWindow());
        inputManager.setCursor(InputManager.INVISIBLE_CURSOR);

        inputManager.mapToKey(moveLeft, KeyEvent.VK_LEFT);
        inputManager.mapToKey(moveRight, KeyEvent.VK_RIGHT);
        inputManager.mapToKey(moveUp, KeyEvent.VK_UP);
        inputManager.mapToKey(moveDown, KeyEvent.VK_DOWN);
        inputManager.mapToKey(shoot, KeyEvent.VK_SPACE);
        inputManager.mapToKey(exit, KeyEvent.VK_ESCAPE);

        inputManager.setRelativeMouseMode(true);
        inputManager.mapToMouse(moveLeft, inputManager.MOUSE_MOVE_LEFT);
        inputManager.mapToMouse(moveRight, inputManager.MOUSE_MOVE_RIGHT);
        inputManager.mapToMouse(moveUp, inputManager.MOUSE_MOVE_UP);
        inputManager.mapToMouse(moveDown, inputManager.MOUSE_MOVE_DOWN);
        inputManager.mapToMouse(shoot, inputManager.MOUSE_BUTTON_1);
    }


    private void checkInput(long elapsedTime) {

        if (exit.isPressed()) {
            stop();
        }

        Player player = (Player)map.getPlayer();
        if (player.isAlive()) {
            float velocityX = 0;
            float velocityY = 0;
            if (moveLeft.isPressed()) {
                velocityX-=player.getMaxSpeed();
            }
            if (moveRight.isPressed()) {
                velocityX+=player.getMaxSpeed();
            }
            if (moveUp.isPressed()) {
                velocityY-=player.getMaxSpeed();
            }
            if (moveDown.isPressed()) {
                velocityY+=player.getMaxSpeed();
            }
            if (shoot.isPressed()) {
                player.shoot();
                soundManager.play(boopSound);
                shootLaser(renderer.pixelsToTiles(player.getX()),renderer.pixelsToTiles(player.getY()));
            }
            player.setVelocityX(velocityX);
            player.setVelocityY(velocityY);
        }

    }


    public void draw(Graphics2D g) {
        renderer.draw(g, map,
            screen.getWidth(), screen.getHeight());
    }


    /**
        Gets the current map.
    */
    public TileMap getMap() {
        return map;
    }


    /**
        Turns on/off drum playback in the midi music (track 1).
    */
    public void toggleDrumPlayback() {
        Sequencer sequencer = midiPlayer.getSequencer();
        if (sequencer != null) {
            sequencer.setTrackMute(DRUM_TRACK,
                !sequencer.getTrackMute(DRUM_TRACK));
        }
    }


    /**
        Gets the tile that a Sprites collides with. Only the
        Sprite's X or Y should be changed, not both. Returns null
        if no collision is detected.
    */
    public Point getTileCollision(Sprite sprite,
        float newX, float newY)
    {
        float fromX = Math.min(sprite.getX(), newX);
        float fromY = Math.min(sprite.getY(), newY);
        float toX = Math.max(sprite.getX(), newX);
        float toY = Math.max(sprite.getY(), newY);

        // get the tile locations
        int fromTileX = TileMapRenderer.pixelsToTiles(fromX);
        int fromTileY = TileMapRenderer.pixelsToTiles(fromY);
        int toTileX = TileMapRenderer.pixelsToTiles(
            toX + sprite.getWidth() - 1);
        int toTileY = TileMapRenderer.pixelsToTiles(
            toY + sprite.getHeight() - 1);

        // check each tile for a collision
        for (int x=fromTileX; x<=toTileX; x++) {
            for (int y=fromTileY; y<=toTileY; y++) {
                if (x < 0 || x >= map.getWidth() || map.getTile(x, y) != null)
                {
                    // collision found, return the tile
                    pointCache.setLocation(x, y);
                    return pointCache;
                }
                else if(y < 0 || y >= map.getHeight() || map.getTile(x, y) != null)
                {
                    // collision found, return the tile
                    pointCache.setLocation(x, y);
                    return pointCache;
                }
            }
        }

        // no collision found
        return null;
    }


    /**
        Checks if two Sprites collide with one another. Returns
        false if the two Sprites are the same. Returns false if
        one of the Sprites is a Creature that is not alive.
    */
    public boolean isCollision(Sprite s1, Sprite s2) {
        // if the Sprites are the same, return false
        if (s1 == s2) {
            return false;
        }
        if( s1 instanceof Centipede && s2 instanceof Centipede){
            //System.out.println("Cent collision");
            return false;
        }

        // if one of the Sprites is a dead Creature, return false
        if (s1 instanceof Creature && !((Creature)s1).isAlive()) {
            return false;
        }
        if (s2 instanceof Creature && !((Creature)s2).isAlive()) {
            return false;
        }

        // get the pixel location of the Sprites
        int s1x = Math.round(s1.getX());
        int s1y = Math.round(s1.getY());
        int s2x = Math.round(s2.getX());
        int s2y = Math.round(s2.getY());

        // check if the two sprites' boundaries intersect
        return (s1x < s2x + s2.getWidth() &&
            s2x < s1x + s1.getWidth() &&
            s1y < s2y + s2.getHeight() &&
            s2y < s1y + s1.getHeight());
    }


    /**
        Gets the Sprite that collides with the specified Sprite,
        or null if no Sprite collides with the specified Sprite.
    */
    public Sprite getSpriteCollision(Sprite sprite) {

        // run through the list of Sprites
        Iterator i = map.getSprites();
        while (i.hasNext()) {
            Sprite otherSprite = (Sprite)i.next();
            if (isCollision(sprite, otherSprite)) {
                // collision found, return the Sprite
                return otherSprite;
            }
        }

        // no collision found
        return null;
    }


    /**
        Updates Animation, position, and velocity of all Sprites
        in the current map.
    */
    public void update(long elapsedTime) {
        Creature player = (Creature)map.getPlayer();


        // player is dead! start map over
        if (player.getState() == Creature.STATE_DEAD) {
            map = resourceManager.reloadMap();
            resetMap();
            return;
        }

        // get keyboard/mouse input
        checkInput(elapsedTime);

        // update player
        updateCreature(player, elapsedTime);
        player.update(elapsedTime);

        // update other sprites
        Iterator i = map.getSprites();
        while (i.hasNext()) {
            Sprite sprite = (Sprite)i.next();
            if (sprite instanceof Creature) {
                Creature creature = (Creature)sprite;
                if (creature.getState() == Creature.STATE_DEAD) {
                    i.remove();
                    map.setScore(map.getScore()+creature.getPointValue());
                }
                else {
                    updateCreature(creature, elapsedTime);
                }
            }
            // normal update
            sprite.update(elapsedTime);
        }

        //check if all centipedes are dead
        checkLastCentipede();

        //check if spider is dead
        checkSpider();

    }


    /**
        Updates the creature, applying gravity for creatures that
        aren't flying, and checks collisions.
    */
    private void updateCreature(Creature creature, long elapsedTime)
    {
        // change x
        float dx = creature.getVelocityX();
        float oldX = creature.getX();
        float newX = oldX + dx * elapsedTime;
        Point tile =
            getTileCollision(creature, newX, creature.getY());
        if (tile == null) {
            creature.setX(newX);
        }
        else {
            // line up with the tile boundary
            if (dx > 0) {
                creature.setX(
                    TileMapRenderer.tilesToPixels(tile.x) -
                    creature.getWidth());
            }
            else if (dx < 0) {
                creature.setX(
                    TileMapRenderer.tilesToPixels(tile.x + 1));
            }
            creature.collideHorizontal();
        }
        if (creature instanceof Player) {
            checkPlayerCollision((Player)creature, false);
        }

        // change y
        float dy = creature.getVelocityY();
        float oldY = creature.getY();
        float newY = oldY + dy * elapsedTime;
        tile = getTileCollision(creature, creature.getX(), newY);
        if (tile == null) {
            creature.setY(newY);
        }
        else {
            // line up with the tile boundary
            if (dy > 0) {
                creature.setY(
                    TileMapRenderer.tilesToPixels(tile.y) -
                    creature.getHeight());
            }
            else if (dy < 0) {
                creature.setY(
                    TileMapRenderer.tilesToPixels(tile.y + 1));
            }
            creature.collideVertical();
        }
        if (creature instanceof Player) {
            boolean canKill = (oldY < creature.getY());
            checkPlayerCollision((Player)creature, false);
        }
        if (creature instanceof Centipede) {
            Sprite collide = getSpriteCollision(creature);
            if (collide instanceof Laser) {
                creature.setHealth(creature.getHealth()-1);
                map.setScore(map.getScore()+2);
                ((Laser) collide).setState(Creature.STATE_DEAD);
            }
            else if (collide instanceof Mushroom) {
                creature.collideHorizontal();
            }
        }
        if(creature instanceof Laser){
            Sprite collide = getSpriteCollision(creature);
            if (collide instanceof Mushroom) {
                ((Mushroom) collide).setHealth(((Mushroom) collide).getHealth()-1);
                map.setScore(map.getScore()+1);
                creature.setState(Creature.STATE_DEAD);
            }
            else if(collide instanceof Spider){
                ((Spider) collide).setHealth(((Spider) collide).getHealth() - 1);
                map.setScore(map.getScore()+100);
                creature.setState(Creature.STATE_DEAD);
            }
        }

    }


    /**
        Checks for Player collision with other Sprites. If
        canKill is true, collisions with Creatures will kill
        them.
    */
    public void checkPlayerCollision(Player player, boolean canKill)
    {
        if (!player.isAlive()) {
            return;
        }
        // check for player collision with other sprites
        Sprite collisionSprite = getSpriteCollision(player);
        if (collisionSprite instanceof Centipede || collisionSprite instanceof Spider) {
            // player dies!
            player.setHealth(player.getHealth()-1);
            player.setX(400);
            player.setY(400);

            //restore mushrooms and add points
            restoreMushrooms();

            killCentipede();
            spawnNewCentipede();

            killSpider();
            spawnNewSpider();
        }
    }

    private void restoreMushrooms(){
        Iterator i = map.getSprites();
        while (i.hasNext()) {
            Sprite sprite = (Sprite) i.next();
            if (sprite instanceof Mushroom) {
                if(((Mushroom) sprite).getHealth() < 3){
                    ((Mushroom) sprite).setHealth(3);
                    map.setScore(map.getScore()+10);
                }
            }
        }
    }

    private void resetMap(){
        //clear mushrooms
        Iterator i = map.getSprites();
        while (i.hasNext()) {
            Sprite sprite = (Sprite) i.next();
            if (sprite instanceof Mushroom) {
               i.remove();
            }
        }

        //spawn mushrooms
        spawnNewMushrooms();
        //spawn centipede
        spawnNewCentipede();

        //spawn player
        map.getPlayer().setX(400);
        map.getPlayer().setY(400);
        ((Creature)map.getPlayer()).setHealth(3);

        //reset score
        map.setScore(0);
    }

    public Sprite getSprite(int x, int y){
        if (x < 0 || x >= map.getWidth() || y < 0 || y >= map.getHeight()) {
            return null;
        }
        else {
            Iterator itr = map.getSprites();
            while (itr.hasNext()) {
                //  moving cursor to next element
                Sprite i = (Sprite) itr.next();
                if (renderer.pixelsToTiles(i.getX()) == x && renderer.pixelsToTiles(i.getY()) ==y ){
                    return i;
                }
            }
        }
        return null;
    }

    private void spawnNewMushrooms(){
        for (int y=1; y<map.getHeight()-8; y++) {
            for (int x = 1; x < map.getWidth() - 1; x++) {
                if(getSprite(x-1,y-1) == null && getSprite(x+1, y-1) == null) {
                    // Roll to place sprite or not
                    int randNum = rand.nextInt(100);
                    if(randNum < SPAWN_RATE) {
                        resourceManager.addSprite(map, resourceManager.getMushroomSprite(), x, y);
                    }
                }
            }
        }
    }

    /**
     * Checks if all the centipededs are gone from the map. If yes, spawns a new one
     */
    private boolean checkLastCentipede() {
        Iterator itr = map.getSprites();
        boolean centipedeAlive = false;
        while (itr.hasNext()) {
            //  moving cursor to next element
            Sprite i = (Sprite) itr.next();
            if (i instanceof Centipede) {
                if(((Centipede) i).getState() == Creature.STATE_NORMAL) {
                    centipedeAlive = true;
                }
            }
        }
        if (centipedeAlive == false) {
            map.setScore(map.getScore()+600);
            spawnNewCentipede();
        }
        return centipedeAlive;
    }

    private void killCentipede(){
        Iterator i = map.getSprites();
        while (i.hasNext()) {
            Sprite sprite = (Sprite) i.next();
            if (sprite instanceof Centipede) {
               i.remove();
            }
        }
    }

    private void spawnNewCentipede(){
        // Spawn new centipede
        for(int k = map.getWidth() - 5;k<map.getWidth();k++) {
            resourceManager.addSprite(map, resourceManager.getCentipedeSprite(), k, 0);
        }
    }

    private boolean checkSpider(){
        Iterator itr = map.getSprites();
        boolean spiderAlive = false;
        while (itr.hasNext()) {
            //  moving cursor to next element
            Sprite i = (Sprite) itr.next();
            if (i instanceof Spider) {
                if(((Spider) i).getState() == Creature.STATE_NORMAL) {
                    spiderAlive = true;
                }
            }
        }
        if (spiderAlive == false) {
            spawnNewSpider();
        }
        return spiderAlive;
    }

    private void killSpider(){
        Iterator i = map.getSprites();
        while (i.hasNext()) {
            Sprite sprite = (Sprite) i.next();
            if (sprite instanceof Spider) {
                i.remove();
            }
        }
    }
    private void spawnNewSpider(){
        //collisionSprite.setX(50);
        //collisionSprite.setY(400);
        //((Spider) collisionSprite).setHealth(2);
        resourceManager.addSprite(map, resourceManager.getSpiderSprite(), 0, 25);
    }

    /**
        Gives the player the speicifed power up and removes it
        from the map.
    */
    public void acquirePowerUp(PowerUp powerUp) {
        // remove it from the map
        map.removeSprite(powerUp);

        if (powerUp instanceof PowerUp.Star) {
            // do something here, like give the player points
            soundManager.play(prizeSound);
        }
        else if (powerUp instanceof PowerUp.Music) {
            // change the music
            soundManager.play(prizeSound);
            toggleDrumPlayback();
        }
        else if (powerUp instanceof PowerUp.Goal) {
            // advance to next map
            soundManager.play(prizeSound,
                new EchoFilter(2000, .7f), false);
            map = resourceManager.loadNextMap();
        }
    }

    public void shootLaser(int tileX, int tileY) {
        //simpleSoundManager.play(simpleStream);
        resourceManager.addSprite(map,resourceManager.getLaserSprite(),tileX,tileY-1);
    }

}
