package com.jedk1.jedcore.collision;

import org.bukkit.util.Vector;

import java.util.Optional;

public class AABB {
    public static AABB PlayerBounds = new AABB(new Vector(-0.3, 0.0, -0.3), new Vector(0.3, 1.8, 0.3));
    public static AABB BlockBounds = new AABB(new Vector(0.0, 0.0, 0.0), new Vector(1.0, 1.0, 1.0));
    private Vector min;
    private Vector max;

    public AABB(Vector min, Vector max) {
        this.min = min;
        this.max = max;
    }

    public AABB at(Vector pos) {
        return new AABB(min.clone().add(pos), max.clone().add(pos));
    }

    public Vector min() {
        return this.min;
    }

    public Vector max() {
        return this.max;
    }

    public Vector mid() {
        return this.min.clone().add(this.max().clone().subtract(this.min()).multiply(0.5));
    }

    public boolean intersects(AABB other) {
        return (max.getX() > other.min.getX() &&
                min.getX() < other.max.getX() &&
                max.getY() > other.min.getY() &&
                min.getY() < other.max.getY() &&
                max.getZ() > other.min.getZ() &&
                min.getZ() < other.max.getZ());
    }

    public Optional<Double> intersects(Ray ray) {
        if (min == null || max == null) return Optional.empty();

        double t1 = (min.getX() - ray.origin.getX()) * ray.directionReciprocal.getX();
        double t2 = (max.getX() - ray.origin.getX()) * ray.directionReciprocal.getX();

        double t3 = (min.getY() - ray.origin.getY()) * ray.directionReciprocal.getY();
        double t4 = (max.getY() - ray.origin.getY()) * ray.directionReciprocal.getY();

        double t5 = (min.getZ() - ray.origin.getZ()) * ray.directionReciprocal.getZ();
        double t6 = (max.getZ() - ray.origin.getZ()) * ray.directionReciprocal.getZ();

        double tmin = Math.max(Math.max(Math.min(t1, t2), Math.min(t3, t4)), Math.min(t5, t6));
        double tmax = Math.min(Math.min(Math.max(t1, t2), Math.max(t3, t4)), Math.max(t5, t6));

        if (tmax < 0 || tmin > tmax) {
            return Optional.empty();
        }

        return Optional.of(tmin);
    }
}
