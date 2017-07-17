package com.jedk1.jedcore.util;

import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.Arrays;
import java.util.List;

public class MaterialUtil {
    private static final List<Integer> TRANSPARENT_MATERIALS = Arrays.asList(0, 6, 8, 9, 10, 11, 30, 31, 32, 37, 38, 39, 40, 50, 51, 59, 78, 83, 106, 175);

    // Do a fast lookup by avoiding the region protection check.
    public static boolean isTransparent(Block block) {
        return isTransparent(block.getType());
    }

    // Do a fast lookup by avoiding the region protection check.
    public static boolean isTransparent(Material material) {
        return TRANSPARENT_MATERIALS.contains(material.getId());
    }
}
