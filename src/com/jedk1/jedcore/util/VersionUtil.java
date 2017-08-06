package com.jedk1.jedcore.util;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.chiblocking.combo.Immobilize;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class VersionUtil {
    private static Method playSandBending = null;
    private static Method isImmobilizedMethod = null;

    static {
        setupSandbendingSound();
        setupImmobilize();
    }

    public static void playSandbendingSound(Location loc) {
        if (playSandBending != null) {
            try {
                playSandBending.invoke(null, loc);
                return;
            } catch (IllegalAccessException | InvocationTargetException e) {

            }
        }

        EarthAbility.playSandbendingSound(loc);
    }

    private static void setupSandbendingSound() {
        try {
            Class<?> earthAbility = Class.forName("com.projectkorra.projectkorra.ability.EarthAbility");
            playSandBending = earthAbility.getDeclaredMethod("playSandBendingSound", Location.class);
        } catch (ClassNotFoundException | NoSuchMethodException e) {

        }
    }

    private static void setupImmobilize() {
        try {
            Class <?> comboClass = Class.forName("com.projectkorra.projectkorra.chiblocking.combo.ChiCombo");
            isImmobilizedMethod = comboClass.getDeclaredMethod("isParalyzed", Player.class);
        } catch (ClassNotFoundException | NoSuchMethodException e) {

        }
    }
}
