package inkball;

import processing.core.PVector;

/**
 * The {@code InvisibleLine} class represents an invisible line segment 
 * hitbox in the Inkball game. This class extends the {@code Hitbox} 
 * abstract class and provides a specific implementation for line 
 * hitboxes, allowing for collision detection along a linear path.
 * 
 * <p>Instances of this class are used to define areas in the game 
 * where interactions occur along a line without being visually 
 * represented, enabling precise gameplay mechanics.</p>
 */
public class InvisibleLine extends Hitbox {
    /** The direction of the line, indicating the allowed direction for normal vectors. */
    private String direction;

    /**
     * Constructs an {@code InvisibleLine} with the specified start and end points, 
     * color, and direction.
     * 
     * @param start the starting point of the line as a {@code PVector}.
     * @param end the ending point of the line as a {@code PVector}.
     * @param color the color of the line, represented as a {@code String}.
     * @param direction the allowed direction for normal vectors as a {@code String}.
     */
    public InvisibleLine(PVector start, PVector end, String color, String direction) {
        this.start = start;
        this.end = end;
        this.color = color; // Store the color for potential use
        this.direction = direction;
    }

    /**
     * Returns the direction of the line.
     * 
     * @return the direction of the line as a {@code String}.
     */
    public String getDirection() {
        return direction;
    }

}