package com.jedk1.jedcore.policies.removal;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.function.Supplier;

public class OutOfRangeRemovalPolicy implements RemovalPolicy {
    private Supplier<Location> fromSupplier;
    private Player player;
    private double range;

    public OutOfRangeRemovalPolicy(Player player, double range, Supplier<Location> from) {
        this.player = player;
        this.range = range;
        this.fromSupplier = from;
    }

    @Override
    public boolean shouldRemove() {
        if (this.range == 0) return false;

        Location from = this.fromSupplier.get();
        return from.distanceSquared(this.player.getLocation()) >= (this.range * this.range);
    }

    @Override
    public void load(ConfigurationSection config) {
        this.range = config.getDouble("Range");
    }

    @Override
    public String getName() {
        return "OutOfRange";
    }
}
