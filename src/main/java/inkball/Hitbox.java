package inkball;

import processing.core.PVector;
import java.util.ArrayList;

/**
 * The {@code Hitbox} class is an abstract representation of a hitbox in the Inkball 
 * game, serving as a base for specific types of hitboxes such as circles or lines. 
 * It provides common attributes and methods to define the properties of a hitbox 
 * used for collision detection and interaction within the game world.
 * 
 * <p>This class includes properties for defining the hitbox's center, radius, 
 * color, and shape, allowing for flexible implementation in derived classes.</p>
 */
public abstract class Hitbox {
    /** The center point of the hitbox as a {@code PVector}. */
    protected PVector center; 

    /** The radius of the hitbox. */
    protected int radius;

    /** The color of the hitbox, represented as a string. */
    protected String color; 

    /** The starting point of a line segment hitbox, if applicable. */
    protected PVector start;
    
    /** The ending point of a line segment hitbox, if applicable. */
    protected PVector end;
    
    /** The current shape of the hitbox, represented as a list of points. */
    protected ArrayList<PVector> currentShape;

    /**
     * Returns the center point of the hitbox.
     * 
     * @return the center of the hitbox as a {@code PVector}.
     */
    public PVector getCenter() {
        return center;
    }

    /**
     * Returns the radius of the hitbox.
     * 
     * @return the radius of the hitbox.
     */
    public int getRadius() {
        return radius;
    }

    /**
     * Returns the color of the hitbox.
     * 
     * @return the color of the hitbox as a {@code String}.
     */
    public String getColor(){
        return color;
    }
}
