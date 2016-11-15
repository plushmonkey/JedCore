package com.jedk1.jedcore.ability.airbending;

import com.jedk1.jedcore.JedCore;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.util.DamageHandler;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Random;

public class SonicBlast extends AirAbility implements AddonAbility {

	private long time;
	private Location location;
	private Vector direction;
	private boolean isCharged;
	private int travelled;

	Random rand = new Random();

	private double damage;
	private double range;
	private long cooldown;
	private long warmup;
	private int nauseaDur;
	private int blindDur;

	public SonicBlast(Player player) {
		super(player);
		if (hasAbility(player, SonicBlast.class) || bPlayer.isOnCooldown(this)) {
			return;
		}
		setFields();
		start();
	}
	
	public void setFields() {
		damage = JedCore.plugin.getConfig().getDouble("Abilities.Air.SonicBlast.Damage");
		range = JedCore.plugin.getConfig().getDouble("Abilities.Air.SonicBlast.Range");
		cooldown = JedCore.plugin.getConfig().getLong("Abilities.Air.SonicBlast.Cooldown");
		warmup = JedCore.plugin.getConfig().getLong("Abilities.Air.SonicBlast.ChargeTime");
		nauseaDur = JedCore.plugin.getConfig().getInt("Abilities.Air.SonicBlast.Effects.NauseaDuration");
		blindDur = JedCore.plugin.getConfig().getInt("Abilities.Air.SonicBlast.Effects.BlindnessDuration");
		time = System.currentTimeMillis();
	}

	@Override
	public void progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		}
		if (player.isSneaking() && travelled == 0) {
			direction = player.getEyeLocation().getDirection().normalize();
			if (isCharged) {
				playAirbendingParticles(player.getLocation().add(0, 1, 0), 5, (float) Math.random(), (float) Math.random(), (float) Math.random());
			} else if (System.currentTimeMillis() > time + warmup) {
				isCharged = true;
			}
		} else {
			if (isCharged) {
				if (!bPlayer.isOnCooldown(this)) {
					bPlayer.addCooldown(this);
				}
				if (travelled < range && isLocationSafe()) {
					advanceLocation();
				} else {
					remove();
					return;
				}
				return;
			} else {
				remove();
				return;
			}
		}
		return;
	}

	private boolean isLocationSafe() {
		if (location == null) {
			Location origin = player.getEyeLocation().clone();
			location = origin.clone();
		}
		Block block = location.getBlock();
		if (!isTransparent(block)) {
			return false;
		}
		return true;
	}

	private void advanceLocation() {
		travelled++;
		if (location == null) {
			Location origin = player.getEyeLocation().clone();
			location = origin.clone();
		}
		for (int i = 0; i < 5; i++) {
			for (int angle = 0; angle < 360; angle += 20) {
				Location temp = location.clone();
				Vector dir = GeneralMethods.getOrthogonalVector(direction.clone(), angle, 1);
				temp.add(dir);
				playAirbendingParticles(temp, 1, 0, 0, 0);
			}
			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 1.6)) {
				if (entity instanceof LivingEntity && entity.getEntityId() != player.getEntityId()) {
					DamageHandler.damageEntity(entity, damage, this);
					LivingEntity lE = (LivingEntity) entity;
					lE.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, nauseaDur/50, 1));
					lE.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, blindDur/50, 1));
					return;
				}
			}
			location = location.add(direction.clone().multiply(0.2));
		}
		location.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1, 0);
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
		return "SonicBlast";
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
		return "* JedCore Addon *\n" + JedCore.plugin.getConfig().getString("Abilities.Air.SonicBlast.Description");
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
		return JedCore.plugin.getConfig().getBoolean("Abilities.Air.SonicBlast.Enabled");
	}
}
