package inkball;
import processing.core.PVector;

/**
 * The {@code InvisibleCircle} class represents an invisible circular hitbox 
 * in the Inkball game. This class extends the {@code Hitbox} abstract class 
 * and provides a specific implementation for circular hitboxes, allowing 
 * for collision detection and interactions without visual representation.
 * 
 * <p>Instances of this class are used to define areas in the game where 
 * interactions occur without being visible to the player, enhancing gameplay 
 * mechanics.</p>
 */
public class InvisibleCircle extends Hitbox {
    /**
     * Constructs an {@code InvisibleCircle} with the specified center, radius, 
     * and color.
     * 
     * @param center the center point of the circle as a {@code PVector}.
     * @param radius the radius of the circle.
     * @param color the color of the circle, represented as a {@code String}.
     */
    public InvisibleCircle(PVector center, int radius, String color) {
        this.center = center;
        this.radius = radius;
        this.color = color;
    }
}
