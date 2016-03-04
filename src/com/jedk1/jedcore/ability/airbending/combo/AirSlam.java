package com.jedk1.jedcore.ability.airbending.combo;

import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.util.ThrownEntityTracker;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.object.HorizontalVelocityTracker;
import com.projectkorra.projectkorra.util.ClickType;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class AirSlam extends AirAbility implements AddonAbility, ComboAbility {

	private long cooldown;
	private double power;
	private int range;

	private LivingEntity target;
	private long time;

	public AirSlam(Player player) {
		super(player);
		
		if (!bPlayer.canBendIgnoreBinds(this)) {
			return;
		}
		
		setFields();
		Entity target = GeneralMethods.getTargetedEntity(player, range, new ArrayList<Entity>());
		if (target != null && target instanceof LivingEntity) {
			if (GeneralMethods.isRegionProtectedFromBuild(this, target.getLocation())) {
				return;
			}
			this.target = (LivingEntity) target;
			target.setVelocity(new Vector(0, 2, 0));
		} else {
			return;
		}
		time = System.currentTimeMillis();
		bPlayer.addCooldown(this);
		start();
	}
	
	public void setFields() {
		cooldown = JedCore.plugin.getConfig().getLong("Abilities.Air.AirCombo.AirSlam.Cooldown");
		power = JedCore.plugin.getConfig().getDouble("Abilities.Air.AirCombo.AirSlam.Power");
		range = JedCore.plugin.getConfig().getInt("Abilities.Air.AirCombo.AirSlam.Range");
	}

	@Override
	public void progress() {
		if (player == null || player.isDead() || !player.isOnline()) {
			remove();
			return;
		}
		if (System.currentTimeMillis() > time + 50) {
			Vector dir = player.getLocation().getDirection();
			GeneralMethods.setVelocity(target, new Vector(dir.getX(), 0.05, dir.getZ()).multiply(power));
			new HorizontalVelocityTracker(target, player, 0l, this);
			new ThrownEntityTracker(this, target, player, 0L);
			target.setFallDistance(0);
		}
		if (System.currentTimeMillis() > time + 400) {
			remove();
			return;
		}
		playAirbendingParticles(target.getLocation(), 10);
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
		return "AirSlam";
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
	public Object createNewComboInstance(Player player) {
		return new AirSlam(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combination = new ArrayList<>();
		combination.add(new AbilityInformation("AirSwipe", ClickType.SHIFT_DOWN));
		combination.add(new AbilityInformation("AirBlast", ClickType.SHIFT_UP));
		combination.add(new AbilityInformation("AirBlast", ClickType.SHIFT_DOWN));
		return combination;
	}

	@Override
	public String getInstructions() {
		return "AirSwipe (Hold Shift) > AirBlast (Release Shift) > AirBlast (Hold Shift)";
	}

	@Override
	public String getDescription() {
	   return "* JedCore Addon *\n" + JedCore.plugin.getConfig().getString("Abilities.Air.AirCombo.AirSlam.Description");
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
		return JedCore.plugin.getConfig().getBoolean("Abilities.Air.AirCombo.AirSlam.Enabled");
	}
}