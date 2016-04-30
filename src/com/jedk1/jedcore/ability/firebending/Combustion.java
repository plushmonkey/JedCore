package com.jedk1.jedcore.ability.firebending;

import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.util.RegenTempBlock;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.CombustionAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Random;

public class Combustion extends CombustionAbility implements AddonAbility {

	private long time;
	private boolean charged;
	private int currPoint;
	private Location location;
	private Vector direction;
	private int ticks;
	private boolean start;
	private boolean hasCollided;
	private boolean setCooldown;
	private boolean hasBeenHit;
	private double playerHealth;
	private boolean hasClicked;
	Random rand = new Random();

	Material[] blocks = { Material.AIR, Material.BEDROCK, Material.CHEST, Material.TRAPPED_CHEST, Material.OBSIDIAN, Material.PORTAL, Material.ENDER_PORTAL, Material.ENDER_PORTAL_FRAME, Material.FIRE, Material.WALL_SIGN, Material.SIGN_POST, Material.WATER, Material.STATIONARY_WATER, Material.LAVA, Material.STATIONARY_LAVA, Material.BANNER, Material.WALL_BANNER, Material.DROPPER, Material.FURNACE, Material.DISPENSER, Material.HOPPER, Material.BEACON, Material.BARRIER, Material.MOB_SPAWNER };

	private double damage;
	private int fireTick;
	private static int misfireModifier;
	private long warmup;
	private long cooldown;
	private long regenTime;
	private static int power;
	private static int range;
	private boolean damageBlocks;
	private boolean regenBlocks;
	private boolean instantExplodeIfHit;

	public Combustion(Player player) {
		super(player);
		if (!bPlayer.canBend(this) || !bPlayer.canCombustionbend() || hasAbility(player, Combustion.class)) {
			return;
		}
		setFields();
		this.time = System.currentTimeMillis();
		this.direction = player.getEyeLocation().getDirection().normalize();
		this.charged = false;
		this.hasCollided = false;
		this.setCooldown = false;
		this.hasBeenHit = false;
		this.start = false;
		this.hasClicked = false;

		Damageable dPlayer = player;
		this.playerHealth = dPlayer.getHealth();

		start();
	}

	public void setFields() {
		damage = JedCore.plugin.getConfig().getDouble("Abilities.Fire.Combustion.Damage");
		fireTick = JedCore.plugin.getConfig().getInt("Abilities.Fire.Combustion.FireTick");
		misfireModifier = JedCore.plugin.getConfig().getInt("Abilities.Fire.Combustion.misfireModifier");
		warmup = JedCore.plugin.getConfig().getLong("Abilities.Fire.Combustion.Warmup");
		cooldown = JedCore.plugin.getConfig().getLong("Abilities.Fire.Combustion.Cooldown");
		regenTime = JedCore.plugin.getConfig().getLong("Abilities.Fire.Combustion.RegenTime");
		power = JedCore.plugin.getConfig().getInt("Abilities.Fire.Combustion.Power");
		range = JedCore.plugin.getConfig().getInt("Abilities.Fire.Combustion.Range");
		damageBlocks = JedCore.plugin.getConfig().getBoolean("Abilities.Fire.Combustion.DamageBlocks");
		regenBlocks = JedCore.plugin.getConfig().getBoolean("Abilities.Fire.Combustion.RegenBlocks");
		instantExplodeIfHit = JedCore.plugin.getConfig().getBoolean("Abilities.Fire.Combustion.InstantExplodeIfHit");
	}

	@Override
	public void progress() {
		if (player == null || player.isDead() || !player.isOnline()) {
			remove();
			return;
		}
		Damageable dPlayer = player;
		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			remove();
			return;
		}
		if (player.isSneaking() && !start) {
			if (!bPlayer.canBendIgnoreBinds(this)) {
				remove();
				return;
			}
			playParticleRing(60, 1.75F, 2);
			if (dPlayer.getHealth() < playerHealth) {
				this.hasBeenHit = true;
			}
		}
		if (charged && !hasCollided) {
			ParticleEffect.LARGE_SMOKE.display(this.player.getLocation(), (float) Math.random(), (float) Math.random(), (float) Math.random(), 0.1F, 1);
		}
		if (!player.isSneaking() || start || (instantExplodeIfHit && hasBeenHit)) {
			start = true;
			if (charged) {
				direction = this.player.getEyeLocation().getDirection().normalize();
				ticks += 1;
				if ((ticks <= range) && !hasCollided)
					advanceLocation();
				if (ticks == range && !hasCollided) {
					bPlayer.addCooldown(this);
					remove();
					return;
				}
				if (hasCollided) {
					if (!setCooldown) {
						bPlayer.addCooldown(this);
						setCooldown = true;
					}
					if (System.currentTimeMillis() > time + regenTime) {
						remove();
					}
					return;
				}
				return;
			}
			remove();
			return;
		}

		if (charged || (System.currentTimeMillis() <= time + warmup))
			return;
		charged = true;
	}

	public void setClicked(Boolean b) {
		hasClicked = b.booleanValue();
	}

	private void advanceLocation() {
		if (location == null) {
			Location origin = player.getEyeLocation().clone();
			origin.setY(origin.getY() - 0.8D);
			location = origin.clone();
		}
		int r = (int) Math.sqrt(range);
		for (int i = 0; i < r; ++i) {
			ParticleEffect.FLAME.display(location, 0.0F, 0.0F, 0.0F, 0.03F, 1);
			ParticleEffect.LARGE_SMOKE.display(location, 0.0F, 0.0F, 0.0F, 0.06F, 1);
			ParticleEffect.FIREWORKS_SPARK.display(location, 0.0F, 0.0F, 0.0F, 0.06F, 1);
			location.getWorld().playSound(location, Sound.FIREWORK_BLAST, 1.0F, 0.01F);

			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 1.6D)) {
				if ((entity instanceof LivingEntity) && (entity.getEntityId() != player.getEntityId())) {
					hasCollided = true;
					location = entity.getLocation();
					createExplosion(location, power, damage);
					AirAbility.removeAirSpouts(location, power, player);
					WaterAbility.removeWaterSpouts(location, power, player);
					return;
				}
			}
			if ((!isTransparent(location.getBlock()) || isWater(location.getBlock())) && !GeneralMethods.isRegionProtectedFromBuild(player, "Combustion", location)) {
				hasCollided = true;
				createExplosion(location, power, damage);
				AirAbility.removeAirSpouts(location, power, player);
				WaterAbility.removeWaterSpouts(location, power, player);
				return;
			}
			if (AirAbility.isWithinAirShield(location) || FireAbility.isWithinFireShield(location)) {
				hasCollided = true;
				createExplosion(location, power, damage);
				AirAbility.removeAirSpouts(location, power, player);
				WaterAbility.removeWaterSpouts(location, power, player);
				return;
			}
			if (hasBeenHit) {
				hasCollided = true;
				location = player.getLocation();
				createExplosion(location, power + misfireModifier, damage + misfireModifier);
				AirAbility.removeAirSpouts(location, power, player);
				WaterAbility.removeWaterSpouts(location, power, player);
				return;
			}
			if (hasClicked) {
				hasCollided = true;
				createExplosion(location, power, damage);
				AirAbility.removeAirSpouts(location, power, player);
				WaterAbility.removeWaterSpouts(location, power, player);
				return;
			}
			location = location.add(direction.clone().multiply(0.2D));
		}
	}

	public void explosion(Location loc) {
		ParticleEffect.FLAME.display((float) Math.random(), (float) Math.random(), (float) Math.random(), 0.5f, 20, loc, 257D);
		ParticleEffect.LARGE_SMOKE.display((float) Math.random(), (float) Math.random(), (float) Math.random(), 0.5f, 20, loc, 257D);
		ParticleEffect.FIREWORKS_SPARK.display((float) Math.random(), (float) Math.random(), (float) Math.random(), 0.5f, 20, loc, 257D);
		ParticleEffect.LARGE_SMOKE.display((float) Math.random(), (float) Math.random(), (float) Math.random(), 0.5f, 20, loc, 257D);
		ParticleEffect.EXPLOSION_HUGE.display((float) Math.random(), (float) Math.random(), (float) Math.random(), 0.5f, 5, loc, 257D);
		loc.getWorld().playSound(loc, Sound.EXPLODE, 1f, 1f);
	}

	private void playParticleRing(int points, float size, int speed) {
		for (int i = 0; i < speed; ++i) {
			currPoint += 360 / points;
			if (currPoint > 360) {
				currPoint = 0;
			}
			double angle = currPoint * 3.141592653589793D / 180.0D;
			double x = size * Math.cos(angle);
			double z = size * Math.sin(angle);
			Location loc = player.getLocation().add(x, 1.0D, z);
			ParticleEffect.FLAME.display(loc, 0.0F, 0.0F, 0.0F, 0.01F, 3);
			ParticleEffect.SMOKE.display(loc, 0.0F, 0.0F, 0.0F, 0.01F, 4);
		}
	}

	private void createExplosion(Location loc, int size, double damage) {
		time = System.currentTimeMillis();
		explosion(loc);
		if (damageBlocks) {
			loc.getWorld().createExplosion(loc, 0.0F);

			if (regenBlocks) {
				for (Location l : GeneralMethods.getCircle(loc, size, size, false, true, 0)) {
					if (TempBlock.isTempBlock(l.getBlock())) {
						TempBlock.revertBlock(l.getBlock(), Material.AIR);
						TempBlock.removeBlock(l.getBlock());
					}
					if (!isTransparent(l.getBlock()) && (!(Arrays.asList(blocks).contains(l.getBlock().getType()))) && (!(GeneralMethods.isRegionProtectedFromBuild(player, "Combustion", l)))) {
						new RegenTempBlock(l.getBlock(), Material.AIR, (byte) 0, regenTime, false);
						placeRandomBlock(l);
						placeRandomFire(l);
					}
				}

			} else {
				for (Location l : GeneralMethods.getCircle(loc, size, size, false, true, 0)) {
					if (!isTransparent(l.getBlock()) && (!(Arrays.asList(blocks).contains(l.getBlock().getType()))) && (!(GeneralMethods.isRegionProtectedFromBuild(player, "Combustion", l)))) {
						Block newBlock = l.getWorld().getBlockAt(l);
						newBlock.setType(Material.AIR);
						placeRandomBlock(l);
						placeRandomFire(l);
					}
				}
			}
		}
		for (Entity e : GeneralMethods.getEntitiesAroundPoint(loc, size)) {
			if (e instanceof LivingEntity) {
				DamageHandler.damageEntity(e, damage, this);
				e.setFireTicks(fireTick);
			}
		}
	}

	private void placeRandomFire(Location l) {
		int chance = rand.nextInt(3);
		if ((!(l.getWorld().getBlockAt(l.getBlockX(), l.getBlockY() - 1, l.getBlockZ()).getType().isSolid())) || (chance != 0))
			return;
		l.getBlock().setType(Material.FIRE);
	}

	private void placeRandomBlock(Location l) {
		int chance = rand.nextInt(3);
		if (!(l.getWorld().getBlockAt(l.getBlockX(), l.getBlockY() - 1, l.getBlockZ()).getType().isSolid()))
			return;
		Material block = l.getWorld().getBlockAt(l.getBlockX(), l.getBlockY() - 1, l.getBlockZ()).getType();
		if (chance == 0)
			l.getBlock().setType(block);
	}
	
	public static void combust(Player player) {
		if(hasAbility(player, Combustion.class)) {
			Combustion c = (Combustion) getAbility(player, Combustion.class);
			c.setClicked(true);
			return;
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
		return "Combustion";
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
		return "* JedCore Addon *\n" + JedCore.plugin.getConfig().getString("Abilities.Fire.Combustion.Description");
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
		return JedCore.plugin.getConfig().getBoolean("Abilities.Fire.Combustion.Enabled");
	}
}
