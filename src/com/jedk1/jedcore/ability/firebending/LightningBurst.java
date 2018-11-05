package com.jedk1.jedcore.ability.firebending;

import com.jedk1.jedcore.JCMethods;
import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.configuration.JedCoreConfig;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.LightningAbility;
import com.projectkorra.projectkorra.util.DamageHandler;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class LightningBurst extends LightningAbility implements AddonAbility {

	private static ConcurrentHashMap<Integer, Bolt> bolts = new ConcurrentHashMap<Integer, Bolt>();

	Random rand = new Random();
	private long cooldown;
	private long chargeup;
	private double damage;
	private double radius;

	private long time;
	private boolean charged;
	private int id;
	private static int ID = Integer.MIN_VALUE;

	public LightningBurst(Player player) {
		super(player);
		if (!bPlayer.canBend(this) || hasAbility(player, LightningBurst.class)) {
			return;
		}
		
		setFields();
		if (bPlayer.isAvatarState() || JCMethods.isSozinsComet(player.getWorld())) {
			chargeup = 0;
			cooldown = 0;
			
		}
		time = System.currentTimeMillis();
		start();
	}
	
	public void setFields() {
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);

		cooldown = config.getLong("Abilities.Fire.LightningBurst.Cooldown");
		chargeup = config.getLong("Abilities.Fire.LightningBurst.ChargeUp");
		damage = config.getDouble("Abilities.Fire.LightningBurst.Damage");
		radius = config.getDouble("Abilities.Fire.LightningBurst.Radius");
	}

	private void spawnBolt(Player player, Location location, double max, double gap, int arc, boolean dodamage){
		id = ID;
		bolts.put(id, new Bolt(this, location, id, max, gap, arc, dodamage));
		if (ID == Integer.MAX_VALUE)
			ID = Integer.MIN_VALUE;
		ID++;
	}

	public class Bolt {

		private LightningBurst ability;
		private Location location;
		private float initYaw;
		private float initPitch;
		private double step;
		private double max;
		private double gap;
		private int id;
		private int arc;
		private boolean dodamage;

		public Bolt(LightningBurst ability, Location location, int id, double max, double gap, int arc, boolean dodamage) {
			this.ability = ability;
			this.location = location;
			this.id = id;
			this.max = max;
			this.arc = arc;
			this.gap = gap;
			this.dodamage = dodamage;
			initYaw = location.getYaw();
			initPitch = location.getPitch();
		}

		private void progress() {
			if (this.step >= max) {
				bolts.remove(id);
				return;
			}
			if (GeneralMethods.isRegionProtectedFromBuild(player, "LightningBurst", location) || !isTransparent(location.getBlock())) {
				bolts.remove(id);
				return;
			}
			double step = 0.2;
			for(double i = 0; i < gap; i+= step){
				this.step += step;
				location = location.add(location.getDirection().clone().multiply(step));

				playLightningbendingParticle(location, 0f, 0f, 0f);
			}
			switch (rand.nextInt(3)) {
			case 0:
				location.setYaw(initYaw - arc);
				break;
			case 1:
				location.setYaw(initYaw + arc);
				break;
			default:
				location.setYaw(initYaw);
				break;
			}
			switch (rand.nextInt(3)) {
			case 0:
				location.setPitch(initPitch - arc);
				break;
			case 1:
				location.setPitch(initPitch + arc);
				break;
			default:
				location.setPitch(initPitch);
				break;
			}

			if(rand.nextInt(3) == 0) {
				location.getWorld().playSound(location, Sound.ENTITY_CREEPER_PRIMED, 1, 0);
			}

			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 2)) {
				if (entity instanceof LivingEntity && entity.getEntityId() != player.getEntityId() && dodamage) {
					DamageHandler.damageEntity(entity, damage, ability);
				}
			}
		}
	}

	@Override
	public void progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		}
		if (!bPlayer.canBendIgnoreCooldowns(this)) {
			remove();
			return;
		}
		if (GeneralMethods.isRegionProtectedFromBuild(player, "LightningBurst", player.getLocation())) {
			remove();
			return;
		}
		if (!player.isSneaking()) {
			if (!isCharging()) {
				Location fake = player.getLocation().add(0, -2, 0);
				fake.setPitch(0);
				for(int i = -180; i < 180; i += 55){
					fake.setYaw(i);
					for(double j = -180; j <= 180; j += 55){
						Location temp = fake.clone();
						Vector dir = fake.getDirection().clone().multiply(2 * Math.cos(Math.toRadians(j)));
						temp.add(dir);
						temp.setY(temp.getY() + 2 + (2 * Math.sin(Math.toRadians(j))));
						dir = GeneralMethods.getDirection(player.getLocation().add(0, 0, 0), temp);
						spawnBolt(player, player.getLocation().clone().add(0, 1, 0).setDirection(dir), radius, 1, 20, true);
					}
				}
				bPlayer.addCooldown(this);
				remove();
				return;
			} else {
				remove();
				return;
			}
		} else if (System.currentTimeMillis() > time + chargeup){
			setCharging(false);
			displayCharging();
		}
		return;
	}

	private void displayCharging() {
		Location fake = player.getLocation().add(0, 0, 0);
		fake.setPitch(0);
		for(int i = -180; i < 180; i += 55){
			fake.setYaw(i);
			for(double j = -180; j <= 180; j += 55){
				if (rand.nextInt(100) == 0) {
					Location temp = fake.clone();
					Vector dir = fake.getDirection().clone().multiply(1.2 * Math.cos(Math.toRadians(j)));
					temp.add(dir);
					temp.setY(temp.getY() + 1.2 + (1.2 * Math.sin(Math.toRadians(j))));
					dir = GeneralMethods.getDirection(temp, player.getLocation().add(0, 1, 0));
					spawnBolt(player, temp.setDirection(dir), 1, 0.2, 20, false);
				}
			}
		}
	}

	public static void progressAll() {
		for (int id : bolts.keySet()) {
			bolts.get(id).progress();
		}
	}
	
	public boolean isCharging() {
		return !charged;
	}
	
	public void setCharging(boolean charging) {
		this.charged = !charging;
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
		return "LightningBurst";
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
		return "* JedCore Addon *\n" + config.getString("Abilities.Fire.LightningBurst.Description");
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
		return config.getBoolean("Abilities.Fire.LightningBurst.Enabled");
	}
}