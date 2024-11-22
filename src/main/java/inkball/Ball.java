package inkball;
import java.util.Random;

import processing.core.PVector;

/**
 * The {@code Ball} class represents a ball in the Inkball game, encapsulating its 
 * properties and behaviors such as position, velocity, and interactions with other 
 * game elements.
 * 
 * <p>Each ball has a name, color, position, velocity, and radius, and it can be 
 * moved, absorbed by circles, and interact with the game world. The ball's 
 * direction is represented as a quadrant based on its velocity.</p>
 */
public class Ball extends App {
    /** The name of the ball, which includes its color information. */
    private String name;

    /** Indicates if the ball is currently on the game board. */
    private boolean onBoard;

    /** The position of the ball in pixels. */
    private PVector position; //in pixels

    /** The initial position of the ball for resetting purposes. */
    private PVector initialPosition;

    /** The velocity vector of the ball. */
    private PVector velocity;

    /** The current color of the ball, represented as an integer. */
    private int color;

    /** The initial color of the ball for resetting purposes. */
    private int initialColor;

    /** The initial radius of the ball. */
    final private float INITIAL_R = 12.0f;

    /** The current radius of the ball. */
    private float radius;

    /** Random number generator for initializing the ball's velocity. */
    private Random random = new Random();

    /** The direction of the ball, represented in quadrants. */
    private int direction = 0; // represented in quadrant

    /**
     * Constructs a {@code Ball} with the specified name and initial position.
     * 
     * @param name the name of the ball, including its color information.
     * @param x the initial x-coordinate of the ball.
     * @param y the initial y-coordinate of the ball.
     */
    public Ball (String name, float x, float y) {
        this.name = name;
        this.color = name.charAt(1) - '0';
        this.initialColor = this.color;
        this.radius = INITIAL_R;
        this.position = new PVector(x, y);
        this.initialPosition = new PVector(x, y);
        this.velocity = new PVector(random.nextBoolean() ? 2 : -2, random.nextBoolean() ? 2 : -2);
        setDirection();
        this.onBoard = false;
    }
    
    /**
     * Returns the velocity vector of the ball.
     * 
     * @return the velocity of the ball as a {@code PVector}.
     */
    public PVector getVelocity(){
        return velocity;
    }

    /**
     * Returns the initial radius of the ball.
     * 
     * @return the initial radius of the ball.
     */
    public float getInitialRadius() {
        return INITIAL_R;
    }

    /**
     * Sets the direction of the ball based on its velocity.
     */
    public void setDirection(){
        float x = velocity.x;
        float y = velocity.y;
        if (x>0 && y<0){
            direction = 1;
        } else if (x<0 && y<0) {
            direction = 2;
        } else if (x<0 && y>0) {
            direction = 3;
        } else if (x>0 && y>0) {
            direction = 4;
        }
    }

    /**
     * Returns the current direction of the ball.
     * 
     * @return the direction of the ball represented as an integer.
     */
    public int getDirection(){
        return direction;
    }

    /**
     * Returns the current radius of the ball.
     * 
     * @return the radius of the ball.
     */
    public float getRadius(){
        return radius;
    }

    /**
     * Sets a new name for the ball and updates its color based on the new name.
     * 
     * @param newName the new name of the ball.
     */
    public void setName(String newName) {
        this.name = newName;
        this.color = name.charAt(1) - '0';
    }

    /**
     * Sets the velocity of the ball to a new value.
     * 
     * @param newV the new velocity vector as a {@code PVector}.
     */
    public void setVelocity(PVector newV) {
        // System.out.println("setVelocity, " + newV.x + ", " + newV.y);
        velocity.set(newV);
        setDirection();
    }

    /**
     * Sets the radius of the ball to a new value.
     * 
     * @param newR the new radius of the ball.
     */
    public void setRadius(float newR) {
        radius = newR;
    }

    /**
     * Returns the x-coordinate of the ball's position.
     * 
     * @return the x-coordinate of the ball.
     */
    public float getX(){
        return position.x;
    }

    /**
     * Returns the y-coordinate of the ball's position.
     * 
     * @return the y-coordinate of the ball.
     */
    public float getY(){
        return position.y;
    }

    /**
     * Determines if the ball can be absorbed by the specified invisible circle.
     * 
     * @param circle the {@code InvisibleCircle} object to check against.
     * @return {@code true} if the ball can be absorbed; {@code false} otherwise.
     */
    public boolean absorbed(InvisibleCircle circle){
        onBoard = false;

        // correct color
        boolean sameColor = circle.getColor().charAt(1) - '0' == this.color;
        boolean universalHole = circle.getColor().equals("H0");
        if (sameColor || universalHole || this.color==0) {
            return true;
        }

        // System.out.println(name+" not correct color " + System.identityHashCode(this));
        
        // not correct color
        position.set(initialPosition);
        radius = INITIAL_R;
        color = initialColor;
        name = "B" + Integer.toString(initialColor);
        return false;
    }

    /**
     * Sets the on-board state of the ball.
     * 
     * @param newState {@code true} if the ball is on the board; {@code false} otherwise.
     */
    public void setIsOnboard(boolean newState){
        onBoard = newState;
    }

    /**
     * Applies an attraction force to the ball, modifying its velocity.
     * 
     * @param force the force vector to apply as a {@code PVector}.
     */
    public void attractedTo(PVector force){
        // velocity.add(force);
        // setDirection();
        setVelocity(force.add(velocity));
    }

    /**
     * Checks if the ball is currently on the game board.
     * 
     * @return {@code true} if the ball is on the board; {@code false} otherwise.
     */
    public boolean isOnBoard(){
        if (onBoard) {
            return true;
        }
        return false;
    }

    /**
     * Moves the ball based on its current velocity.
     */
    public void move(){
        position.add(velocity);
    }

    /**
     * Sets the position of the ball to a new value.
     * 
     * @param newPos the new position as a {@code PVector}.
     */
    public void setPosition(PVector newPos) {
        position.set(newPos);
    }

    /**
     * Returns the current position of the ball.
     * 
     * @return the position of the ball as a {@code PVector}.
     */
    public PVector getPosition(){
        return position;
    }

    /**
     * Returns the center position of the ball.
     * 
     * @return the center of the ball as a {@code PVector}.
     */
    public PVector getCentre(){
        return new PVector(position.x + radius, position.y + radius);
    }

    /**
     * Returns the current color of the ball.
     * 
     * @return the color of the ball as an integer.
     */
    public int getColor(){
        return color;
    }
}