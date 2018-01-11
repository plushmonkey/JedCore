package com.jedk1.jedcore.collision;

import org.bukkit.util.Vector;

public class Sphere implements Collider {
    public Vector center;
    public double radius;

    public Sphere(Vector center, double radius) {
        this.center = center;
        this.radius = radius;
    }

    public Sphere at(Vector newCenter) {
        return new Sphere(newCenter, radius);
    }

    public boolean intersects(AABB aabb) {
        Vector min = aabb.min();
        Vector max = aabb.max();

        // Get the point closest to sphere center on the aabb.
        double x = Math.max(min.getX(), Math.min(center.getX(), max.getX()));
        double y = Math.max(min.getY(), Math.min(center.getY(), max.getY()));
        double z = Math.max(min.getZ(), Math.min(center.getZ(), max.getZ()));

        // Check if that point is inside of the sphere.
        return contains(new Vector(x, y, z));
    }

    public boolean intersects(Sphere other) {
        double distSq = other.center.distanceSquared(center);
        double rsum = radius + other.radius;

        // Spheres will be colliding if their distance apart is less than the sum of the radii.
        return distSq <= rsum * rsum;
    }

    @Override
    public Vector getPosition() {
        return center.clone();
    }

    @Override
    public Vector getHalfExtents() {
        return new Vector(radius, radius, radius);
    }

    public boolean contains(Vector point) {
        double distSq = center.distanceSquared(point);
        return distSq <= radius * radius;
    }
}
