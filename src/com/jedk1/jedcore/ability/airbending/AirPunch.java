package com.jedk1.jedcore.ability.airbending;

import com.jedk1.jedcore.JedCore;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.util.DamageHandler;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class AirPunch extends AirAbility implements AddonAbility {

	private ConcurrentHashMap<Location, Double> locations = new ConcurrentHashMap<>();

	private long cooldown;
	private long threshold;
	private int maxShots;
	private double range;
	private double damage;

	private int shots;
	private long lastShotTime;

	public AirPunch(Player player) {
		super(player);
		if (!bPlayer.canBend(this)) {
			return;
		}

		if (hasAbility(player, AirPunch.class)) {
			AirPunch ap = (AirPunch) getAbility(player, AirPunch.class);
			ap.createShot();
			return;
		}
		
		setFields();

		start();
		createShot();
	}
	
	public void setFields() {
		cooldown = JedCore.plugin.getConfig().getLong("Abilities.Air.AirPunch.Cooldown");
		threshold = JedCore.plugin.getConfig().getLong("Abilities.Air.AirPunch.Threshold");
		maxShots = JedCore.plugin.getConfig().getInt("Abilities.Air.AirPunch.Shots");
		range = JedCore.plugin.getConfig().getDouble("Abilities.Air.AirPunch.Range");
		damage = JedCore.plugin.getConfig().getDouble("Abilities.Air.AirPunch.Damage");
		shots = maxShots;
	}

	@Override
	public void progress() {
		progressShots();
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		}
		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			prepareRemove();
			return;
		}
		if (shots == 0 || System.currentTimeMillis() > lastShotTime + threshold) {
			prepareRemove();
			return;
		}
		return;
	}

	private void prepareRemove() {
		if (player.isOnline() && !bPlayer.isOnCooldown(this)) {
			bPlayer.addCooldown(this);
		}
		if (locations.isEmpty()) {
			remove();
			return;
		}
	}

	private void createShot() {
		if (shots >= 1) {
			lastShotTime = System.currentTimeMillis();
			shots--;
			locations.put(player.getEyeLocation().add(player.getLocation().getDirection().multiply(1.5).normalize()), 0D);
		}
	}

	private void progressShots() {
		for (Location l : locations.keySet()) {
			Location loc = l.clone();
			double dist = locations.get(l);
			boolean cancel = false;
			for (int i = 0; i < 3; i++) {
				dist++;
				if (cancel || dist >= range) {
					cancel = true;
					break;
				}
				loc = loc.add(loc.getDirection().clone().multiply(1));
				if (GeneralMethods.isSolid(loc.getBlock()) || isWater(loc.getBlock()) || GeneralMethods.isRegionProtectedFromBuild(player, "AirPunch", loc)) {
					cancel = true;
					break;
				}

				getAirbendingParticles().display((float) Math.random() / 5, (float) Math.random() / 5, (float) Math.random() / 5, 0f, 2, loc, 257D);
				playAirbendingSound(loc);

				for (Entity entity : GeneralMethods.getEntitiesAroundPoint(loc, 2.0)) {
					if (entity instanceof LivingEntity && entity.getEntityId() != player.getEntityId() && !(entity instanceof ArmorStand)) {
						DamageHandler.damageEntity(entity, damage, this);
						cancel = true;
						break;
					}
				}
			}

			if (cancel) {
				locations.remove(l);
			} else {
				locations.remove(l);
				locations.put(loc, dist);
			}
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
	public void handleCollision(Collision collision) {
		if (collision.isRemovingFirst()) {
			Location location = collision.getLocationFirst();

			locations.remove(location);
		}
	}

	@Override
	public List<Location> getLocations() {
		return new ArrayList<>(locations.keySet());
	}

	@Override
	public String getName() {
		return "AirPunch";
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
		return "* JedCore Addon *\n" + JedCore.plugin.getConfig().getString("Abilities.Air.AirPunch.Description");
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
		return JedCore.plugin.getConfig().getBoolean("Abilities.Air.AirPunch.Enabled");
	}
}
