package com.jedk1.jedcore.ability.earthbending;

import com.jedk1.jedcore.JedCore;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.earthbending.Collapse;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class EarthPillar extends EarthAbility implements AddonAbility {

	private static ConcurrentHashMap<Block, EarthPillar> affectedblocks = new ConcurrentHashMap<Block, EarthPillar>();
	private static ConcurrentHashMap<EarthPillar, List<Block>> affected = new ConcurrentHashMap<EarthPillar, List<Block>>();

	private Block block;
	private BlockFace face;
	private int height;
	private int range;
	private int step;

	private List<Block> blocks = new ArrayList<Block>();

	public EarthPillar(Player player) {
		super(player);
		if (!bPlayer.canBend(this)) {
			return;
		}
		setFields();
		Block target = BlockSource.getEarthSourceBlock(player, range, ClickType.SHIFT_DOWN);
		if (target != null && !affectedblocks.containsKey(target)) {
			List<Block> blocks = player.getLastTwoTargetBlocks((HashSet<Material>) null, range);
			if (blocks.size() > 1) {
				this.player = player;
				face = blocks.get(1).getFace(blocks.get(0));
				block = blocks.get(1);
				height = getEarthbendableBlocksLength(block, getDirection(face).clone().multiply(-1), height);
				start();
			}
		} else if (target != null && affectedblocks.containsKey(target)) {
			List<Block> blocks = affected.get(affectedblocks.get(target));
			if (blocks != null && !blocks.isEmpty()) {
				for (Block b : blocks) {
					Collapse.revertBlock(b);
				}
				playEarthbendingSound(target.getLocation());
				affected.remove(affectedblocks.get(target));
			}
		}
	}

	public void setFields() {
		height = JedCore.plugin.getConfig().getInt("Abilities.Earth.EarthPillar.Height");
		range = JedCore.plugin.getConfig().getInt("Abilities.Earth.EarthPillar.Range");
	}

	@Override
	public void progress() {
		if (step < height) {
			step++;
			movePillar();
		} else {
			affected.put(this, blocks);
			remove();
			return;
		}
		return;
	}

	private void movePillar() {
		moveEarth(block, getDirection(face), height);
		block = block.getRelative(face);
		affectedblocks.put(block, this);
		blocks.add(block);
	}

	private Vector getDirection(BlockFace face) {
		switch (face) {
		case UP:
			return new Vector(0, 1, 0);
		case DOWN:
			return new Vector(0, -1, 0);
		case NORTH:
			return new Vector(0, 0, -1);
		case SOUTH:
			return new Vector(0, 0, 1);
		case EAST:
			return new Vector(1, 0, 0);
		case WEST:
			return new Vector(-1, 0, 0);
		default:
			return null;
		}
	}

	public static void progressAll() {
		for (Block block : affectedblocks.keySet()) {
			if (!EarthAbility.isEarthbendable(affectedblocks.get(block).getPlayer(), block)) {
				affectedblocks.remove(block);
			}
		}
	}
	
	@Override
	public long getCooldown() {
		return 0;
	}

	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public String getName() {
		return "EarthPillar";
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
		return "* JedCore Addon *\n" + JedCore.plugin.getConfig().getString("Abilities.Earth.EarthPillar.Description");
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
		return JedCore.plugin.getConfig().getBoolean("Abilities.Earth.EarthPillar.Enabled");
	}
}