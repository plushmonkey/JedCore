package com.jedk1.jedcore.util;

import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MaterialUtil {
    private static final List<Material> TRANSPARENT_MATERIALS = Arrays.asList(
            Material.AIR, Material.VOID_AIR, Material.CAVE_AIR, Material.OAK_SAPLING, Material.SPRUCE_SAPLING, Material.BIRCH_SAPLING,
            Material.JUNGLE_SAPLING, Material.ACACIA_SAPLING, Material.DARK_OAK_SAPLING, Material.WATER,
            Material.LAVA, Material.COBWEB, Material.TALL_GRASS, Material.GRASS, Material.FERN, Material.DEAD_BUSH,
            Material.DANDELION, Material.POPPY, Material.BLUE_ORCHID, Material.ALLIUM,
            Material.AZURE_BLUET, Material.RED_TULIP, Material.ORANGE_TULIP, Material.WHITE_TULIP, Material.PINK_TULIP,
            Material.OXEYE_DAISY, Material.BROWN_MUSHROOM, Material.RED_MUSHROOM, Material.TORCH, Material.FIRE,
            Material.WHEAT, Material.SNOW, Material.SUGAR_CANE, Material.VINE, Material.SUNFLOWER, Material.LILAC,
            Material.LARGE_FERN, Material.ROSE_BUSH, Material.PEONY
    );

    private static List<Material> signMaterials = new ArrayList<>();

    static {
        List<String> potentialSigns = Arrays.asList(
            "SIGN", "WALL_SIGN",
            "ACACIA_SIGN", "ACACIA_WALL_SIGN",
            "BIRCH_SIGN", "BIRCH_WALL_SIGN",
            "DARK_OAK_SIGN", "DARK_OAK_WALL_SIGN",
            "JUNGLE_SIGN", "JUNGLE_WALL_SIGN",
            "OAK_SIGN", "OAK_WALL_SIGN",
            "SPRUCE_SIGN", "SPRUCE_WALL_SIGN"
        );

        // Load up the potential sign types
        for (String signType : potentialSigns) {
            try {
                Material signMaterial = Material.valueOf(signType);

                signMaterials.add(signMaterial);
            } catch (IllegalArgumentException e) {
                // pass
            }
        }
    }

    public static boolean isSign(Material material) {
        return signMaterials.contains(material);
    }

    public static boolean isSign(Block block) {
        return isSign(block.getType());
    }

    // Do a fast lookup by avoiding the region protection check.
    public static boolean isTransparent(Block block) {
        return isTransparent(block.getType());
    }

    // Do a fast lookup by avoiding the region protection check.
    public static boolean isTransparent(Material material) {
        return TRANSPARENT_MATERIALS.contains(material);
    }
}
