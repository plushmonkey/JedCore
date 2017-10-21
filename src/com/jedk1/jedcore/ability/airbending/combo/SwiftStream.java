package com.jedk1.jedcore.ability.airbending.combo;

import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.configuration.JedCoreConfig;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.FlightAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.object.HorizontalVelocityTracker;
import com.projectkorra.projectkorra.util.ClickType;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class SwiftStream extends FlightAbility implements AddonAbility, ComboAbility {
	
	public long cooldown;
	public double dragFactor;
	public long duration;

	private List<LivingEntity> affectedEntities = new ArrayList<LivingEntity>();

	private long startTime;

	public SwiftStream(Player player) {
		super(player);
		if (!bPlayer.canBendIgnoreBinds(this) || !bPlayer.canUseFlight()) {
			return;
		}

		setFields();
		startTime = System.currentTimeMillis();
		launch();
		start();
		bPlayer.addCooldown(this);
	}
	
	public void setFields() {
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);

		cooldown = config.getLong("Abilities.Air.AirCombo.SwiftStream.Cooldown");
		dragFactor = config.getDouble("Abilities.Air.AirCombo.SwiftStream.DragFactor");
		duration = config.getLong("Abilities.Air.AirCombo.SwiftStream.Duration");
	}

	public void launch() {
		Vector v = player.getEyeLocation().getDirection().normalize();

		v = v.multiply(5);
		v.add(new Vector(0, 0.2, 0));

		GeneralMethods.setVelocity(player, v);
	}

	public void affectNearby() {
		for (Entity e : GeneralMethods.getEntitiesAroundPoint(player.getLocation(), 2.5)) {
			if (e instanceof LivingEntity && !affectedEntities.contains(e) && e.getEntityId() != player.getEntityId()) {
				Vector v = player.getVelocity().clone();

				v = v.multiply(dragFactor);

				v = v.setY(player.getVelocity().getY());

				v = v.add(new Vector(0, 0.15, 0));

				GeneralMethods.setVelocity(e, v);
				affectedEntities.add((LivingEntity) e);
				new HorizontalVelocityTracker(e, player, 200, this);
			}
		}
	}

	@Override
	public void progress() {
		if (!player.isOnline() || player.isDead()) {
			remove();
			return;
		}

		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			remove();
			return;
		}

		if (System.currentTimeMillis() > startTime + duration) {
			remove();
			return;
		}
		
		playAirbendingParticles(player.getLocation(), 4);
		affectNearby();
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
		return "SwiftStream";
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
		return true;
	}

	@Override
	public Object createNewComboInstance(Player player) {
		return new SwiftStream(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combination = new ArrayList<>();
		combination.add(new AbilityInformation("Flight", ClickType.SHIFT_UP));
		combination.add(new AbilityInformation("Flight", ClickType.LEFT_CLICK));
		combination.add(new AbilityInformation("Flight", ClickType.LEFT_CLICK));
		return combination;
	}

	@Override
	public String getInstructions() {
		return "Flight (Start Flying) " + "> Flight (Release Shift) > Flight (Left Click) " + "> Flight (Left Click)";
	}

	@Override
	public String getDescription() {
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);
	   return "* JedCore Addon *\n" + config.getString("Abilities.Air.AirCombo.SwiftStream.Description");
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
	public void load() {
	}

	@Override
	public void stop() {
	}
	
	@Override
	public boolean isEnabled() {
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);
		return config.getBoolean("Abilities.Air.AirCombo.SwiftStream.Enabled");
	}
}