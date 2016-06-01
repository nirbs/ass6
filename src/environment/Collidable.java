package environment;

import game.Velocity;
import geometry.Point;
import geometry.Rectangle;
import sprites.Ball;

/**
 * The Collidable holds 2 statements of any Collidable object.
 *
 * @author Matan Ben Noach Nir Ben Shalom
 * @version 1.0 9 April 2016
 */
public interface Collidable {
    /**
     * getCollisionRectangle returns the rectangle shape of the collision.
     *
     * @return the "collision shape" of the object.
     */
    Rectangle getCollisionRectangle();

    /**
     * Notify the object that we collided with it at collisionPoint with a given
     * velocity.
     *
     * @param hitter          is the specific ball that is about to hit the block.
     * @param collisionPoint  is the collision point of an object with the block.
     * @param currentVelocity is the current velocity of the object that will collide with the block.
     * @return the new velocity after the hit.
     */
    Velocity hit(Ball hitter, Point collisionPoint, Velocity currentVelocity);
}
