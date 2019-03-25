package com.jedk1.jedcore.ability.waterbending;

import com.jedk1.jedcore.JCMethods;
import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.configuration.JedCoreConfig;
import com.jedk1.jedcore.util.RegenTempBlock;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.util.ParticleEffect;

import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.util.WaterReturn;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Drain extends WaterAbility implements AddonAbility {
	private List<Location> locations = new ArrayList<>();
	private static final Biome[] INVALID_BIOMES = {
			Biome.DESERT,
			Biome.DESERT_HILLS,
			Biome.NETHER,
			Biome.BADLANDS,
			Biome.BADLANDS_PLATEAU,
			Biome.ERODED_BADLANDS,
			Biome.SAVANNA,
			Biome.SAVANNA_PLATEAU
	};

	private long regenDelay;
	private long duration; // 2000
	private long cooldown; // 2000
	private double absorbSpeed; // 0.1
	private int radius; // 6
	private int chance; // 20
	private int absorbRate; // 6
	private int holdRange; // 2
	private boolean blastsEnabled; // true
	private int maxBlasts;
	private boolean keepSrc; // false
	private boolean useRain;
	private boolean usePlants;

	private double blastRange; // 20
	private double blastDamage; // 1.5
	private double blastSpeed; // 2

	private boolean drainTemps;

	private long time;
	private int absorbed = 0;
	private int charge = 7;
	private boolean noFill;
	private int blasts;
	private boolean hasCharge;
	private Material[] fillables = { Material.GLASS_BOTTLE, Material.BUCKET };

	Random rand = new Random();

	public Drain(Player player) {
		super(player);
		if (!bPlayer.canBend(this) || hasAbility(player, Drain.class)) {
			return;
		}
		setFields();
		this.usePlants = bPlayer.canPlantbend();
		time = System.currentTimeMillis() + duration;
		if (!canFill()) {
			if (!blastsEnabled)
				return;
			noFill = true;
		}
		start();
	}

	public void setFields() {
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);

		regenDelay = config.getLong("Abilities.Water.Drain.RegenDelay");
		duration = config.getLong("Abilities.Water.Drain.Duration");
		cooldown = config.getLong("Abilities.Water.Drain.Cooldown");
		absorbSpeed = config.getDouble("Abilities.Water.Drain.AbsorbSpeed");
		radius = config.getInt("Abilities.Water.Drain.Radius");
		chance = config.getInt("Abilities.Water.Drain.AbsorbChance");
		absorbRate = config.getInt("Abilities.Water.Drain.AbsorbRate");
		holdRange = config.getInt("Abilities.Water.Drain.HoldRange");
		blastsEnabled = config.getBoolean("Abilities.Water.Drain.BlastsEnabled");
		maxBlasts = config.getInt("Abilities.Water.Drain.MaxBlasts");
		keepSrc = config.getBoolean("Abilities.Water.Drain.KeepSource");
		blastRange = config.getDouble("Abilities.Water.Drain.BlastRange");
		blastDamage = config.getDouble("Abilities.Water.Drain.BlastDamage");
		blastSpeed = config.getDouble("Abilities.Water.Drain.BlastSpeed");
		useRain = config.getBoolean("Abilities.Water.Drain.AllowRainSource");
		drainTemps = config.getBoolean("Abilities.Water.Drain.DrainTempBlocks");
	}
	
	public boolean isValidBiome(Biome biome) {
		return !Arrays.asList(INVALID_BIOMES).contains(biome);
	}

	@Override
	public void progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		}
		if (!bPlayer.canBendIgnoreCooldowns(this)) {
			bPlayer.addCooldown(this);
			remove();
			return;
		}
		if (!noFill) {
			if (!player.isSneaking()) {
				bPlayer.addCooldown(this);
				remove();
				return;
			}
			if (!canFill()) {
				bPlayer.addCooldown(this);
				remove();
				return;
			}
			if (System.currentTimeMillis() > time) {
				bPlayer.addCooldown(this);
				remove();
				return;
			}
			if (absorbed >= absorbRate) {
				fill();
				absorbed = 0;
			}
			checkForValidSource();
		} else {
			if (blasts >= maxBlasts) {
				bPlayer.addCooldown(this);
				remove();
				return;
			}
			if (player.isSneaking()) {
				if (charge >= 2) {
					checkForValidSource();
				}
				if (absorbed >= absorbRate) {
					hasCharge = true;
					absorbed = 0;
					if (charge >= 3) {
						charge -= 2;
					}
				}
			} else if (!hasCharge || !keepSrc) {
				bPlayer.addCooldown(this);
				remove();
				return;
			}
			if (hasCharge) {
				displayWaterSource();
			}
		}
		dragWater();
		return;
	}

	public static void fireBlast(Player player) {
		if (hasAbility(player, Drain.class)) {
			((Drain) getAbility(player, Drain.class)).fireBlast();
		}
	}

	private void fireBlast() {
		if (charge <= 1) {
			hasCharge = false;
			charge = 7;
			blasts++;
			new DrainBlast(player, blastRange, blastDamage, blastSpeed, holdRange);
		}
	}

	private void displayWaterSource() {
		Location location = player.getEyeLocation().add(player.getLocation().getDirection().multiply(holdRange));
		if (!GeneralMethods.isSolid(location.getBlock()) || isTransparent(location.getBlock())) {
			Block block = location.getBlock();
			//revert.put(block, 0l);
			//new TempBlock(block, Material.STATIONARY_WATER, (byte) charge);
			new RegenTempBlock(block, Material.WATER, Material.WATER.createBlockData(bd -> ((Levelled)bd).setLevel(charge)), 100l);
		}
	}

	private boolean canFill() {
		for (ItemStack items : player.getInventory()) {
			if (items != null && Arrays.asList(fillables).contains(items.getType())) {
				return true;
			}
		}
		return false;
	}

	private void fill() {
		for (int x = 0; x < absorbed; x++) {
			for (Material fillable : fillables) {
				int slot = player.getInventory().first(fillable);
				if (slot == -1){
					continue;
				}
				if (player.getInventory().getItem(slot).getAmount() > 1) {
					player.getInventory().getItem(slot).setAmount(player.getInventory().getItem(slot).getAmount() - 1);

					ItemStack filled = getFilled(fillable);
					HashMap<Integer, ItemStack> cantfit = player.getInventory().addItem(filled);
					for (int id : cantfit.keySet()) {
						player.getWorld().dropItem(player.getEyeLocation(), cantfit.get(id));
					}
				} else {
					player.getInventory().setItem(slot, getFilled(fillable));
				}
				break;
			}
		}
	}

	private ItemStack getFilled(Material type) {
		ItemStack filled = null;
		if (type == Material.GLASS_BOTTLE) {
			filled = WaterReturn.waterBottleItem();
		} else if (type == Material.BUCKET) {
			filled = new ItemStack(Material.WATER_BUCKET, 1);
		}

		return filled;
	}

	private void checkForValidSource() {
		List<Location> locs = GeneralMethods.getCircle(player.getLocation(), radius, radius, false, true, 0);
		for (int i = 0; i < locs.size(); i++) {
			Block block = locs.get(rand.nextInt(locs.size()-1)).getBlock();
			if (block != null && block.getY() > 2 && block.getY() < 255) {
				if (rand.nextInt(chance) == 0) {
					Location temp = player.getLocation();
					Biome biome = temp.getWorld().getBiome(temp.getBlockX(), temp.getBlockZ());
					if (useRain && player.getWorld().hasStorm() && isValidBiome(biome)) {
						if (player.getLocation().getY() >= player.getWorld().getHighestBlockAt(player.getLocation()).getLocation().getY()) {
							if (block.getLocation().getY() >= player.getWorld().getHighestBlockAt(player.getLocation()).getLocation().getY()) {
								locations.add(block.getLocation().clone().add(.5, .5, .5));
								return;
							}
						}
					}
					if (usePlants && JCMethods.isSmallPlant(block) && !isObstructed(block.getLocation(), player.getEyeLocation())) {
						drainPlant(block);
					} else if(usePlants && ElementalAbility.isPlant(block) && !isObstructed(block.getLocation(), player.getEyeLocation())) {
						locations.add(block.getLocation().clone().add(.5, .5, .5));
						new RegenTempBlock(block, Material.AIR, Material.AIR.createBlockData(), regenDelay);
					} else if (isWater(block)) {
						if (drainTemps || !TempBlock.isTempBlock(block)) {
							drainWater(block);
						}
					}
				}
			}
		}
	}

	private boolean isObstructed(Location location1, Location location2) {
		Vector loc1 = location1.toVector();
		Vector loc2 = location2.toVector();

		Vector direction = loc2.subtract(loc1);
		direction.normalize();

		Location loc;

		double max = location1.distance(location2);

		for (double i = 1; i <= max; i++) {
			loc = location1.clone().add(direction.clone().multiply(i));
			//Material type = loc.getBlock().getType();
			//if (type != Material.AIR && !Arrays.asList(plantIds).contains(type.getId()) && !isWater(loc.getBlock()))
			if (!isTransparent(loc.getBlock()))
				return true;
		}

		return false;
	}

	private void drainPlant(Block block) {
		if (JCMethods.isSmallPlant(block)) {
			if (JCMethods.isSmallPlant(block.getRelative(BlockFace.DOWN))) {
				if (JCMethods.isDoublePlant(block.getType())) {
					block = block.getRelative(BlockFace.DOWN);
					//revert.put(block, System.currentTimeMillis() + regenDelay);
					locations.add(block.getLocation().clone().add(.5, .5, .5));
					//new TempBlock(block, Material.DEAD_BUSH, (byte) 0);
					new RegenTempBlock(block, Material.DEAD_BUSH, Material.DEAD_BUSH.createBlockData(), regenDelay);
					return;
				}
				block = block.getRelative(BlockFace.DOWN);
			}
			//revert.put(block, System.currentTimeMillis() + regenDelay);
			locations.add(block.getLocation().clone().add(.5, .5, .5));
			//new TempBlock(block, Material.DEAD_BUSH, (byte) 0);
			new RegenTempBlock(block, Material.DEAD_BUSH, Material.DEAD_BUSH.createBlockData(), regenDelay);
		}
	}

	private void drainWater(Block block) {
		if (isTransparent(block.getRelative(BlockFace.UP)) && !isWater(block.getRelative(BlockFace.UP))) {
			locations.add(block.getLocation().clone().add(.5, .5, .5));
			if (block.getBlockData() instanceof Waterlogged) {
				new RegenTempBlock(block, block.getBlockData().getMaterial(), block.getBlockData().getMaterial().createBlockData(bd -> ((Waterlogged) bd).setWaterlogged(false)), regenDelay);
			} else {
				new RegenTempBlock(block, Material.WATER, Material.WATER.createBlockData(bd -> ((Levelled) bd).setLevel(1)), regenDelay);
			}
		}
	}

	private void dragWater() {
		List<Integer> toRemove = new ArrayList<Integer>();
		if (!locations.isEmpty()) {
			for (Location l : locations) {
				Location playerLoc = player.getLocation().add(0, 1, 0);
				if (noFill)
					playerLoc = player.getEyeLocation().add(player.getLocation().getDirection().multiply(holdRange)).subtract(0, .8, 0);
				Vector dir = GeneralMethods.getDirection(l, playerLoc);
				l = l.add(dir.multiply(absorbSpeed));
				ParticleEffect.WATER_SPLASH.display(l, 1, 0, 0, 0, 0);
				GeneralMethods.displayColoredParticle("0099FF", l);
				if (l.distance(playerLoc) < 1) {
					toRemove.add(locations.indexOf(l));
					absorbed++;
				}
			}
		}
		if (!toRemove.isEmpty()) {
			for (int i : toRemove) {
				if (i < locations.size())
					locations.remove(i);
			}
			toRemove.clear();
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
	public String getName() {
		return "Drain";
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
		return "* JedCore Addon *\n" + config.getString("Abilities.Water.Drain.Description");
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
		return config.getBoolean("Abilities.Water.Drain.Enabled");
	}
}
