package com.jedk1.jedcore.ability.earthbending;

import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.collision.AABB;
import com.jedk1.jedcore.collision.CollisionDetector;
import com.jedk1.jedcore.collision.CollisionUtil;
import com.jedk1.jedcore.configuration.JedCoreConfig;
import com.jedk1.jedcore.util.BlockUtil;
import com.jedk1.jedcore.util.TempFallingBlock;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.util.*;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.util.stream.Collectors.toList;

public class EarthKick extends EarthAbility implements AddonAbility {
	private List<TempFallingBlock> temps = new ArrayList<>();

	private Material material;
	private BlockData materialData;
	private Location location;
	private Random rand = new Random();

	private long cooldown;
	private int earthBlocks;
	private double damage;
	private double entityCollisionRadius;
	private Block block;

	public EarthKick(Player player) {
		super(player);

		if (!bPlayer.canBend(this)) {
			return;
		}

		setFields();
		location = player.getLocation();
		if ((player.getLocation().getPitch() > -5) && prepare()) {
			if(GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation())){
				return;
			}
			launchBlocks();
			start();
		}
	}

	public void setFields() {
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);
		
		cooldown = config.getLong("Abilities.Earth.EarthKick.Cooldown");
		earthBlocks = config.getInt("Abilities.Earth.EarthKick.EarthBlocks");
		damage = config.getDouble("Abilities.Earth.EarthKick.Damage");
		entityCollisionRadius = config.getDouble("Abilities.Earth.EarthKick.EntityCollisionRadius");

		if (entityCollisionRadius < 1.0) {
			entityCollisionRadius = 1.0;
		}
	}

	private boolean prepare() {
		block = player.getTargetBlock(getTransparentMaterialSet(), 2);
		if (!isEarthbendable(player, block)){
			return false;
		}

		if (block != null && !isMetal(block)) {
			material = block.getType();
			materialData = block.getBlockData().clone();
			location.setX(block.getX() + 0.5);
			location.setY(block.getY());
			location.setZ(block.getZ() + 0.5);

			return true;
		}

		return false;
	}

	@Override
	public void progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		}

		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			remove();
			return;
		}

		bPlayer.addCooldown(this);
		track();

		if (temps.isEmpty()) {
			remove();
		}
	}

	private void launchBlocks() {
		if (EarthAbility.getMovedEarth().containsKey(block)){
			block.setType(Material.AIR);
		}
		if (block.getType() != Material.AIR) {
			TempBlock air = new TempBlock(block, Material.AIR);
			air.setRevertTime(5000L);
		}

		location.setPitch(0);
		Vector direction = location.getDirection();
		location.add(direction.clone().multiply(1.0));

		if(!ElementalAbility.isAir(location.getBlock().getType())){
			location.setY(location.getY() + 1.0);
		}

		ParticleEffect.CRIT.display(location, 10, Math.random(), Math.random(), Math.random(), 0.1);

		int yaw = Math.round(location.getYaw());

		playEarthbendingSound(location);

		for (int i = 0; i < earthBlocks; i++) {
			location.setYaw(yaw + (rand.nextInt((20 - -20) + 1) + -20));
			location.setPitch(rand.nextInt(25) - 45);

			Vector v = location.clone().add(0, 0.8, 0).getDirection().normalize();
			Location location1 = location.clone().add(new Vector(v.getX() * 2, v.getY(), v.getZ() * 2));
			Vector dir = location1.setDirection(location.getDirection()).getDirection();

			temps.add(new TempFallingBlock(location, materialData, dir, this));
		}
	}

	public void track() {
		List<TempFallingBlock> destroy = new ArrayList<>();

		for (TempFallingBlock tfb : temps) {
			FallingBlock fb = tfb.getFallingBlock();

			if (fb == null || fb.isDead()) {
				destroy.add(tfb);
				continue;
			}

			for (int i = 0; i < 2; i++) {
				ParticleEffect.BLOCK_CRACK.display(fb.getLocation(), 1, 0.0, 0.0, 0.0, 0.1, materialData);
				ParticleEffect.BLOCK_CRACK.display(fb.getLocation(), 1, 0.0, 0.0, 0.0, 0.2, materialData);
			}

			AABB collider = BlockUtil.getFallingBlockBoundsFull(fb).scale(entityCollisionRadius * 2.0);

			CollisionDetector.checkEntityCollisions(player, collider, (entity) -> {
				DamageHandler.damageEntity(entity, damage, this);
				return false;
			});
		}

		temps.removeAll(destroy);
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
		return temps.stream().map(TempFallingBlock::getLocation).collect(toList());
	}

	@Override
	public double getCollisionRadius() {
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);
		return config.getDouble("Abilities.Earth.EarthKick.AbilityCollisionRadius");
	}

	@Override
	public void handleCollision(Collision collision) {
		CollisionUtil.handleFallingBlockCollisions(collision, temps);
	}

	@Override
	public String getName() {
		return "EarthKick";
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
		return "* JedCore Addon *\n" + config.getString("Abilities.Earth.EarthKick.Description");
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
		return config.getBoolean("Abilities.Earth.EarthKick.Enabled");
	}
}
