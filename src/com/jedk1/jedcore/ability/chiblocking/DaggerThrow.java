package com.jedk1.jedcore.ability.chiblocking;

import com.jedk1.jedcore.JCMethods;
import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.configuration.JedCoreConfig;
import com.jedk1.jedcore.util.AbilitySelector;
import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.util.DamageHandler;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
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
	private static List<AbilityInteraction> interactions = new ArrayList<>();
	private static boolean particles;
	private static double damage;

	private Location location;
	private long time;
	private int shots = 1;
	private long cooldown;
	private boolean limitEnabled;
	private int maxShots;
	private List<Arrow> arrows = new ArrayList<>();

	public DaggerThrow(Player player) {
		super(player);

		if (this instanceof DamageAbility) {
			return;
		}

		if (!bPlayer.canBend(this)) {
			return;
		}

		if (bPlayer.isOnCooldown("DaggerThrowShot")) {
			return;
		}

		if (hasAbility(player, DaggerThrow.class)) {
			DaggerThrow dt = getAbility(player, DaggerThrow.class);
			dt.shootArrow();
			return;
		}

		setFields();
		
		time = System.currentTimeMillis() + 500;
		start();
		shootArrow();
	}
	
	public void setFields() {
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);
		
		cooldown = config.getLong("Abilities.Chi.DaggerThrow.Cooldown");
		limitEnabled = config.getBoolean("Abilities.Chi.DaggerThrow.MaxDaggers.Enabled");
		maxShots = config.getInt("Abilities.Chi.DaggerThrow.MaxDaggers.Amount");
		particles = config.getBoolean("Abilities.Chi.DaggerThrow.ParticleTrail");
		damage = config.getDouble("Abilities.Chi.DaggerThrow.Damage");

		loadInteractions();
	}

	private void loadInteractions() {
		interactions.clear();

		String path = "Abilities.Chi.DaggerThrow.Interactions";

		ConfigurationSection config = JedCoreConfig.getConfig(this.player);
		ConfigurationSection section = config.getConfigurationSection(path);
		for (String abilityName : section.getKeys(false)) {
			interactions.add(new AbilityInteraction(abilityName));
		}
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
		}
	}

	private void shootArrow() {
		if (JCMethods.removeItemFromInventory(player, Material.ARROW, 1)) {
			shots++;
			location = player.getEyeLocation();

			Vector vector = location.toVector().
					add(location.getDirection().multiply(2.5)).
					toLocation(location.getWorld()).toVector().
					subtract(player.getEyeLocation().toVector());

			Arrow arrow = player.launchProjectile(Arrow.class);
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

		if (!(entity instanceof Player)) {
			return;
		}

		Player target = (Player)entity;
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(target);

		for (AbilityInteraction interaction : interactions) {
			if (!interaction.enabled) continue;

			CoreAbility abilityDefinition = AbilitySelector.getAbility(interaction.name);
			if (abilityDefinition == null) continue;

			CoreAbility ability = CoreAbility.getAbility(target, abilityDefinition.getClass());
			if (ability == null) continue;

			ability.remove();
			bPlayer.addCooldown(ability, interaction.cooldown);
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
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);
		return "* JedCore Addon *\n" + config.getString("Abilities.Chi.DaggerThrow.Description");
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
		return config.getBoolean("Abilities.Chi.DaggerThrow.Enabled");
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
		}
	}

	private class AbilityInteraction {
		public boolean enabled;
		public long cooldown;
		public String name;

		public AbilityInteraction(String abilityName) {
			this.name = abilityName;
			loadConfig();
		}

		public void loadConfig() {
			ConfigurationSection config = JedCoreConfig.getConfig(player);
			this.enabled = config.getBoolean("Abilities.Chi.DaggerThrow.Interactions." + name + ".Enabled", true);
			this.cooldown = config.getLong("Abilities.Chi.DaggerThrow.Interactions." + name + ".Cooldown", 1000);
		}
	}
}
