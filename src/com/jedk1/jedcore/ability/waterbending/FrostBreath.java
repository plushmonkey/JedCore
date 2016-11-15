package com.jedk1.jedcore.ability.waterbending;

import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.util.RegenTempBlock;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.IceAbility;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.PhaseChangeFreeze;
import com.projectkorra.projectkorra.waterbending.Torrent;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class FrostBreath extends IceAbility implements AddonAbility {

	private long time;
	Material[] invalidBlocks = { 
			Material.ICE, 
			Material.LAVA, 
			Material.STATIONARY_LAVA, 
			Material.AIR };
	Biome[] invalidBiomes = { 
			Biome.DESERT, 
			Biome.DESERT_HILLS,
			Biome.HELL, 
			Biome.MESA,
			Biome.MESA_CLEAR_ROCK,
			Biome.MESA_ROCK,
			Biome.SAVANNA, 
			Biome.SAVANNA_ROCK
	};
	Random rand = new Random();

	private long cooldown;
	private long duration;
	private int particles;
	private int freezeDuration;
	private int snowDuration;
	private int range;
	private boolean snowEnabled;
	private boolean bendSnow;
	private boolean damageEnabled;
	private double playerDamage;
	private double mobDamage;
	private boolean slowEnabled;
	private long slowDuration;
	private boolean restrictBiomes;

	public FrostBreath(Player player) {
		super(player);
		if (!bPlayer.canBend(this) || !bPlayer.canIcebend()) {
			return;
		}
		setFields();
		Location temp = player.getLocation();
		Biome biome = temp.getWorld().getBiome(temp.getBlockX(), temp.getBlockZ());
		if (restrictBiomes && !isValidBiome(biome)) {
			return;
		}
		time = System.currentTimeMillis();
		start();
	}

	public void setFields() {
		cooldown = JedCore.plugin.getConfig().getLong("Abilities.Water.FrostBreath.Cooldown");
		duration = JedCore.plugin.getConfig().getLong("Abilities.Water.FrostBreath.Duration");
		particles = JedCore.plugin.getConfig().getInt("Abilities.Water.FrostBreath.Particles");
		freezeDuration = JedCore.plugin.getConfig().getInt("Abilities.Water.FrostBreath.FrostDuration");
		snowDuration = JedCore.plugin.getConfig().getInt("Abilities.Water.FrostBreath.SnowDuration");
		range = JedCore.plugin.getConfig().getInt("Abilities.Water.FrostBreath.Range");
		snowEnabled = JedCore.plugin.getConfig().getBoolean("Abilities.Water.FrostBreath.Snow");
		bendSnow = JedCore.plugin.getConfig().getBoolean("Abilities.Water.FrostBreath.BendableSnow");
		damageEnabled = JedCore.plugin.getConfig().getBoolean("Abilities.Water.FrostBreath.Damage.Enabled");
		playerDamage = JedCore.plugin.getConfig().getDouble("Abilities.Water.FrostBreath.Damage.Player");
		mobDamage = JedCore.plugin.getConfig().getDouble("Abilities.Water.FrostBreath.Damage.Mob");
		slowEnabled = JedCore.plugin.getConfig().getBoolean("Abilities.Water.FrostBreath.Slow.Enabled");
		slowDuration = JedCore.plugin.getConfig().getLong("Abilities.Water.FrostBreath.Slow.Duration");
		restrictBiomes = JedCore.plugin.getConfig().getBoolean("Abilities.Water.FrostBreath.RestrictBiomes");
	}

	@Override
	public void progress() {
		if (player == null || !player.isOnline()) {
			remove();
			return;
		}
		if (!bPlayer.canBendIgnoreCooldowns(this)) {
			bPlayer.addCooldown(this);
			remove();
			return;
		}
		if (!player.isSneaking() || player.isDead()) {
			bPlayer.addCooldown(this);
			remove();
			return;
		}
		if (System.currentTimeMillis() < time + duration) {
			createBeam();
		} else {
			bPlayer.addCooldown(this);
			remove();
			return;
		}
		return;
	}

	private boolean isLocationSafe(Location loc) {
		Block block = loc.getBlock();
		if (GeneralMethods.isRegionProtectedFromBuild(player, "FrostBreath", loc)) {
			return false;
		}
		if (!isTransparent(block)) {
			return false;
		}
		return true;
	}
	
	public boolean isValidBiome(Biome biome) {
		return !Arrays.asList(invalidBiomes).contains(biome);
	}

	private void createBeam() {
		Location loc = player.getEyeLocation();
		Vector dir = player.getLocation().getDirection();
		double step = 1;
		double size = 0;
		double offset = 0;
		double damageregion = 1.5;

		for (double i = 0; i < range; i += step) {
			loc = loc.add(dir.clone().multiply(step));
			size += 0.005;
			offset += 0.3;
			damageregion += 0.01;

			if (!isLocationSafe(loc))
				return;

			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(loc, damageregion)) {
				if (entity instanceof LivingEntity && entity.getEntityId() != player.getEntityId()) {

					for (Location l2 : createCage(entity.getLocation())) {
						if (!GeneralMethods.isRegionProtectedFromBuild(player, "FrostBreath", l2) && (!l2.getBlock().getType().isSolid() || l2.getBlock().getType().equals(Material.AIR))) {
							Block block = l2.getBlock();

							RegenTempBlock.revert(block);
							new RegenTempBlock(block, Material.ICE, (byte) 0, freezeDuration);
							Torrent.getFrozenBlocks().put(TempBlock.get(block), player);
						}
					}

					if (slowEnabled) {
						((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) slowDuration / 50, 5));
					}
					if (damageEnabled) {
						if (entity instanceof Player) {
							DamageHandler.damageEntity(entity, playerDamage, this);
						} else {
							DamageHandler.damageEntity(entity, mobDamage, this);
						}
					}
				}
			}

			if (snowEnabled) {
				freezeGround(loc);
			}

			ParticleEffect.SNOW_SHOVEL.display(loc, (float) Math.random(), (float) Math.random(), (float) Math.random(), Float.valueOf((float) size), particles);
			ParticleEffect.MOB_SPELL.display((float) 220, (float) 220, (float) 220, 0.003F, 0, getOffsetLocation(loc, offset), 257D);
			ParticleEffect.MOB_SPELL.display((float) 150, (float) 150, (float) 255, 0.0035F, 0, getOffsetLocation(loc, offset), 257D);
		}
	}

	private Location getOffsetLocation(Location loc, double offset) {
		return loc.clone().add((float) ((Math.random() - 0.5) * offset), (float) ((Math.random() - 0.5) * offset), (float) ((Math.random() - 0.5) * offset));
	}

	private void freezeGround(Location loc) {
		for (Location l : GeneralMethods.getCircle(loc, 2, 2, false, true, 0)) {
			if (!GeneralMethods.isRegionProtectedFromBuild(player, "FrostBreath", l)) {
				Block block = l.getBlock();
				if (isWater(l.getBlock())) {
					PhaseChangeFreeze.freeze(player, block);
				} else if (isTransparent(l.getBlock()) && l.clone().add(0, -1, 0).getBlock().getType().isSolid() && !Arrays.asList(invalidBlocks).contains(l.clone().add(0, -1, 0).getBlock().getType())) {
					new RegenTempBlock(block, Material.SNOW, (byte) 0, snowDuration, !bendSnow);
				}
			}
		}
	}

	private List<Location> createCage(Location centerBlock) {
		List<Location> selectedBlocks = new ArrayList<Location>();

		int bX = centerBlock.getBlockX();
		int bY = centerBlock.getBlockY();
		int bZ = centerBlock.getBlockZ();

		for (int x = bX - 1; x <= bX + 1; x++) {
			for (int y = bY - 1; y <= bY + 1; y++) {
				Location l = new Location(centerBlock.getWorld(), x, y, bZ);
				selectedBlocks.add(l);
			}
		}

		for (int y = bY - 1; y <= bY + 2; y++) {
			Location l = new Location(centerBlock.getWorld(), bX, y, bZ);
			selectedBlocks.add(l);
		}

		for (int z = bZ - 1; z <= bZ + 1; z++) {
			for (int y = bY - 1; y <= bY + 1; y++) {
				Location l = new Location(centerBlock.getWorld(), bX, y, z);
				selectedBlocks.add(l);
			}
		}

		for (int x = bX - 1; x <= bX + 1; x++) {
			for (int z = bZ - 1; z <= bZ + 1; z++) {
				Location l = new Location(centerBlock.getWorld(), x, bY, z);
				selectedBlocks.add(l);
			}
		}

		return selectedBlocks;
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
		return "FrostBreath";
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
		return "* JedCore Addon *\n" + JedCore.plugin.getConfig().getString("Abilities.Water.FrostBreath.Description");
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
		return JedCore.plugin.getConfig().getBoolean("Abilities.Water.FrostBreath.Enabled");
	}
}
