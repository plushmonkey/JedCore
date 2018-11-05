package com.jedk1.jedcore.ability.avatar.elementsphere;

import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.configuration.JedCoreConfig;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AvatarAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.firebending.BlazeArc;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class ESFire extends AvatarAbility implements AddonAbility {

	private Location location;
	private Vector direction;
	private double travelled;

	private long cooldown;
	private double range;
	private double damage;
	private long burnTime;
	private int speed;
	private boolean controllable;

	public ESFire(Player player) {
		super(player);
		if (!hasAbility(player, ElementSphere.class)) {
			return;
		}
		ElementSphere currES = getAbility(player, ElementSphere.class);
		if (currES.getFireUses() == 0) {
			return;
		}
		if (bPlayer.isOnCooldown("ESFire")) {
			return;
		}
		setFields();
		bPlayer.addCooldown("ESFire", getCooldown());
		currES.setFireUses(currES.getFireUses() - 1);
		location = player.getEyeLocation().clone().add(player.getEyeLocation().getDirection().multiply(1));
		direction = location.getDirection().clone();
		start();
	}
	
	public void setFields() {
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);
		
		cooldown = config.getLong("Abilities.Avatar.ElementSphere.Fire.Cooldown");
		range = config.getDouble("Abilities.Avatar.ElementSphere.Fire.Range");
		damage = config.getDouble("Abilities.Avatar.ElementSphere.Fire.Damage");
		burnTime = config.getLong("Abilities.Avatar.ElementSphere.Fire.BurnDuration");
		speed = config.getInt("Abilities.Avatar.ElementSphere.Fire.Speed");
		controllable = config.getBoolean("Abilities.Avatar.ElementSphere.Fire.Controllable");
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
	}

	private void advanceAttack() {
		for (int i = 0; i < speed; i++) {
			travelled++;
			if (travelled >= range)
				return;

			if (!player.isDead() && controllable)
				direction = GeneralMethods.getDirection(player.getLocation(), GeneralMethods.getTargetedLocation(player, range, Material.WATER)).normalize();

			location = location.add(direction.clone().multiply(1));
			if (GeneralMethods.isSolid(location.getBlock()) || isWater(location.getBlock())) {
				travelled = range;
				return;
			}

			ParticleEffect.SMOKE_LARGE.display(location, 2, Math.random(), Math.random(), Math.random(), 0.01);
			ParticleEffect.FLAME.display(location, 5, Math.random(), Math.random(), Math.random(), 0.02);
			FireAbility.playFirebendingSound(location);

			placeFire();

			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 2.5)) {
				if (entity instanceof LivingEntity && entity.getEntityId() != player.getEntityId() && !(entity instanceof ArmorStand)) {
					DamageHandler.damageEntity(entity, damage, this);
					entity.setFireTicks((int) Math.round(burnTime / 50));
					travelled = range;
				}
			}
		}
	}

	private void placeFire() {
		if (GeneralMethods.isSolid(location.getBlock().getRelative(BlockFace.DOWN))) {
			location.getBlock().setType(Material.FIRE);
			new BlazeArc(player, location, direction, 2);
		}
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
		return "ElementSphere Fire";
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
		return;
	}

	@Override
	public void stop() {
		return;
	}
	
	@Override
	public boolean isEnabled() {
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);
		return config.getBoolean("Abilities.Avatar.ElementSphere.Enabled");
	}
}
