package com.jedk1.jedcore.ability.waterbending;

import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.configuration.JedCoreConfig;
import com.jedk1.jedcore.util.ThrownEntityTracker;
import com.jedk1.jedcore.util.VersionUtil;
import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.BloodAbility;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.object.HorizontalVelocityTracker;
import com.projectkorra.projectkorra.util.DamageHandler;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class Bloodbending extends BloodAbility implements AddonAbility {

	private boolean nightOnly;
	private boolean fullMoonOnly;
	private boolean undeadMobs;
	private boolean bloodbendingThroughBlocks;
	private boolean requireBound;
	private int distance;
	private long holdtime;
	private long cooldown;
	
	private long time;
	public LivingEntity victim;
	private BendingPlayer victimBPlayer;
	private boolean grabbed;
	
	public Bloodbending(Player player) {
		super(player);
		if (!isEligible(player, true)) {
			return;
		}
		setFields();
		time = System.currentTimeMillis() + holdtime;
		if (grab()) {
			start();
		}
	}
	
	public void setFields() {
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);

		nightOnly = config.getBoolean("Abilities.Water.Bloodbending.NightOnly");
		fullMoonOnly = config.getBoolean("Abilities.Water.Bloodbending.FullMoonOnly");
		undeadMobs = config.getBoolean("Abilities.Water.Bloodbending.UndeadMobs");
		bloodbendingThroughBlocks = config.getBoolean("Abilities.Water.Bloodbending.IgnoreWalls");
		requireBound = config.getBoolean("Abilities.Water.Bloodbending.RequireBound");
		distance = config.getInt("Abilities.Water.Bloodbending.Distance");
		holdtime = config.getLong("Abilities.Water.Bloodbending.HoldTime");
		cooldown = config.getLong("Abilities.Water.Bloodbending.Cooldown");
	}

	public boolean isEligible(Player player, boolean hasAbility) {
		if (!bPlayer.canBend(this) || !bPlayer.canBloodbend() || (hasAbility && hasAbility(player, Bloodbending.class))) {
			return false;
		}
		if (nightOnly && !isNight(player.getWorld()) && !bPlayer.canBloodbendAtAnytime()) {
			return false;
		}
		return !fullMoonOnly || isFullMoon(player.getWorld()) || bPlayer.canBloodbendAtAnytime();
	}

	public static void launch(Player player) {
		if (hasAbility(player, Bloodbending.class)) {
			((Bloodbending) getAbility(player, Bloodbending.class)).launch();
		}
	}

	private void launch() {
		Vector direction = GeneralMethods.getDirection(player.getEyeLocation(), VersionUtil.getTargetedLocation(player, 20));
		direction = direction.normalize();
		direction.multiply(3);
		victim.setVelocity(direction);
		new HorizontalVelocityTracker(victim, player, 200L, this);
		new ThrownEntityTracker(this, victim, player, 200L);
		remove();
	}

	private boolean grab() {
		List<Entity> entities = new ArrayList<Entity>();
		for (int i = 0; i < distance; i++) {
			Location location = null;
			if (bloodbendingThroughBlocks) {
				location = player.getTargetBlock((HashSet<Material>) null, i).getLocation();
			} else {
				location = VersionUtil.getTargetedLocationTransparent(player, i);
			}
			entities = GeneralMethods.getEntitiesAroundPoint(location, 1.7);
			if (entities.contains(player)) {
				entities.remove(player);
			}
			if (entities != null && !entities.isEmpty() && !entities.contains(player)) {
				break;
			}
		}
		if (entities == null || entities.isEmpty()) {
			return false;
		}
		Entity e = entities.get(0);
		if (e == null) {
			return false;
		}
		if (!(e instanceof LivingEntity)) {
			return false;
		}
		if (!undeadMobs	&& com.projectkorra.projectkorra.waterbending.blood.Bloodbending.isUndead(e)) {
			return false;
		}
		if ((e instanceof Player) && !canBeBloodbent((Player) e)) {
			return false;
		}
		if (GeneralMethods.isRegionProtectedFromBuild(player, "Bloodbending", e.getLocation())) {
			return false;
		}
		for (Bloodbending bb : getAbilities(Bloodbending.class)) {
			if (bb.victim.getEntityId() == e.getEntityId()) {
				return false;
			}
		}

		victim = (LivingEntity) e;
		DamageHandler.damageEntity(victim, 0, this);
		HorizontalVelocityTracker.remove(victim);
		if (victim instanceof Creature) {
			((Creature) victim).setTarget(null);
		}
		if ((e instanceof Player) && BendingPlayer.getBendingPlayer((Player) e) != null) {
			victimBPlayer = BendingPlayer.getBendingPlayer((Player) e);
		}
		return true;
	}
	
	private boolean canBeBloodbent(Player player) {
		if (Commands.invincible.contains(player.getName())) {
			return false;
		}
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (requireBound) {
			if (bPlayer.getAbilities().containsValue("Bloodbending")) {
				return false;
			}
			if (bPlayer.getAbilities().containsValue("BloodPuppet")) {
				return false;
			}
		} else {
			if (bPlayer.canBind(getAbility("Bloodbending")) && bPlayer.canBloodbend()) {
				if ((!isDay(player.getWorld()) || bPlayer.canBloodbendAtAnytime())) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public void progress() {
		if (!isEligible(player, false)) {
			remove();
			return;
		}
		if (!grabbed) {
			if (victim instanceof Player && victimBPlayer != null) {
				victimBPlayer.blockChi();
				grabbed = true;
			}
		}

		if (!player.isSneaking()) {
			remove();
			return;
		}
		if (!player.isOnline() || player.isDead()) {
			remove();
			return;
		}
		if (System.currentTimeMillis() > time) {
			remove();
			return;
		}
		if ((victim instanceof Player) && !((Player) victim).isOnline()
				|| victim.isDead()) {
			remove();
			return;
		}
		Location oldLocation = victim.getLocation();
		Location loc = VersionUtil.getTargetedLocation(player, (int) player.getLocation().distance(oldLocation));
		double distance = loc.distance(oldLocation);
		Vector v = GeneralMethods.getDirection(oldLocation, VersionUtil.getTargetedLocation(player, 10));
		if (distance > 1.2D) {
			victim.setVelocity(v.normalize().multiply(0.8D));
		} else {
			victim.setVelocity(new Vector(0, 0, 0));
		}
		victim.setFallDistance(0.0F);
		if (victim instanceof Creature) {
			((Creature) victim).setTarget(null);
		}
		AirAbility.breakBreathbendingHold(victim);
		return;
	}

	@Override
	public void remove() {
		if (player.isOnline()) {
			bPlayer.addCooldown(this);
		}
		if (victim instanceof Player && victimBPlayer != null) {
			victimBPlayer.unblockChi();
		}
		super.remove();
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
		return "Bloodbending";
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
		return "* JedCore Addon *\n" + config.getString("Abilities.Water.Bloodbending.Description");
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
		return config.getBoolean("Abilities.Water.Bloodbending.Enabled");
	}
}
