package com.jedk1.jedcore.ability.waterbending;

import com.jedk1.jedcore.JedCore;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.IceAbility;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;

import org.bukkit.Location;
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
		cooldown = JedCore.plugin.getConfig().getLong("Abilities.Water.IceClaws.Cooldown");
		chargeUp = JedCore.plugin.getConfig().getLong("Abilities.Water.IceClaws.ChargeTime");
		slowDur = JedCore.plugin.getConfig().getInt("Abilities.Water.IceClaws.SlowDuration")/50;
		damage = JedCore.plugin.getConfig().getDouble("Abilities.Water.IceClaws.Damage");
		range = JedCore.plugin.getConfig().getDouble("Abilities.Water.IceClaws.Range");
		throwable = JedCore.plugin.getConfig().getBoolean("Abilities.Water.IceClaws.Throwable");
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
			GeneralMethods.displayColoredParticle(head, "66FFFF");
			GeneralMethods.displayColoredParticle(head, "CCFFFF");
			ParticleEffect.SNOW_SHOVEL.display(0f, 0f, 0f, 0f, 1, head, 257D);
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
		GeneralMethods.displayColoredParticle(location, "66FFFF");
		GeneralMethods.displayColoredParticle(location, "CCFFFF");
	}

	private void displayChargeUp() {
		Location location = getRightHandPos().toVector().add(player.getEyeLocation().getDirection().clone().multiply(.75D)).toLocation(player.getWorld());
		ParticleEffect.WATER_SPLASH.display((float) Math.random()/3, (float) Math.random()/3, (float) Math.random()/3, 0.0F, 1, location, 256D);
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
		return "* JedCore Addon *\n" + JedCore.plugin.getConfig().getString("Abilities.Water.IceClaws.Description");
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
		return JedCore.plugin.getConfig().getBoolean("Abilities.Water.IceClaws.Enabled");
	}
}