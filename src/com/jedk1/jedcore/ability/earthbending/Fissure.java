package com.jedk1.jedcore.ability.earthbending;

import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.configuration.JedCoreConfig;
import com.jedk1.jedcore.util.RegenTempBlock;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.LavaAbility;
import com.projectkorra.projectkorra.earthbending.passive.DensityShift;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class Fissure extends LavaAbility implements AddonAbility {

	private int slapRange;
	private int maxWidth;
	private long slapDelay;
	private long duration;
	private long cooldown;

	private Location location;
	private Vector direction;
	private Vector blockdirection;
	private long time;
	private long step;
	private int slap;
	private int width;
	private boolean progressed;
	
	static Random rand = new Random();

	private List<Location> centerSlap = new ArrayList<Location>();
	private List<Block> blocks = new ArrayList<Block>();

	public Fissure(Player player) {
		super(player);
		
		if (!bPlayer.canBend(this) || hasAbility(player, Fissure.class) || !bPlayer.canLavabend()) {
			return;
		}

		setFields();
		time = System.currentTimeMillis();
		step = System.currentTimeMillis() + slapDelay;
		location = player.getLocation().clone();
		location.setPitch(0);
		direction = location.getDirection();
		blockdirection = this.direction.clone().setX(Math.round(this.direction.getX()));
		blockdirection = blockdirection.setZ(Math.round(direction.getZ()));
		if (prepareLine()) {
			start();
			bPlayer.addCooldown(this);
		}
	}
	
	public void setFields() {
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);
		
		slapRange = config.getInt("Abilities.Earth.Fissure.SlapRange");
		maxWidth = config.getInt("Abilities.Earth.Fissure.MaxWidth");
		slapDelay = config.getInt("Abilities.Earth.Fissure.SlapDelay");
		duration = config.getInt("Abilities.Earth.Fissure.Duration");
		cooldown = config.getInt("Abilities.Earth.Fissure.Cooldown");
	}

	@Override
	public void progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		}
		if (System.currentTimeMillis() > step && slap <= centerSlap.size()) {
			time = System.currentTimeMillis();
			step = System.currentTimeMillis() + slapDelay;
			slapCenter();
			slap++;
		}
		if (System.currentTimeMillis() > time + duration) {
			remove();
			return;
		}
	}

	private boolean prepareLine() {
		direction = player.getEyeLocation().getDirection().setY(0).normalize();
		blockdirection = this.direction.clone().setX(Math.round(this.direction.getX()));
		blockdirection = blockdirection.setZ(Math.round(direction.getZ()));
		Location origin = player.getLocation().add(0, -1, 0).add(blockdirection.multiply(2));
		if (isEarthbendable(player, origin.getBlock())) {
			BlockIterator bi = new BlockIterator(player.getWorld(), origin.toVector(), direction, 0, slapRange);

			while (bi.hasNext()) {
				Block b = bi.next();

				if (b != null && b.getY() > 1 && b.getY() < 255) {
					while (!isEarthbendable(player, b)) {
						b = b.getRelative(BlockFace.DOWN);
						if (b == null || b.getY() < 1 || b.getY() > 255) {
							break;
						}
						if (isEarthbendable(player, b)) {
							break;
						}
					}

					while (!isTransparent(b.getRelative(BlockFace.UP))) {
						b = b.getRelative(BlockFace.UP);
						if (b == null || b.getY() < 1 || b.getY() > 255) {
							break;
						}
						if (isEarthbendable(player, b.getRelative(BlockFace.UP))) {
							break;
						}
					}

					if (isEarthbendable(player, b)) {
						centerSlap.add(b.getLocation());
					} else {
						break;
					}
				}
			}
			return true;
		}
		return false;
	}

	private void slapCenter() {
		for (Location location : centerSlap) {
			if (centerSlap.indexOf(location) <= slap) {
				addTempBlock(location.getBlock(), Material.LAVA);
			}
		}
		if (slap >= centerSlap.size()) {
			progressed = true;
		}
	}
	
	public static void performAction(Player player) {
		if (hasAbility(player, Fissure.class)) {
			((Fissure) getAbility(player, Fissure.class)).performAction();
		}
	}
	
	private void performAction() {
		if (width < maxWidth) {
			expandFissure();
		} else if (width >= maxWidth && blocks.contains(player.getTargetBlock((HashSet<Material>) null, (int) 10))) {
			forceRevert();
		}
	}

	private void expandFissure() {
		if (progressed && width <= maxWidth) {
			width++;
			for (Location location : centerSlap) {
				Block left = location.getBlock().getRelative(getLeftBlockFace(GeneralMethods.getCardinalDirection(blockdirection)), width);
				expand(left);

				Block right = location.getBlock().getRelative(getLeftBlockFace(GeneralMethods.getCardinalDirection(blockdirection)).getOppositeFace(), width);
				expand(right);
			}
		}
		Collections.reverse(blocks);
	}

	private void expand(Block block) {
		if (block != null && block.getY() > 1 && block.getY() < 255) {
			while (!isEarthbendable(player, block)) {
				block = block.getRelative(BlockFace.DOWN);
				if (block == null || block.getY() < 1 || block.getY() > 255) {
					break;
				}
				if (isEarthbendable(player, block)) {
					break;
				}
			}

			while (!isTransparent(player, block.getRelative(BlockFace.UP))) {
				block = block.getRelative(BlockFace.UP);
				if (block == null || block.getY() < 1 || block.getY() > 255) {
					break;
				}
				if (isEarthbendable(player, block.getRelative(BlockFace.UP))) {
					break;
				}
			}

			if (isEarthbendable(player, block)) {
				addTempBlock(block, Material.LAVA);
			} else {
				return;
			}
		}
	}

	private void addTempBlock(Block block, Material material) {
		ParticleEffect.LAVA.display(block.getLocation(), 0, 0, 0, 0, 1);
		playEarthbendingSound(block.getLocation());
		if (DensityShift.isPassiveSand(block)) {
            DensityShift.revertSand(block);
		}
		new TempBlock(block, material, material.createBlockData());
		blocks.add(block);
	}

	public BlockFace getLeftBlockFace(BlockFace forward) {
		switch (forward) {
		case NORTH:
			return BlockFace.WEST;
		case SOUTH:
			return BlockFace.EAST;
		case WEST:
			return BlockFace.SOUTH;
		case EAST:
			return BlockFace.NORTH;
		case NORTH_WEST:
			return BlockFace.SOUTH_WEST;
		case NORTH_EAST:
			return BlockFace.NORTH_WEST;
		case SOUTH_WEST:
			return BlockFace.SOUTH_EAST;
		case SOUTH_EAST:
			return BlockFace.NORTH_EAST;

		default:
			return BlockFace.NORTH;
		}
	}
	
	private void forceRevert() {
		for (Block block : blocks) {
			new RegenTempBlock(block, Material.STONE, Material.STONE.createBlockData(), 500 + (long) rand.nextInt((int) 1000));
		}
		coolLava();
	}
	
	private void coolLava() {
		for (Block block : blocks) {
			new RegenTempBlock(block, Material.STONE, Material.STONE.createBlockData(), 500 + (long) rand.nextInt((int) 1000));
		}
		blocks.clear();
	}

	@Override
	public void remove() {
		coolLava();
		super.remove();
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
		return "Fissure";
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
		return "* JedCore Addon *\n" + config.getString("Abilities.Earth.Fissure.Description");
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
		return config.getBoolean("Abilities.Earth.Fissure.Enabled");
	}
}