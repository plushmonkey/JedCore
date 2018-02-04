package com.jedk1.jedcore.util;

import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.configuration.JedCoreConfig;
import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.event.BendingPlayerCreationEvent;
import com.projectkorra.projectkorra.event.BendingReloadEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// Remembers cooldowns for players that log off and back in.
public class CooldownEnforcer implements Listener {
    private static Field cooldownField = null;
    private static boolean enabled;
    private static boolean onReload;

    // Stores the old BendingPlayer object between login and BendingPlayer creation.
    private Map<UUID, BendingPlayer> playerCache = new HashMap<>();

    static {
        try {
            Class<?> bPlayerClass = Class.forName("com.projectkorra.projectkorra.BendingPlayer");

            cooldownField = bPlayerClass.getDeclaredField("cooldowns");
            cooldownField.setAccessible(true);
        } catch (ClassNotFoundException|NoSuchFieldException e) {
            JedCore.log.warning("Failed to load BendingPlayer#cooldowns field. Disabling CooldownEnforcer.");
            cooldownField = null;
        }

        onConfigReload();
    }

    public static void onConfigReload() {
        ConfigurationSection config = JedCoreConfig.getConfig((Player)null);

        enabled = config.getBoolean("Properties.CooldownEnforcer.Enabled");
        onReload = config.getBoolean("Properties.CooldownEnforcer.OnReload");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        cachePlayer(player);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBendingReload(BendingReloadEvent event) {
        if (!isEnabled() || !onReload) return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            cachePlayer(player);
        }
    }

    @EventHandler
    public void onBendingPlayerCreation(BendingPlayerCreationEvent event) {
        if (!isEnabled()) return;

        BendingPlayer newBendingPlayer = event.getBendingPlayer();
        BendingPlayer oldBendingPlayer = playerCache.get(newBendingPlayer.getUUID());

        if (oldBendingPlayer == null) {
            return;
        }

        playerCache.remove(newBendingPlayer.getUUID());

        try {
            Object cooldowns = cooldownField.get(oldBendingPlayer);
            cooldownField.set(newBendingPlayer, cooldowns);
        } catch (IllegalAccessException e) {

        }
    }

    public boolean isEnabled() {
        return cooldownField != null && enabled;
    }

    private void cachePlayer(Player player) {
        BendingPlayer bPlayer = BendingPlayer.getPlayers().get(player.getUniqueId());

        if (bPlayer == null) {
            // The player logging in isn't stored in memory, so they have no cooldowns.
            return;
        }

        // Save the old BendingPlayer so the cooldowns can be copied over after the new one is created.
        playerCache.put(player.getUniqueId(), bPlayer);
    }
}
