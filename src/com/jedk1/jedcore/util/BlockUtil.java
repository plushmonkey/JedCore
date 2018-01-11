package com.jedk1.jedcore.util;

import com.jedk1.jedcore.collision.AABB;
import org.bukkit.Location;
import org.bukkit.entity.FallingBlock;

public final class BlockUtil {
    private BlockUtil() {

    }

    public static AABB getFallingBlockBoundsFull(FallingBlock fb) {
        Location loc = fb.getLocation();

        // Subtract to line the bounding box up with the corner of the box.
        // The FallingBlock#getLocation returns the center of the block at the bottom.
        return AABB.BlockBounds.at(loc.subtract(0.5, 0, 0.5));
    }
}
