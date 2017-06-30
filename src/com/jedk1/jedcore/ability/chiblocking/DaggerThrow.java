package com.jedk1.jedcore.ability.chiblocking;

import com.jedk1.jedcore.JCMethods;
import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.util.TempFallingBlock;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.util.DamageHandler;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DaggerThrow extends ChiAbility implements AddonAbility {
	
	private Location location;
	private long time;
	private int shots = 1;

	private long cooldown;
	private boolean limitEnabled;
	private int maxShots;
	private static boolean particles;
	private static double damage;
	private List<Arrow> arrows = new ArrayList<>();

	public DaggerThrow(Player player) {
		super(player);
		if (!bPlayer.canBend(this)) {
			return;
		}
		if (bPlayer.isOnCooldown("DaggerThrowShot")) {
			return;
		}

		if (hasAbility(player, DaggerThrow.class)) {
			DaggerThrow dt = (DaggerThrow) getAbility(player, DaggerThrow.class);
			dt.shootArrow();
			return;
		}

		setFields();
		
		time = System.currentTimeMillis() + 500;
		start();
		shootArrow();
	}
	
	public void setFields() {
		cooldown = JedCore.plugin.getConfig().getLong("Abilities.Chi.DaggerThrow.Cooldown");
		limitEnabled = JedCore.plugin.getConfig().getBoolean("Abilities.Chi.DaggerThrow.MaxDaggers.Enabled");
		maxShots = JedCore.plugin.getConfig().getInt("Abilities.Chi.DaggerThrow.MaxDaggers.Amount");
		particles = JedCore.plugin.getConfig().getBoolean("Abilities.Chi.DaggerThrow.ParticleTrail");
		damage = JedCore.plugin.getConfig().getDouble("Abilities.Chi.DaggerThrow.Damage");
	}

	@Override
	public void progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		}
		if (System.currentTimeMillis() > time) {
			bPlayer.addCooldown(this);
			remove();
			return;
		}
		if (shots > maxShots && limitEnabled) {
			bPlayer.addCooldown(this);
			remove();
			return;
		}
		return;
	}

	private void shootArrow() {
		if (JCMethods.removeItemFromInventory(player, Material.ARROW, 1)) {
			shots++;
			Arrow arrow;
			location = player.getEyeLocation();
			Vector vector = location.toVector().add(location.getDirection().multiply(2.5)).toLocation(location.getWorld()).toVector().subtract(player.getEyeLocation().toVector());
			arrow = player.launchProjectile(Arrow.class);
			arrow.setVelocity(vector);
			arrow.getLocation().setDirection(vector);
			arrow.setKnockbackStrength(0);
			arrow.setBounce(false);
			arrow.setMetadata("daggerthrow", new FixedMetadataValue(JedCore.plugin, "1"));
			if (particles) {
				arrow.setCritical(true);
			}

			arrows.add(arrow);
			time = System.currentTimeMillis() + 500;
			bPlayer.addCooldown("DaggerThrowShot", 100);
		}
	}

	public static void damageEntityFromArrow(Player player, LivingEntity entity, Arrow arrow) {
		if (GeneralMethods.isRegionProtectedFromBuild((Player) arrow.getShooter(), "DaggerThrow", arrow.getLocation())) {
			return;
		}
		arrow.setVelocity(new Vector(0, 0, 0));
		entity.setNoDamageTicks(0);
		double prevHealth = entity.getHealth();
		Player shooter = (Player) arrow.getShooter();
		DamageAbility da = new DamageAbility(shooter);
		DamageHandler.damageEntity(entity, damage, da);
		da.remove();
		if (prevHealth > entity.getHealth()) {
			arrow.remove();
		}
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
	public List<Location> getLocations() {
		return arrows.stream().map(Arrow::getLocation).collect(Collectors.toList());
	}

	@Override
	public void handleCollision(Collision collision) {
		if (collision.isRemovingFirst()) {
			Location location = collision.getLocationFirst();

			Optional<Arrow> collidedObject = arrows.stream().filter(arrow -> arrow.getLocation().equals(location)).findAny();

			if (collidedObject.isPresent()) {
				arrows.remove(collidedObject.get());
				collidedObject.get().remove();
			}
		}
	}

	@Override
	public String getName() {
		return "DaggerThrow";
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
		return "* JedCore Addon *\n" + JedCore.plugin.getConfig().getString("Abilities.Chi.DaggerThrow.Description");
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
		return JedCore.plugin.getConfig().getBoolean("Abilities.Chi.DaggerThrow.Enabled");
	}
	
	public static class DamageAbility extends DaggerThrow {
		
		public DamageAbility(Player player) {
			super(player);
			start();
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
			return "DaggerThrow";
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
		public void progress() {
			remove();
			return;
		}
	}
}
