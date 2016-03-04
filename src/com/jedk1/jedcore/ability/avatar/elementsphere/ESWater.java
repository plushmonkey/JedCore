package com.jedk1.jedcore.ability.avatar.elementsphere;

import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.util.RegenTempBlock;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AvatarAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.util.DamageHandler;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class ESWater extends AvatarAbility implements AddonAbility {

	private Location location;
	private Vector direction;
	private double travelled;

	private long cooldown;
	private double range;
	private double damage;
	private int speed;

	public ESWater(Player player) {
		super(player);
		if (!hasAbility(player, ElementSphere.class)) {
			return;
		}
		ElementSphere currES = getAbility(player, ElementSphere.class);
		if (currES.getWaterUses() == 0) {
			return;
		}
		if (!bPlayer.canBendIgnoreBindsCooldowns(this) || bPlayer.isOnCooldown("ESWater")) {
			return;
		}
		setFields();
		bPlayer.addCooldown("ESWater", getCooldown());
		currES.setWaterUses(currES.getWaterUses() - 1);
		location = player.getEyeLocation().clone().add(player.getEyeLocation().getDirection().multiply(1));
		start();
	}

	public void setFields() {
		cooldown = JedCore.plugin.getConfig().getLong("Abilities.Avatar.ElementSphere.Water.Cooldown");
		range = JedCore.plugin.getConfig().getDouble("Abilities.Avatar.ElementSphere.Water.Range");
		damage = JedCore.plugin.getConfig().getDouble("Abilities.Avatar.ElementSphere.Water.Damage");
		speed = JedCore.plugin.getConfig().getInt("Abilities.Avatar.ElementSphere.Water.Speed");
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

			if (!player.isDead())
				direction = GeneralMethods.getDirection(player.getLocation(), GeneralMethods.getTargetedLocation(player, range, new Integer[] { 8, 9 })).normalize();
			location = location.add(direction.clone().multiply(1));
			if (GeneralMethods.isSolid(location.getBlock()) || !isTransparent(location.getBlock())) {
				travelled = range;
				return;
			}

			WaterAbility.playWaterbendingSound(location);
			new RegenTempBlock(location.getBlock(), Material.STATIONARY_WATER, (byte) 8, 100L);

			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 2.5)) {
				if (entity instanceof LivingEntity && entity.getEntityId() != player.getEntityId() && !(entity instanceof ArmorStand)) {
					DamageHandler.damageEntity(entity, damage, this);
					travelled = range;
				}
			}
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
		return "ElementSphere Water";
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
		return JedCore.plugin.getConfig().getBoolean("Abilities.Avatar.ElementSphere.Enabled");
	}
}
