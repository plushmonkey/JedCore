package com.jedk1.jedcore.ability.earthbending;

import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.util.RegenTempBlock;
import com.jedk1.jedcore.util.TempFallingBlock;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.earthbending.passive.EarthPassive;
import com.projectkorra.projectkorra.util.TempBlock;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Random;

public class EarthSurf extends EarthAbility implements AddonAbility {

	private Location location;
	private double initHealth;
	private long time;
	Random rand = new Random();

	private boolean couldFly = false, wasFlying = false;

	//Player Positioning
	private double distOffset = 2.5;
	private double heightOffset = 1.5;

	private long cooldown;
	private long duration;
	private boolean cooldownEnabled;
	private boolean durationEnabled;
	private double speed;

	public EarthSurf(Player player) {
		super(player);

		if (!bPlayer.canBend(this)) {
			return;
		}

		if (hasAbility(player, EarthSurf.class)) {
			((EarthSurf) getAbility(player, EarthSurf.class)).remove();
			return;
		}
		setFields();
		time = System.currentTimeMillis();
		location = player.getLocation();
		if (isEarthbendable(player, getBlockBeneath(player.getLocation().clone())) && !isMetal(getBlockBeneath(player.getLocation().clone()))) {
			initHealth = player.getHealth();
			couldFly = player.getAllowFlight();
			wasFlying = player.isFlying();
			start();
		}
	}

	public void setFields() {
		cooldown = JedCore.plugin.getConfig().getLong("Abilities.Earth.EarthSurf.Cooldown.Cooldown");
		duration = JedCore.plugin.getConfig().getLong("Abilities.Earth.EarthSurf.Duration.Duration");
		cooldownEnabled = JedCore.plugin.getConfig().getBoolean("Abilities.Earth.EarthSurf.Cooldown.Enabled");
		durationEnabled = JedCore.plugin.getConfig().getBoolean("Abilities.Earth.EarthSurf.Duration.Enabled");
		speed = JedCore.plugin.getConfig().getDouble("Abilities.Earth.EarthSurf.Speed");
	}

	@Override
	public void progress() {
		if (player == null || player.isDead() || !player.isOnline()) {
			remove();
			return;
		}
		if (!bPlayer.canBendIgnoreCooldowns(this)) {
			remove();
			return;
		}
		if (!isEarthbendable(player, getBlockBeneath(player.getLocation().clone()))) {
			remove();
			return;
		}
		if (durationEnabled && System.currentTimeMillis() > time + duration) {
			remove();
			return;
		}
		if (!collision() && player.getHealth() >= initHealth) {
			movePlayer();
		} else {
			remove();
			return;
		}

		if (player.isSneaking()) {
			remove();
			return;
		}
	}
	
	private void allowFlight() {
		player.setAllowFlight(true);
		player.setFlying(true);
	}

	private void removeFlight() {
		player.setAllowFlight(false);
		player.setFlying(false);
	}
	
	private void movePlayer() {

		location = player.getEyeLocation();
		location.setPitch(0);
		Vector dV = location.getDirection().normalize();
		Vector travel = new Vector();

		if (getPlayerDistance() > heightOffset + 2) {
			remove();
			return;
		} else if (getPlayerDistance() > heightOffset + 1) {
			removeFlight();
			travel = new Vector(dV.getX() * speed, -0.11, dV.getZ() * speed);
		} else if (getPlayerDistance() > heightOffset + 0.8) {
			travel = new Vector(dV.getX() * speed, -0.09, dV.getZ() * speed);
		} else if (getPlayerDistance() < heightOffset + 0.7) {
			allowFlight();
			travel = new Vector(dV.getX() * speed, 0.11, dV.getZ() * speed);
		} else {
			travel = new Vector(dV.getX() * speed, 0, dV.getZ() * speed);
		}

		rideWave();
		player.setVelocity(travel);
		player.setFallDistance(0);
	}

	private double getPlayerDistance() {
		Location l = player.getLocation().clone();
		while (l.getBlock() != null && l.getBlockY() > 1 && !GeneralMethods.isSolid(l.getBlock())) {
			l.add(0, -0.1, 0);
		}
		return player.getLocation().getY() - l.getY();
	}

	private Block getBlockBeneath(Location l) {
		while (l.getBlock() != null && l.getBlockY() > 1 && isTransparent(l.getBlock())) {
			l.add(0, -0.5, 0);
		}
		return l.getBlock();
	}

	private boolean collision() {
		Location l = player.getEyeLocation();
		l.setPitch(0);
		Vector dV = l.getDirection().normalize();
		l.add(new Vector(dV.getX() * 0.8, 0, dV.getZ() * 0.8));

		if (!isTransparent(l.getBlock()) || l.getBlock().isLiquid() || l.getBlock().getType().isSolid())
			return true;
		if (!isTransparent(l.clone().add(0, -1, 0).getBlock()) || l.clone().add(0, -1, 0).getBlock().isLiquid() || l.clone().add(0, -1, 0).getBlock().getType().isSolid())
			return true;
		if (!isTransparent(l.clone().add(0, -2, 0).getBlock()) || l.clone().add(0, -2, 0).getBlock().isLiquid() || l.clone().add(0, -2, 0).getBlock().getType().isSolid())
			return true;
		return false;
	}

	@SuppressWarnings("deprecation")
	private void rideWave() {
		for (int i = 0; i < 3; i++) {
			Location loc = location.clone();
			if (i < 2)
				loc.add(getSideDirection(i));

			Location bL = loc.clone().add(0, -2.9, 0).toVector().add(location.clone().getDirection().multiply(distOffset)).toLocation(player.getWorld());
			while (loc.clone().add(0, -2.9, 0).toVector().add(location.clone().getDirection().multiply(distOffset)).toLocation(player.getWorld()).getBlock().getType() != Material.AIR) {
				loc.add(0, 0.1, 0);
			}
			if (isEarthbendable(player, getBlockBeneath(loc.clone().add(0, -2.9, 0).toVector().add(location.clone().getDirection().multiply(distOffset)).toLocation(player.getWorld()))) && getBlockBeneath(bL) != null) {
				Block block = loc.clone().add(0, -3.9, 0).toVector().add(location.clone().getDirection().multiply(distOffset - 0.5)).toLocation(player.getWorld()).getBlock();
				Location temp = loc.clone().add(0, -2.9, 0).toVector().add(location.clone().getDirection().multiply(distOffset)).toLocation(player.getWorld());

				if (EarthPassive.isPassiveSand(block)) {
					EarthPassive.revertSand(block);
				}

				if (!GeneralMethods.isSolid(block.getLocation().add(0, 1, 0).getBlock()) && block.getLocation().add(0, 1, 0).getBlock().getType() != null && block.getLocation().add(0, 1, 0).getBlock().getType() != Material.AIR) {
					if (EarthPassive.isPassiveSand(block.getRelative(BlockFace.UP))) {
						EarthPassive.revertSand(block.getRelative(BlockFace.UP));
					}
					new TempBlock(block.getRelative(BlockFace.UP), Material.AIR, (byte) 0);
				}
				
				new RegenTempBlock(block, Material.AIR, (byte) 0, 1000L);
				new TempFallingBlock(temp, getBlockBeneath(bL).getType(), getBlockBeneath(bL).getData(), new Vector(0, 0.25, 0), this);
				
				for (Entity e : GeneralMethods.getEntitiesAroundPoint(loc.clone().add(0, -2.9, 0).toVector().add(location.clone().getDirection().multiply(distOffset)).toLocation(player.getWorld()), 1.5D)) {
					if (e instanceof LivingEntity && e.getEntityId() != player.getEntityId()) {
						e.setVelocity(new Vector(0, 0.3, 0));
					}
				}
			}
		}
	}

	private Vector getSideDirection(int side) {
		Vector direction = location.clone().getDirection().normalize();
		switch (side) {
			case 0: // RIGHT
				return new Vector(-direction.getZ(), 0.0, direction.getX()).normalize();
			case 1: // LEFT
				return new Vector(direction.getZ(), 0.0, -direction.getX()).normalize();
			default:
				break;
		}

		return null;
	}

	@Override
	public void remove() {
		player.setAllowFlight(false);
		player.setFlying(false);
		if (couldFly) {
			player.setAllowFlight(couldFly);
			player.setFlying(wasFlying);
		}

		if (cooldownEnabled && player.isOnline()) {
			bPlayer.addCooldown(this);
		}
		super.remove();
	}
	
	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public String getName() {
		return "EarthSurf";
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public String getAuthor() {
		return JedCore.dev;
	}

	@Override
	public String getVersion() {
		return JedCore.version;
	}

	@Override
	public String getDescription() {
		return "* JedCore Addon *\n" + JedCore.plugin.getConfig().getString("Abilities.Earth.EarthSurf.Description");
	}

	@Override
	public void load() {
		return;
	}

	@Override
	public void stop() {
		return;
	}

	@Override
	public boolean isEnabled() {
		return JedCore.plugin.getConfig().getBoolean("Abilities.Earth.EarthSurf.Enabled");
	}
}
