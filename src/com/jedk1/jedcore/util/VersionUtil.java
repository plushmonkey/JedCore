package com.jedk1.jedcore.util;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.earthbending.passive.DensityShift;
import com.projectkorra.projectkorra.util.MovementHandler;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class VersionUtil {
    private static Method playSandBendingMethod = null;
    private static Method isImmobilizedMethod = null;
    private static Method getTargetedLocationMethod = null;
    private static Method isPassiveSandMethod = null;
    private static Method revertSandMethod = null;
    private static Field nonOpaque = null;
    private static boolean hasMovementHandler = false;

    static {
        setupSandbendingSound();
        setupImmobilize();
        setupTargetedLocation();
        setupMovementHandler();
        setupEarthPassive();
    }

    public static Location getTargetedLocation(Player player, double range, Material... materials) {
        if (getTargetedLocationMethod != null) {
            try {
                if (materials == null) {
                    return (Location)getTargetedLocationMethod.invoke(null, player, range);
                }

                Integer[] materialIds = Arrays.stream(materials).map(Material::getId).toArray(Integer[]::new);

                return (Location)getTargetedLocationMethod.invoke(null, player, range, materialIds);
            } catch (IllegalAccessException | InvocationTargetException e) {

            }
        }

        return GeneralMethods.getTargetedLocation(player, range, materials);
    }

    public static Location getTargetedLocationTransparent(Player player, double range) {
        if (getTargetedLocationMethod != null) {
            try {
                Integer[] materialIds = (Integer[])nonOpaque.get(null);

                return (Location)getTargetedLocationMethod.invoke(null, player, range, materialIds);
            } catch (IllegalAccessException | InvocationTargetException e) {

            }
        }

        Material[] transparent = GeneralMethods.NON_OPAQUE;
        return GeneralMethods.getTargetedLocation(player, range, transparent);
    }

    public static void playSandbendingSound(Location loc) {
        if (playSandBendingMethod != null) {
            try {
                playSandBendingMethod.invoke(null, loc);
                return;
            } catch (IllegalAccessException | InvocationTargetException e) {

            }
        }

        EarthAbility.playSandbendingSound(loc);
    }

    public static boolean isImmobilized(Player player) {
        if (isImmobilizedMethod != null) {
            try {
                return (boolean)isImmobilizedMethod.invoke(null, player);
            } catch (Exception e) {

            }
        }
        return false;
    }

    public static boolean isStopped(Player player) {
        if (hasMovementHandler)
            return MovementHandler.isStopped(player);

        BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
        return bPlayer.isParalyzed() || isImmobilized(player);
    }

    public static boolean isPassiveSand(Block block) {
        if (isPassiveSandMethod != null) {
            try {
                return (boolean)isPassiveSandMethod.invoke(null, block);
            } catch (Exception e) {

            }
        }

        return DensityShift.isPassiveSand(block);
    }

    public static void revertSand(Block block) {
        if (revertSandMethod != null) {
            try {
                revertSandMethod.invoke(null, block);
                return;
            } catch (Exception e) {

            }
        }

        DensityShift.revertSand(block);
    }

    private static void setupEarthPassive() {
        try {
            Class<?> earthPassive = Class.forName("com.projectkorra.projectkorra.earthbending.passive.EarthPassive");

            isPassiveSandMethod = earthPassive.getDeclaredMethod("isPassiveSand", Block.class);
            revertSandMethod = earthPassive.getDeclaredMethod("revertSand", Block.class);
        } catch (ClassNotFoundException | NoSuchMethodException e) {

        }
    }

    private static void setupMovementHandler() {
        try {
            Class<?> movementHandler = Class.forName("com.projectkorra.projectkorra.util.MovementHandler");
            hasMovementHandler = true;
        } catch (ClassNotFoundException e) {

        }
    }

    private static void setupTargetedLocation() {
        try {
            Class<?> generalMethods = Class.forName("com.projectkorra.projectkorra.GeneralMethods");
            getTargetedLocationMethod = generalMethods.getDeclaredMethod("getTargetedLocation", Player.class, double.class, Integer[].class);
            nonOpaque = generalMethods.getDeclaredField("NON_OPAQUE");
        } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException e) {

        }
    }

    private static void setupSandbendingSound() {
        try {
            Class<?> earthAbility = Class.forName("com.projectkorra.projectkorra.ability.EarthAbility");
            playSandBendingMethod = earthAbility.getDeclaredMethod("playSandBendingSound", Location.class);
        } catch (ClassNotFoundException | NoSuchMethodException e) {

        }
    }

    private static void setupImmobilize() {
        try {
            Class <?> comboClass = Class.forName("com.projectkorra.projectkorra.chiblocking.combo.ChiCombo");
            isImmobilizedMethod = comboClass.getDeclaredMethod("isParalyzed", Player.class);
            return;
        } catch (ClassNotFoundException | NoSuchMethodException e) {

        }

        try {
            Class <?> immobilize = Class.forName("com.projectkorra.projectkorra.chiblocking.Immobilize");
            isImmobilizedMethod = immobilize.getDeclaredMethod("isParalyzed", Player.class);
        } catch (ClassNotFoundException | NoSuchMethodException e) {

        }
    }
}
