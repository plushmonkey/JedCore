package com.jedk1.jedcore.ability.airbending;

import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.collision.CollisionDetector;
import com.jedk1.jedcore.collision.Sphere;
import com.jedk1.jedcore.configuration.JedCoreConfig;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.util.DamageHandler;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class AirBlade extends AirAbility implements AddonAbility {

	private Location location;
	private Vector direction;
	private double travelled;
	private double growth = 1;
	private long cooldown;
	private double range;
	private double damage;
	private double entityCollisionRadius;

	public AirBlade(Player player) {
		super(player);
		if (!bPlayer.canBend(this)) {
			return;
		}
		
		setFields();
		
		this.location = player.getEyeLocation().clone();
		this.direction = player.getEyeLocation().getDirection().clone();
		bPlayer.addCooldown(this);
		start();
	}

	public void setFields() {
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);

		cooldown = config.getLong("Abilities.Air.AirBlade.Cooldown");
		range = config.getDouble("Abilities.Air.AirBlade.Range");
		damage = config.getDouble("Abilities.Air.AirBlade.Damage");
		entityCollisionRadius = config.getDouble("Abilities.Air.AirBlade.EntityCollisionRadius");
	}
	
	@Override
	public void progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		}

		if (travelled >= range) {
			remove();
			return;
		}

		progressBlade();
	}

	private void progressBlade() {
		for (int j = 0; j < 2; j++) {
			location = location.add(direction.multiply(1));
			playAirbendingSound(location);
			travelled++;
			growth += 0.125;
			if (travelled >= range) {
				remove();
				return;
			}

			if (!isTransparent(location.getBlock())) {
				remove();
				return;
			}

			if (GeneralMethods.isRegionProtectedFromBuild(player, "AirBlade", player.getLocation())) {
				remove();
				return;
			}

			double pitch = -location.getPitch();
			Location lastLoc = location.clone();
			for (double i = -90 + pitch; i <= 90 + pitch; i += 8) {
				Location tempLoc = location.clone();
				tempLoc.setPitch(0);
				Vector tempDir = tempLoc.getDirection().clone();
				tempDir.setY(0);
				Vector newDir = tempDir.clone().multiply(growth * Math.cos(Math.toRadians(i)));
				tempLoc.add(newDir);
				tempLoc.setY(tempLoc.getY() + (growth * Math.sin(Math.toRadians(i))));
				playAirbendingParticles(tempLoc, 1, (float) Math.random() / 2, (float) Math.random() / 2, (float) Math.random() / 2);

				if (j == 0) {
					// Only check collisions for each block.
					if (!lastLoc.getBlock().getLocation().equals(tempLoc.getBlock().getLocation())) {
						lastLoc = tempLoc;

						boolean hit = CollisionDetector.checkEntityCollisions(player, new Sphere(tempLoc.toVector(), entityCollisionRadius), (entity) -> {
							DamageHandler.damageEntity(entity, damage, this);
							remove();
							return true;
						});

						if (hit) {
							remove();
							return;
						}
					}
				}
			}
		}
	}

	public Player getPlayer() {
		return player;
	}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public List<Location> getLocations() {
		List<Location> locations = new ArrayList<>();

		double pitch = -location.getPitch();
		for (double i = -90 + pitch; i <= 90 + pitch; i += 8) {
			Location tempLoc = location.clone();
			tempLoc.setPitch(0);
			Vector tempDir = tempLoc.getDirection().clone();
			tempDir.setY(0);
			Vector newDir = tempDir.clone().multiply(growth * Math.cos(Math.toRadians(i)));
			tempLoc.add(newDir);
			tempLoc.setY(tempLoc.getY() + (growth * Math.sin(Math.toRadians(i))));

			locations.add(tempLoc);
		}

		return locations;
	}

	@Override
	public double getCollisionRadius() {
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);
		return config.getDouble("Abilities.Air.AirBlade.AbilityCollisionRadius");
	}

	public long getCooldown() {
		return cooldown;
	}
	
	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

	@Override
	public String getName() {
		return "AirBlade";
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public boolean isSneakAbility() {
		return false;
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
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);
		return "* JedCore Addon *\n" + config.getString("Abilities.Air.AirBlade.Description");
	}
	
	@Override
	public void load() {

	}

	@Override
	public void stop() {

	}
	
	@Override
	public boolean isEnabled() {
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);
		return config.getBoolean("Abilities.Air.AirBlade.Enabled");
	}
}
