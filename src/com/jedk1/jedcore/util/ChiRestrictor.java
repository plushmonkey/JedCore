package com.jedk1.jedcore.util;

import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.configuration.JedCoreConfig;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.event.AbilityStartEvent;
import com.projectkorra.projectkorra.event.BendingReloadEvent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// Stops chi abilities from being activated from ranged attacks.
// Only works on Chi abilities that have a public method with the signature "Entity getTarget()".
public class ChiRestrictor implements Listener {
    private static Map<Class<? extends ChiAbility>, Method> cache = new HashMap<>();
    private static Set<String> whitelist = new HashSet<>();

    private static boolean enabled;
    private static boolean resetCooldown;
    private static double meleeDistanceSq;

    static {
        reloadConfig();
    }

    @EventHandler
    public void onBendingReload(BendingReloadEvent event) {
        new BukkitRunnable() {
            public void run() {
                reloadConfig();
            }
        }.runTaskLater(JedCore.plugin, 1);
    }

    public static void reloadConfig() {
        whitelist.clear();

        ConfigurationSection config = JedCoreConfig.getConfig((Player)null);

        enabled = config.getBoolean("Properties.ChiRestrictor.Enabled");
        resetCooldown = config.getBoolean("Properties.ChiRestrictor.ResetCooldown");
        double meleeDistance = config.getDouble("Properties.ChiRestrictor.MeleeDistance");

        meleeDistanceSq = meleeDistance * meleeDistance;

        for (String abilityName : config.getStringList("Properties.ChiRestrictor.Whitelist")) {
            whitelist.add(abilityName.toLowerCase());
        }
    }

    @EventHandler
    public void onAbilityStart(AbilityStartEvent event) {
        if (!enabled || event.isCancelled() || !(event.getAbility() instanceof ChiAbility)) {
            return;
        }

        ChiAbility ability = (ChiAbility)event.getAbility();
        if (isWhitelisted(ability.getName())) {
            return;
        }

        Entity target = getTarget(ability);
        if (target == null) {
            return;
        }

        Player player = event.getAbility().getPlayer();

        if (player.getWorld() != target.getWorld()) {
            return;
        }

        double distanceSq = target.getLocation().distanceSquared(player.getLocation());
        if (distanceSq > meleeDistanceSq) {
            event.setCancelled(true);

            if (resetCooldown) {
                ability.getBendingPlayer().removeCooldown(ability);
            }
        }
    }

    private static boolean isWhitelisted(String name) {
        return whitelist.contains(name.toLowerCase());
    }

    private static Entity getTarget(ChiAbility ability) {
        Class<? extends ChiAbility> clazz = ability.getClass();

        Method method;

        // Check to see if this class was stored in cache to minimize reflection usage.
        if (cache.containsKey(clazz)) {
            method = cache.get(clazz);

            // method will be null when the class has already been checked and didn't have getTarget.
            if (method == null) {
                return null;
            }
        } else {
            // Use reflection to see if the ability has getTarget method.
            // Store this in a map to minimize reflection usage.
            try {
                method = clazz.getDeclaredMethod("getTarget");

                cache.put(clazz, method);
            } catch (NoSuchMethodException e) {
                cache.put(clazz, null);
                return null;
            }
        }

        // Call the getTarget method if it exists and return the Entity.
        try {
            Object entity = method.invoke(ability);

            if (entity instanceof Entity) {
                return (Entity)entity;
            }

            return null;
        } catch (IllegalAccessException|InvocationTargetException e) {
            return null;
        }
    }
}
