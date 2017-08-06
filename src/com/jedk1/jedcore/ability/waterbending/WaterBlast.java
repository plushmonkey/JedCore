package com.jedk1.jedcore.ability.waterbending;

import com.jedk1.jedcore.JedCore;
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
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class WaterBlast extends WaterAbility implements AddonAbility {

	private Location location;
	private Vector direction;
	private double travelled;
	private Ability ability;

	private double range;
	private double damage;
	private int speed;

	public WaterBlast(Player player, Location origin, double range, double damage, int speed, Ability ability) {
		super(player);
		this.range = range;
		this.damage = damage;
		this.speed = speed;
		this.ability = ability;
		this.location = origin;
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
		advanceAttack();
		return;
	}

	private void advanceAttack() {
		for (int i = 0; i < speed; i++) {
			travelled++;
			if (travelled >= range)
				return;

			if (!player.isDead())
				direction = GeneralMethods.getDirection(location, VersionUtil.getTargetedLocation(player, range, Material.WATER, Material.STATIONARY_WATER)).normalize();
			location = location.add(direction.clone().multiply(1));

			if (GeneralMethods.isSolid(location.getBlock())) {
				if (!GeneralMethods.isSolid(location.getBlock().getRelative(BlockFace.UP))) {
					location.add(0, 1, 0);
				} else {
					travelled = range;
					return;
				}
			}

			if (!isTransparent(location.getBlock()) || GeneralMethods.isRegionProtectedFromBuild(player, "WaterManipulation", location)) {
				travelled = range;
				return;
			}

			playWaterbendingSound(location);
			//new TempBlock(location.getBlock(), Material.STATIONARY_WATER, (byte) 8);
			//revert.put(location.getBlock(), System.currentTimeMillis() + 250L);
			new RegenTempBlock(location.getBlock(), Material.STATIONARY_WATER, (byte) 8, 250l);

			if (travelled >= 3) {
				for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 2)) {
					if (entity instanceof LivingEntity && entity.getEntityId() != player.getEntityId() && !(entity instanceof ArmorStand)) {
						DamageHandler.damageEntity(entity, damage, ability);
						travelled = range;
					}
				}
			}
		}
	}
	
	@Override
	public long getCooldown() {
		return 0;
	}

	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public String getName() {
		return "WaterBlast";
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
		return;
	}

	@Override
	public void stop() {
		return;
	}
	
	@Override
	public boolean isEnabled() {
		return JedCore.plugin.getConfig().getBoolean("Abilities.Water.WaterBlast.Enabled");
	}
}
