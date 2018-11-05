package com.jedk1.jedcore.ability.waterbending;

import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.configuration.JedCoreConfig;
import com.jedk1.jedcore.util.RegenTempBlock;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.util.DamageHandler;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.Levelled;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class DrainBlast extends WaterAbility implements AddonAbility {

	private Location location;
	private Vector direction;
	private double travelled;

	private double blastRange; // 20
	private double blastDamage; // 1.5
	private double blastSpeed; // 2
	private int holdRange; // 2

	public DrainBlast(Player player, double range, double damage, double speed, int holdrange) {
		super(player);
		this.blastRange = range;
		this.blastDamage = damage;
		this.blastSpeed = speed;
		this.holdRange = holdrange;
		location = player.getEyeLocation().clone().add(player.getEyeLocation().getDirection().multiply(holdRange));
		start();
	}

	@Override
	public void progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		}
		if (travelled >= blastRange) {
			remove();
			return;
		}
		advanceAttack();
		return;
	}

	private void advanceAttack() {
		for (int i = 0; i < blastSpeed; i++) {
			travelled++;
			if (travelled >= blastRange)
				return;

			if (!player.isDead())
				direction = GeneralMethods.getDirection(player.getLocation(), GeneralMethods.getTargetedLocation(player, blastRange, Material.WATER)).normalize();
			location = location.add(direction.clone().multiply(1));
			if (GeneralMethods.isSolid(location.getBlock()) || !isTransparent(location.getBlock())) {
				travelled = blastRange;
				return;
			}

			playWaterbendingSound(location);
			new RegenTempBlock(location.getBlock(), Material.WATER, Material.WATER.createBlockData(bd -> ((Levelled)bd).setLevel(0)), 100L);

			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 2.5)) {
				if (entity instanceof LivingEntity && entity.getEntityId() != player.getEntityId() && !(entity instanceof ArmorStand)) {
					DamageHandler.damageEntity(entity, blastDamage, this);
					travelled = blastRange;
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
		return "Drain";
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
		return "* JedCore Addon *\n" + config.getString("Abilities.Water.Drain.Description");
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
		return config.getBoolean("Abilities.Water.Drain.Enabled");
	}
}
