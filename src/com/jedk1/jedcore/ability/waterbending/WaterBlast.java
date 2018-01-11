package com.jedk1.jedcore.ability.waterbending;

import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.collision.AABB;
import com.jedk1.jedcore.collision.CollisionDetector;
import com.jedk1.jedcore.util.CollisionInitializer;
import com.jedk1.jedcore.util.RegenTempBlock;
import com.jedk1.jedcore.util.VersionUtil;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.util.DamageHandler;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class WaterBlast extends WaterAbility implements AddonAbility {
	private Location location;
	private Vector direction;
	private Ability ability;
	private double travelled;

	private double range;
	private double damage;
	private double speed;
	private double entityCollisionRadius;
	private double abilityCollisionRadius;

	static {
		CollisionInitializer.abilityMap.put("WaterBlast", "WaterGimbal");
	}

	public WaterBlast(Player player, Location origin, double range, double damage, double speed, double entityCollisionRadius, double abilityCollisionRadius, Ability ability) {
		super(player);

		this.range = range;
		this.damage = damage;
		this.speed = speed;
		this.ability = ability;
		this.location = origin;
		this.entityCollisionRadius = entityCollisionRadius;
		this.abilityCollisionRadius = abilityCollisionRadius;

		start();
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

		if (!advanceAttack()) {
			remove();
		}
	}

	private boolean advanceAttack() {
		int steps = (int)Math.ceil(speed);
		// This is how much the last step should move by.
		double remainder = speed - Math.floor(speed);

		// Move in discrete steps so each block can be checked for collisions.
		for (int i = 0; i < steps; i++) {
			double stepSpeed = 1.0;

			if (remainder > 0 && i == steps - 1) {
				// The last step should only move by the remainder because there are Math.ceil(speed) steps.
				stepSpeed = remainder;
			}

			travelled += stepSpeed;

			if (travelled >= range) {
				return false;
			}

			if (!player.isDead()) {
				Location target = VersionUtil.getTargetedLocation(player, range, Material.WATER, Material.STATIONARY_WATER);
				if (location.distanceSquared(target) <= 1) {
					// Make sure the WaterBlast moves in to the solid block.
					target = target.add(player.getLocation().getDirection());
				}
				direction = GeneralMethods.getDirection(location, target).normalize();
			}

			location = location.add(direction.clone().multiply(stepSpeed));

			if (GeneralMethods.isSolid(location.getBlock())) {
				if (!GeneralMethods.isSolid(location.getBlock().getRelative(BlockFace.UP))) {
					location.add(0, 1, 0);
				} else {
					return false;
				}
			}

			if (!isTransparent(location.getBlock()) || GeneralMethods.isRegionProtectedFromBuild(this, location)) {
				return false;
			}

			playWaterbendingSound(location);

			new RegenTempBlock(location.getBlock(), Material.STATIONARY_WATER, (byte) 8, 250);

			// Only damage entities that are more than 3 blocks away.
			if (travelled >= 3) {
				AABB collider = AABB.BlockBounds.at(location).scale(entityCollisionRadius);

				boolean hit = CollisionDetector.checkEntityCollisions(player, collider, (entity) -> {
					DamageHandler.damageEntity(entity, damage, ability);
					return true;
				});

				if (hit) {
					return false;
				}
			}
		}

		return true;
	}
	
	@Override
	public long getCooldown() {
		return 0;
	}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public double getCollisionRadius() {
		return abilityCollisionRadius;
	}

	@Override
	public String getName() {
		return "WaterBlast";
	}

	@Override
	public boolean isHiddenAbility() {
		return true;
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
		return null;
	}

	@Override
	public void load() {

	}

	@Override
	public void stop() {

	}
	
	@Override
	public boolean isEnabled() {
		return true;
	}
}
