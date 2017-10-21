package com.jedk1.jedcore.ability.airbending;

import com.jedk1.jedcore.JCMethods;
import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.configuration.JedCoreConfig;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Random;

public class AirBreath extends AirAbility implements AddonAbility {

	private long time;
	private boolean isAvatar;

	private long cooldown;
	private long duration;
	private int particles;

	private boolean coolLava;
	private boolean extinguishFire;
	private boolean extinguishMobs;

	private boolean damageEnabled;
	private double playerDamage;
	private double mobDamage;

	private double knockback;
	private int range;

	private double launch;

	private boolean regenOxygen;

	private boolean avatarAmplify;
	private int avatarRange;
	private double avatarKnockback;

	public AirBreath(Player player) {
		super(player);
		if (!bPlayer.canBend(this)) {
			return;
		}

		setFields();
		time = System.currentTimeMillis();
		isAvatar = bPlayer.isAvatarState();
		if (isAvatar && avatarAmplify) {
			range = avatarRange;
			knockback = avatarKnockback;
		}
		start();
	}

	public void setFields() {
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);

		cooldown = config.getLong("Abilities.Air.AirBreath.Cooldown");
		duration = config.getLong("Abilities.Air.AirBreath.Duration");
		particles = config.getInt("Abilities.Air.AirBreath.Particles");
		coolLava = config.getBoolean("Abilities.Air.AirBreath.AffectBlocks.Lava");
		extinguishFire = config.getBoolean("Abilities.Air.AirBreath.AffectBlocks.Fire");
		extinguishMobs = config.getBoolean("Abilities.Air.AirBreath.ExtinguishEntities");
		damageEnabled = config.getBoolean("Abilities.Air.AirBreath.Damage.Enabled");
		playerDamage = config.getDouble("Abilities.Air.AirBreath.Damage.Player");
		mobDamage = config.getDouble("Abilities.Air.AirBreath.Damage.Mob");
		knockback = config.getDouble("Abilities.Air.AirBreath.Knockback");
		range = config.getInt("Abilities.Air.AirBreath.Range");
		launch = config.getDouble("Abilities.Air.AirBreath.LaunchPower");
		regenOxygen = config.getBoolean("Abilities.Air.AirBreath.RegenTargetOxygen");
		avatarAmplify = config.getBoolean("Abilities.Air.AirBreath.Avatar.Enabled");
		avatarRange = config.getInt("Abilities.Air.AirBreath.Avatar.Range");
		avatarKnockback = config.getDouble("Abilities.Air.AirBreath.Avatar.Knockback");
	}

	@Override
	public void progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		}
		if (!(bPlayer.getBoundAbility() instanceof AirBreath)) {
			bPlayer.addCooldown(this);
			remove();
			return;
		}
		if (!player.isSneaking()) {
			bPlayer.addCooldown(this);
			remove();
			return;
		}
		if (System.currentTimeMillis() < time + duration) {
			playAirbendingSound(player.getLocation());
			createBeam();
		} else {
			bPlayer.addCooldown(this);
			remove();
			return;
		}
		return;
	}

	private boolean isLocationSafe(Location loc) {
		Block block = loc.getBlock();
		if (GeneralMethods.isRegionProtectedFromBuild(player, "AirBreath", loc)) {
			return false;
		}
		if (!isTransparent(block)) {
			return false;
		}
		return true;
	}

	private void createBeam() {
		Location loc = player.getEyeLocation();
		Vector dir = player.getLocation().getDirection();
		double step = 1;
		double size = 0;
		double damageregion = 1.5;

		for (double i = 0; i < range; i += step) {
			loc = loc.add(dir.clone().multiply(step));
			size += 0.005;
			damageregion += 0.01;

			if (!isLocationSafe(loc)) {
				if (!isTransparent(loc.getBlock())) {
					if (player.getLocation().getPitch() > 30) {
						player.setVelocity(player.getLocation().getDirection().multiply(-launch));
					}
				}
				return;
			}

			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(loc, damageregion)) {
				if (entity.getEntityId() != player.getEntityId() && !(entity instanceof ArmorStand)) {
					if (entity instanceof Player && Commands.invincible.contains(((Player) entity).getName()))
						continue;
					if (entity instanceof LivingEntity) {
						if (damageEnabled) {
							if (entity instanceof Player)
								DamageHandler.damageEntity(entity, playerDamage, this);
							else
								DamageHandler.damageEntity(entity, mobDamage, this);
						}

						if (regenOxygen && isWater(entity.getLocation().getBlock())) {
							if (!((LivingEntity) entity).hasPotionEffect(PotionEffectType.WATER_BREATHING))
								((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 100, 2));
						}

						if (extinguishMobs)
							entity.setFireTicks(0);
					}

					dir.multiply(knockback);
					entity.setVelocity(dir);
				}
			}

			if (isWater(loc.getBlock())) {
				ParticleEffect.BUBBLE.display(loc, (float) Math.random(), (float) Math.random(), (float) Math.random(), Float.valueOf((float) size), particles);
			}

			JCMethods.extinguishBlocks(player, "AirBreath", range, 2, extinguishFire, coolLava);

			if (getAirbendingParticles() == ParticleEffect.CLOUD) {
				ParticleEffect.CLOUD.display(loc, (float) Math.random(), (float) Math.random(), (float) Math.random(), Float.valueOf((float) size), particles);
				ParticleEffect.SPELL.display(loc, (float) Math.random(), (float) Math.random(), (float) Math.random(), Float.valueOf((float) size), particles);
			} else {
				getAirbendingParticles().display(loc, (float) Math.random(), (float) Math.random(), (float) Math.random(), Float.valueOf((float) size), particles);
			}
		}
	}

	/*
	 * @Override public void remove() { if (player.isOnline()) {
	 * bPlayer.addCooldown("AirBreath", cooldown); } super.remove(); }
	 */

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public String getName() {
		return "AirBreath";
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
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);
		return "* JedCore Addon *\n" + config.getString("Abilities.Air.AirBreath.Description");
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
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);
		return config.getBoolean("Abilities.Air.AirBreath.Enabled");
	}
}