package com.jedk1.jedcore.ability.waterbending;

import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.configuration.JedCoreConfig;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.IceAbility;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class IceClaws extends IceAbility implements AddonAbility {
	
	private long cooldown;
	private long chargeUp;
	private int slowDur;
	private double damage;
	private double range;
	private boolean throwable;

	private Location head;
	private Location origin;
	private boolean launched;
	private long time;

	public IceClaws(Player player) {
		super(player);
		if (!bPlayer.canBend(this) || !bPlayer.canIcebend()) {
			return;
		}

		if (hasAbility(player, IceClaws.class)) {
			IceClaws ic = ((IceClaws) getAbility(player, IceClaws.class));
			if (!ic.throwable) {
				ic.remove();
			}
			return;
		}

		setFields();
		time = System.currentTimeMillis();
		start();
	}
	
	public void setFields() {
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);

		cooldown = config.getLong("Abilities.Water.IceClaws.Cooldown");
		chargeUp = config.getLong("Abilities.Water.IceClaws.ChargeTime");
		slowDur = config.getInt("Abilities.Water.IceClaws.SlowDuration")/50;
		damage = config.getDouble("Abilities.Water.IceClaws.Damage");
		range = config.getDouble("Abilities.Water.IceClaws.Range");
		throwable = config.getBoolean("Abilities.Water.IceClaws.Throwable");
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
		if (System.currentTimeMillis() > time + chargeUp) {
			if (!launched && throwable) {
				displayClaws();
			} else {
				if (!shoot()) {
					remove();
					return;
				}
			}
		} else if (player.isSneaking()) {
			displayChargeUp();
		} else {
			remove();
			return;
		}
		return;
	}

	public boolean shoot() {
		for (double i = 0; i < 1; i+=.5) {
			head.add(origin.clone().getDirection().multiply(.5));
			if (origin.distance(head) >= range) return false;
			if (!isTransparent(head.getBlock())) return false;
			GeneralMethods.displayColoredParticle("66FFFF", head);
			GeneralMethods.displayColoredParticle("CCFFFF", head);
			ParticleEffect.SNOW_SHOVEL.display(head, 1, 0, 0, 0, 0);
			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(head, 1.5)) {
				if (entity instanceof LivingEntity && entity.getEntityId() != player.getEntityId() && !(entity instanceof ArmorStand)) {
					freezeEntity((LivingEntity) entity);
					return false;
				}
			}
		}
		return true;

	}

	public static void throwClaws(Player player) {
		if (hasAbility(player, IceClaws.class)) {
			IceClaws ic = ((IceClaws) getAbility(player, IceClaws.class));
			if (!ic.launched && player.isSneaking()) {
				ic.launched = true;
				ic.origin = ic.player.getEyeLocation();
				ic.head = ic.origin.clone();
			}
		}
	}

	public Location getRightHandPos(){
		return GeneralMethods.getRightSide(player.getLocation(), .55).add(0, 1.2, 0);
	}

	private void displayClaws(){
		Location location = getRightHandPos().toVector().add(player.getEyeLocation().getDirection().clone().multiply(.75D)).toLocation(player.getWorld());
		GeneralMethods.displayColoredParticle("66FFFF", location);
		GeneralMethods.displayColoredParticle("CCFFFF", location);
	}

	private void displayChargeUp() {
		Location location = getRightHandPos().toVector().add(player.getEyeLocation().getDirection().clone().multiply(.75D)).toLocation(player.getWorld());
		ParticleEffect.WATER_SPLASH.display(location, 1, Math.random()/3, Math.random()/3, Math.random()/3, 0.0);
	}

	public static boolean freezeEntity(Player player, LivingEntity entity) {
		if (hasAbility(player, IceClaws.class)) {
			((IceClaws) getAbility(player, IceClaws.class)).freezeEntity(entity);
			return true;
		}
		return false;
	}

	private void freezeEntity(LivingEntity entity) {
		if (entity.hasPotionEffect(PotionEffectType.SPEED)) {
			entity.removePotionEffect(PotionEffectType.SPEED);
			entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, slowDur, 3));
		} else {
			entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, slowDur, 3));
		}
		bPlayer.addCooldown(this);
		remove();
		DamageHandler.damageEntity(entity, damage, this);
		return;
	}
	
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
		return "IceClaws";
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
		return "* JedCore Addon *\n" + config.getString("Abilities.Water.IceClaws.Description");
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
		return config.getBoolean("Abilities.Water.IceClaws.Enabled");
	}
}