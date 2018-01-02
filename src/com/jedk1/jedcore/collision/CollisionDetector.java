package com.jedk1.jedcore.collision;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;

public class CollisionDetector {
    // Checks a sphere collider to see if it's hitting any entities near it.
    // Calls the CollisionCallback when hitting a target.
    // Returns true if it hits a target.
    public static boolean checkEntityCollisions(Player player, Sphere collider, CollisionCallback function) {
        double maxRange = collider.radius * 4;

        World world = player.getWorld();
        Location location = new Location(world, collider.center.getX(), collider.center.getY(), collider.center.getZ());

        boolean hit = false;

        for (Entity entity : location.getWorld().getNearbyEntities(location, maxRange, maxRange, maxRange)) {
            if (!(entity instanceof LivingEntity)) continue;
            if (entity instanceof ArmorStand) continue;
            if (entity == player) continue;

            AABB entityBounds = new AABB(entity).at(entity.getLocation());

            if (collider.intersects(entityBounds)) {
                if (function.onCollision((LivingEntity)entity)) {
                    return true;
                }
                hit = true;
            }
        }

        return hit;
    }

    // Checks if the entity is on the ground. Uses NMS bounding boxes for accuracy.
    public static boolean isOnGround(Entity entity) {
        final double epsilon = 0.01;

        Location location = entity.getLocation();
        AABB entityBounds = new AABB(entity).at(location.clone().subtract(0, epsilon, 0));

        for (int x = -1; x <= 1; ++x) {
            for (int z = -1; z <= 1; ++z) {
                Block checkBlock = location.clone().add(x, -epsilon, z).getBlock();
                if (checkBlock.getType() == Material.AIR) continue;

                AABB checkBounds = new AABB(checkBlock).at(checkBlock.getLocation());

                if (entityBounds.intersects(checkBounds)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static double distanceAboveGround(Entity entity) {
        return distanceAboveGround(entity, Collections.emptySet());
    }

    // Cast a ray down to find how far above the ground this entity is.
    public static double distanceAboveGround(Entity entity, Set<Material> groundMaterials) {
        Location location = entity.getLocation().clone();
        Ray ray = new Ray(location, new Vector(0, -1, 0));

        for (double y = location.getY() - 1; y >= 0; --y) {
            location.setY(y);

            Block block = location.getBlock();
            AABB checkBounds;

            if (groundMaterials.contains(block.getType())) {
                checkBounds = AABB.BlockBounds;
            } else {
                checkBounds = new AABB(block);
            }

            checkBounds = checkBounds.at(block.getLocation());

            Optional<Double> rayHit = checkBounds.intersects(ray);

            if (rayHit.isPresent()) {
                return rayHit.get();
            }
        }

        return Double.MAX_VALUE;
    }

    public interface CollisionCallback {
        // return true to break out of the loop
        boolean onCollision(LivingEntity e);
    }
}
