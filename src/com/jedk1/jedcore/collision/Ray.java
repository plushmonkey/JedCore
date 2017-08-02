package com.jedk1.jedcore.collision;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class Ray {
    public Vector origin;
    public Vector direction;
    public Vector directionReciprocal;

    public Ray(Vector origin, Vector direction) {
        this.direction = direction;
        this.origin = origin;
        this.directionReciprocal = new Vector(0, 0, 0);

        if (direction.getX() == 0.0) {
            directionReciprocal.setX(Double.MAX_VALUE);
        } else {
            directionReciprocal.setX(1.0 / direction.getX());
        }

        if (direction.getY() == 0.0) {
            directionReciprocal.setY(Double.MAX_VALUE);
        } else {
            directionReciprocal.setY(1.0 / direction.getY());
        }

        if (direction.getZ() == 0.0) {
            directionReciprocal.setZ(Double.MAX_VALUE);
        } else {
            directionReciprocal.setZ(1.0 / direction.getZ());
        }
    }

    public Ray(Location origin, Vector direction) {
        this(origin.toVector(), direction);
    }
}
