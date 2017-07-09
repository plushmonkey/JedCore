package com.jedk1.jedcore.util;

import com.projectkorra.projectkorra.airbending.AirShield;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class AirShieldReflector {
    public static void reflect(AirShield shield, Location location, Vector direction) {
        Location shieldLocation = shield.getPlayer().getEyeLocation().clone();
        double radius = shield.getRadius();

        if (shieldLocation.distanceSquared(location) > radius * radius)
            return;

        Vector normal = location.toVector().subtract(shieldLocation.toVector()).normalize();
        // Move this instance so it's at the edge of the shield.
        Location newLocation = shieldLocation.clone().add(normal.clone().multiply(radius));

        location.setX(newLocation.getX());
        location.setY(newLocation.getY());
        location.setZ(newLocation.getZ());

        // Reflect the direction about the normal.
        direction.subtract(normal.clone().multiply(2 * direction.dot(normal))).normalize();
    }

    private AirShieldReflector() { }
}
