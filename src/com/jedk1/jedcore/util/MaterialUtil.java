package com.jedk1.jedcore.util;

import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.Arrays;
import java.util.List;

public class MaterialUtil {
    private static final List<Material> TRANSPARENT_MATERIALS = Arrays.asList(
            Material.AIR, Material.OAK_SAPLING, Material.SPRUCE_SAPLING, Material.BIRCH_SAPLING,
            Material.JUNGLE_SAPLING, Material.ACACIA_SAPLING, Material.DARK_OAK_SAPLING, Material.WATER,
            Material.LAVA, Material.COBWEB, Material.TALL_GRASS, Material.GRASS, Material.FERN, Material.DEAD_BUSH,
            Material.DANDELION, Material.DANDELION_YELLOW, Material.POPPY, Material.BLUE_ORCHID, Material.ALLIUM,
            Material.AZURE_BLUET, Material.RED_TULIP, Material.ORANGE_TULIP, Material.WHITE_TULIP, Material.PINK_TULIP,
            Material.OXEYE_DAISY, Material.BROWN_MUSHROOM, Material.RED_MUSHROOM, Material.TORCH, Material.FIRE,
            Material.WHEAT, Material.SNOW, Material.SUGAR_CANE, Material.VINE, Material.SUNFLOWER, Material.LILAC,
            Material.LARGE_FERN, Material.ROSE_BUSH, Material.PEONY
    );

    // Do a fast lookup by avoiding the region protection check.
    public static boolean isTransparent(Block block) {
        return isTransparent(block.getType());
    }

    // Do a fast lookup by avoiding the region protection check.
    public static boolean isTransparent(Material material) {
        return TRANSPARENT_MATERIALS.contains(material);
    }
}
