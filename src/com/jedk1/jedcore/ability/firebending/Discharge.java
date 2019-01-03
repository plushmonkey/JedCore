package com.jedk1.jedcore.ability.firebending;

import com.jedk1.jedcore.JCMethods;
import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.collision.CollisionDetector;
import com.jedk1.jedcore.collision.Sphere;
import com.jedk1.jedcore.configuration.JedCoreConfig;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.LightningAbility;
import com.projectkorra.projectkorra.util.DamageHandler;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Discharge extends LightningAbility implements AddonAbility {
	private HashMap<Integer, Location> branches = new HashMap<>();
	
	private long time;
	private Location location;
	private Vector direction;
	private boolean hit;
	private int spaces;
	private double branchSpace;
	private Random rand = new Random();

	private double damage;
	private long cooldown, avatarcooldown;
	private long duration;
	private boolean slotSwapping;
	private double entityCollisionRadius;

	public Discharge(Player player) {
		super(player);

		if (!bPlayer.canBend(this) || hasAbility(player, Discharge.class) || !bPlayer.canLightningbend()) {
			return;
		}

		setFields();
		
		direction = player.getEyeLocation().getDirection().normalize();
		
		if (bPlayer.isAvatarState() || JCMethods.isSozinsComet(player.getWorld())) {
			this.cooldown = avatarcooldown;
		}

		bPlayer.addCooldown(this);
		start();
	}

	public void setFields() {
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);

		damage = config.getDouble("Abilities.Fire.Discharge.Damage");
		cooldown = config.getLong("Abilities.Fire.Discharge.Cooldown");
		avatarcooldown = config.getLong("Abilities.Fire.Discharge.AvatarCooldown");
		duration = config.getLong("Abilities.Fire.Discharge.Duration");
		slotSwapping = config.getBoolean("Abilities.Fire.Discharge.SlotSwapping");
		entityCollisionRadius = config.getDouble("Abilities.Fire.Discharge.EntityCollisionRadius");
		
		branchSpace = 0.2;
		time = System.currentTimeMillis();
	}
	
	@Override
	public void progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		}

		if (!canBend()) {
			remove();
			return;
		}

		if (System.currentTimeMillis() < (time + duration) && !hit) {
			advanceLocation();
		} else {
			remove();
		}
	}

	private boolean canBend() {
		if (!slotSwapping) {
			if (!bPlayer.canBendIgnoreCooldowns(this)) {
				return false;
			}
		} else {
			if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
				return false;
			}
		}

		return true;
	}

	private void advanceLocation() {
		if (location == null) {
			Location origin = player.getEyeLocation().clone();
			location = origin.clone();
			branches.put(branches.size()+1, location);
		}

		spaces++;
		if (spaces % 3 == 0) {
			Location prevBranch = branches.get(1);
			branches.put(branches.size()+1, prevBranch);
		}
		
		List<Integer> cleanup = new ArrayList<>();
		
		for (int i : branches.keySet()) {
			Location origin = branches.get(i);

			if (origin != null) {
				Location l = origin.clone();

				if (!isTransparent(l.getBlock())) {
					cleanup.add(i);
					continue;
				}

				l.add(createBranch(l.getX()), createBranch(l.getY()), createBranch(l.getZ()));
				branchSpace += 0.001;

				for (int j = 0; j < 5; j++) {
					playLightningbendingParticle(l.clone(), 0f, 0f, 0f);

					if (rand.nextInt(3) == 0) {
						player.getWorld().playSound(l, Sound.ENTITY_CREEPER_PRIMED, 1, 0);
					}

					Vector vec = l.toVector();

					hit = CollisionDetector.checkEntityCollisions(player, new Sphere(l.toVector(), entityCollisionRadius), (entity) -> {
						Vector knockbackVector = entity.getLocation().toVector().subtract(vec).normalize().multiply(0.8);
						entity.setVelocity(knockbackVector);

						DamageHandler.damageEntity(entity, damage, this);

						for (int k = 0; k < 5; k++) {
							playLightningbendingParticle(entity.getLocation(), (float) Math.random(), (float) Math.random(), (float) Math.random());
						}

						entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 1, 0);
						player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 1, 0);

						return true;
					});

					l = l.add(direction.clone().multiply(0.2));
				}

				branches.put(i, l);
			}
		}
		
		for (int i : cleanup) {
			branches.remove(i);
		}

		cleanup.clear();
	}

	private double createBranch(double start) {
		int i = rand.nextInt(3);

		switch (i) {
			case 0:
				return branchSpace;
			case 1:
				return 0.0;
			case 2:
				return -branchSpace;
			default:
				return 0.0;
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
	public List<Location> getLocations() {
		return new ArrayList<>(branches.values());
	}

	@Override
	public double getCollisionRadius() {
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);
		return config.getDouble("Abilities.Fire.Discharge.AbilityCollisionRadius");
	}

	@Override
	public String getName() {
		return "Discharge";
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
		return "* JedCore Addon *\n" + config.getString("Abilities.Fire.Discharge.Description");
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
		return config.getBoolean("Abilities.Fire.Discharge.Enabled");
	}
}
