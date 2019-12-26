package com.jedk1.jedcore.collision;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.util.Optional;

public class AABB implements Collider {
    public static AABB PlayerBounds = new AABB(new Vector(-0.3, 0.0, -0.3), new Vector(0.3, 1.8, 0.3));
    public static AABB BlockBounds = new AABB(new Vector(0.0, 0.0, 0.0), new Vector(1.0, 1.0, 1.0));

    private Vector min;
    private Vector max;
    private boolean hasVolume;

    public AABB(Block block) {
        this.min = min(block);
        this.max = max(block);

        this.hasVolume = max.clone().subtract(min).lengthSquared() > 0;
    }

    public AABB(Entity entity) {
        this.min = min(entity);
        this.max = max(entity);

        this.hasVolume = max.clone().subtract(min).lengthSquared() > 0;
    }

    public AABB(Vector min, Vector max) {
        this.min = min;
        this.max = max;

        if (min != null && max != null) {
            this.hasVolume = max.clone().subtract(min).lengthSquared() > 0;
        }
    }

    public boolean hasVolume() {
        return this.hasVolume;
    }

    public AABB at(Vector pos) {
        if (min == null || max == null) return new AABB(null, null);

        return new AABB(min.clone().add(pos), max.clone().add(pos));
    }

    public AABB at(Location location) {
        if (min == null || max == null) return new AABB(null, null);

        return at(location.toVector());
    }

    public AABB grow(double x, double y, double z) {
        Vector change = new Vector(x, y, z);

        return new AABB(min.clone().subtract(change), max.clone().add(change));
    }

    public AABB scale(double x, double y, double z) {
        Vector extents = getHalfExtents();
        Vector newExtents = extents.clone().multiply(new Vector(x, y, z));

        Vector diff = newExtents.clone().subtract(extents);
        return grow(diff.getX(), diff.getY(), diff.getZ());
    }

    public AABB scale(double amount) {
        Vector extents = getHalfExtents();
        Vector newExtents = extents.clone().multiply(amount);

        Vector diff = newExtents.clone().subtract(extents);
        return grow(diff.getX(), diff.getY(), diff.getZ());
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

    public boolean contains(Vector test) {
        if (min == null || max == null || !hasVolume) return false;

        return (test.getX() >= min.getX() && test.getX() <= max.getX()) &&
                (test.getY() >= min.getY() && test.getY() <= max.getY()) &&
                (test.getZ() >= min.getZ() && test.getZ() <= max.getZ());
    }

    public Optional<Double> intersects(Ray ray) {
        if (min == null || max == null || !hasVolume) return Optional.empty();

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

    public boolean intersects(AABB other) {
        if (min == null || max == null || other.min == null || other.max == null || !hasVolume || !other.hasVolume) {
            return false;
        }

        return (max.getX() > other.min.getX() &&
                min.getX() < other.max.getX() &&
                max.getY() > other.min.getY() &&
                min.getY() < other.max.getY() &&
                max.getZ() > other.min.getZ() &&
                min.getZ() < other.max.getZ());
    }

    public boolean intersects(Sphere sphere) {
        if (!this.hasVolume) return false;

        return sphere.intersects(this);
    }

    @Override
    public Vector getPosition() {
        return mid();
    }

    @Override
    public Vector getHalfExtents() {
        Vector half = max.clone().subtract(min).multiply(0.5);
        // Return a vector of half extents that reach from mid to box sides.
        return new Vector(Math.abs(half.getX()), Math.abs(half.getY()), Math.abs(half.getZ()));
    }

    private Vector min(Entity entity) {
        Vector worldMin = entity.getBoundingBox().getMin();

        return worldMin.clone().subtract(entity.getLocation().toVector());
    }

    private Vector max(Entity entity) {
        Vector worldMax = entity.getBoundingBox().getMax();

        return worldMax.clone().subtract(entity.getLocation().toVector());
    }

    private Vector min(Block block) {
        if (block.isPassable()) {
            return new Vector(0, 0, 0);
        }

        Vector worldMin = block.getBoundingBox().getMin();

        return worldMin.clone().subtract(block.getLocation().toVector());
    }

    private Vector max(Block block) {
        if (block.isPassable()) {
            return new Vector(0, 0, 0);
        }

        Vector worldMax = block.getBoundingBox().getMax();

        return worldMax.clone().subtract(block.getLocation().toVector());
    }
}
