package com.jedk1.jedcore.policies.removal;

import org.bukkit.entity.Player;

public class IsOfflineRemovalPolicy implements RemovalPolicy {
    private Player player;

    public IsOfflineRemovalPolicy(Player player) {
        this.player = player;
    }

    @Override
    public boolean shouldRemove() {
        return this.player == null || !this.player.isOnline();
    }

    @Override
    public String getName() {
        return "IsOffline";
    }
}
