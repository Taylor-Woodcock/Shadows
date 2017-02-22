package edu.lancs.game.entity;

import edu.lancs.game.Debug;
import edu.lancs.game.InputHandler;
import edu.lancs.game.Window;
import org.jsfml.graphics.FloatRect;
import org.jsfml.graphics.Sprite;
import org.jsfml.graphics.Texture;
import org.jsfml.system.Vector2f;

import java.util.ArrayList;

import static edu.lancs.game.Constants.*;
import static edu.lancs.game.entity.Actor.State.*;

public abstract class Actor extends Entity {

    // actual player variables
    private int health;
    private int hearts; // different to health as this is the amount they can have (health / hearts)

    private Vector2f velocity;

    private int testHealth = -1; // TODO: Remove once finished with HUD testing
    private boolean isColliding = false; //TODO: REMOVE THIS ONCE FINISHED TESTING/FIXING COLLISION

    // the entity variables
    private InputHandler inputHandler;

    private ArrayList<Texture> idleAnimation;
    private ArrayList<Texture> runAnimation;
    private ArrayList<Texture> attackAnimation;
    private ArrayList<Texture> deathAnimation;

    private State state;

    public Actor(Window window, String actorName, float xPos, float yPos, boolean isAnimated, int maxHealth, int currentHealth) {
        super(window, actorName + "_idle", xPos, yPos, isAnimated);
        // initialise player stats (health, score, etc)
        health = currentHealth;
        hearts = maxHealth;
        velocity = new Vector2f(0, 0);

        // load animations
        idleAnimation = getWindow().getResourceManager().getAnimations(actorName + "_idle");
        runAnimation = getWindow().getResourceManager().getAnimations(actorName + "_run");
        attackAnimation = getWindow().getResourceManager().getAnimations(actorName + "_melee");
        deathAnimation = getWindow().getResourceManager().getAnimations(actorName + "_dead");

        // set state to IDLE as the actor has just been created
        setState(IDLE);
    }

    /***
     * Special actions should be performed in here.
     */
    @Override
    public abstract void performAction();

    /***
     * This updates the players variables every iteration.
     */
    @Override
    public abstract void update();

    /***
     * Checks if directional keys are bing pressed and moves the character in that direction is so.
     * Support for diagonal movement is present as each if condition is on its own line.
     */
    public abstract void handleMovement();

    /***
     * Moves the player left by PLAYER_BASE_MOVEMENT
     */
    public void moveLeft() {
        velocity = new Vector2f(-PLAYER_BASE_MOVEMENT, 0);
        if(!isColliding)
            move(velocity);
        setScale(-1.f, 1.f); // flip the sprite to face right
        if (state != RUNNING)
            setState(RUNNING);
    }

    /***
     * Moves the player right by PLAYER_BASE_MOVEMENT
     */
    public void moveRight() {
        velocity = new Vector2f(PLAYER_BASE_MOVEMENT, 0);
        if(!isColliding)
            move(velocity);
        setScale(1.f, 1.f); // flip the sprite to face left
        if (state != RUNNING)
            setState(RUNNING);
    }

    /***
     * Moves the player up by PLAYER_BASE_MOVEMENT
     */
    public void moveUp() {
        velocity = new Vector2f(0, -PLAYER_BASE_MOVEMENT);
        if(!isColliding)
            move(velocity);
        if (state != RUNNING)
            setState(RUNNING);
    }

    /***
     * Moves the player down by PLAYER_BASE_MOVEMENT
     */
    public void moveDown() {
        velocity = new Vector2f(0, PLAYER_BASE_MOVEMENT);
        if(!isColliding)
            move(velocity);
        if (state != RUNNING)
            setState(RUNNING);
    }

    /***
     * Returns the current State of the Player.
     *
     * @return - Current State of the Player
     */
    public State getState() {
        return state;
    }

    /***
     * Sets the current state of the Player and changes the animation accordingly.
     *
     * @param state - Current state of the Player
     */
    public void setState(State state) {
        this.state = state;
        switch (state) {
            case IDLE:
                setAnimation(idleAnimation);
                break;

            case RUNNING:
                setAnimation(runAnimation);
                break;

            case ATTACKING:
                setAnimation(attackAnimation);
                break;

            case DYING:
                setAnimation(deathAnimation);
                break;
        }
        Debug.print("Player state: " + state);
    }

    /***
     * Sets the velocity of the Actor for it to move.
     *
     * @param velocity - Velocity of where to move the Actor
     */
    public void setVelocity(Vector2f velocity) {
        this.velocity = velocity;
    }

    /***
     * Returns the current velocity of the character.
     *
     * @return - Current velocity of the character
     */
    public Vector2f getVelocity() {
        return velocity;
    }

    /***
     * Checks whether or not the Player is colliding with a Sprite.
     *
     * @param sprite - Sprite the player is checking against
     * @return - Whether or not it is colliding
     */
    public boolean checkCollision(Sprite sprite) {
        return (getGlobalBounds().intersection(new FloatRect(sprite.getPosition().x, sprite.getPosition().x, 1, 1)) != null);
    }

    /***
     * Performs an attack and sets the Players state to ATTACKING.
     * TODO: Add hit detection in here. Should check the current frame syncs up with the hit blow.
     */
    public void attack() {
        setState(ATTACKING);
    }

    /***
     * Returns the players health.
     *
     * @return - Players health
     */
    public int getHealth() {
        return health;
    }

    /***
     * Sets the Actors current health
     *
     * @param health - Current health
     */
    public void setHealth(int health) {
        if(health <= hearts && health >= 0)
            this.health = health;
        else
            this.health = 0; //FIXME: Here I just basically say, if you set the health wrong, kill the character (i.e. setting it to -1 will go to 0)
    }

    /***
     * Returns the players hearts.
     *
     * @return - Players hearts
     */
    public int getHearts() {
        return hearts;
    }

    /***
     * States for the players animation/actions
     */
    public enum State {
        IDLE, RUNNING, ATTACKING, DYING
    }

    /***
     * Sets whether or not the Player is colliding currently.
     *
     * @param colliding - Whether or not the Player is currently colliding
     */
    public void setColliding(boolean colliding) {
        isColliding = colliding;
    }

    /***
     * Returns whether or not the Player is colliding currently.
     *
     * @return - Whether or not the Player is currently colliding
     */
    public boolean isColliding() {
        return isColliding;
    }
}
