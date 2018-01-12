package com.jedk1.jedcore.collision;

import com.jedk1.jedcore.util.TempFallingBlock;
import com.projectkorra.projectkorra.ability.util.Collision;
import org.bukkit.Location;

import java.util.Iterator;
import java.util.List;

public final class CollisionUtil {
    private CollisionUtil() {

    }

    public static void handleFallingBlockCollisions(Collision collision, List<TempFallingBlock> fallingBlocks) {
        if (collision.isRemovingFirst()) {
            Location location = collision.getLocationSecond();
            double firstRadius = collision.getAbilityFirst().getCollisionRadius();
            double secondRadius = collision.getAbilitySecond().getCollisionRadius();
            double collisionRadiusSq = (firstRadius + secondRadius) * (firstRadius + secondRadius);

            // Loop through all falling blocks because the collision system stops on the first collision.
            for (Iterator<TempFallingBlock> iterator = fallingBlocks.iterator(); iterator.hasNext();) {
                TempFallingBlock tfb = iterator.next();

                // Check if this falling block is within collision radius
                if (tfb.getLocation().distanceSquared(location) <= collisionRadiusSq) {
                    tfb.remove();
                    iterator.remove();
                }
            }
        }
    }
}
