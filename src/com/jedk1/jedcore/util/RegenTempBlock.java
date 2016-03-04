package com.jedk1.jedcore.util;

import com.projectkorra.projectkorra.util.TempBlock;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import java.util.concurrent.ConcurrentHashMap;

public class RegenTempBlock {

	public static ConcurrentHashMap<Block, Long> blocks = new ConcurrentHashMap<Block, Long>();
	public static ConcurrentHashMap<Block, TempBlock> temps = new ConcurrentHashMap<Block, TempBlock>();
	public static ConcurrentHashMap<Block, BlockState> states = new ConcurrentHashMap<Block, BlockState>();

	/**
	 * Creates a TempBlock that reverts after a delay.
	 * @param block Block to be updated/reverted.
	 * @param material Material to be changed.
	 * @param data Data to be changed.
	 * @param delay Delay until block regens.
	 */
	public RegenTempBlock(Block block, Material material, byte data, long delay) {
		this(block, material, data, delay, true);
	}

	/**
	 * Creates a TempBlock or a State of a block that reverts after a certain time.
	 * @param block Block to be updated/reverted.
	 * @param material Material to be changed.
	 * @param data Data to be changed.
	 * @param delay Delay until block regens.
	 * @param temp Use TempBlock or BlockState.
	 */
	@SuppressWarnings("deprecation")
	public RegenTempBlock(Block block, Material material, byte data, long delay, boolean temp) {
		if (blocks.containsKey(block)) {
			blocks.replace(block, System.currentTimeMillis() + delay);
			block.setType(material);
			block.setData(data);
		} else {
			blocks.put(block, System.currentTimeMillis() + delay);
			if (TempBlock.isTempBlock(block)) {
				TempBlock.get(block).revertBlock();
			}
			if (temp) {
				TempBlock tb = new TempBlock(block, material, data);
				temps.put(block, tb);
			} else {
				states.put(block, block.getState());
				if (material != null) {
					block.setType(material);
					block.setData(data);
				}
			}
		}
	}

	/**
	 * Manages blocks to be reverted.
	 */
	public static void manage() {
		for (Block b : blocks.keySet()) {
			if (System.currentTimeMillis() >= blocks.get(b)) {
				if (temps.containsKey(b)) {
					TempBlock tb = temps.get(b);
					tb.revertBlock();
					temps.remove(b);
				}
				if (states.containsKey(b)) {
					BlockState bs = states.get(b);
					bs.update(true);
					states.remove(b);
				}
				//handlePlant(b);
				blocks.remove(b);
			}
		}
	}

	/**
	 * Reverts an individual block.
	 * @param block
	 */
	public static void revert(Block block) {
		if (blocks.containsKey(block)) {
			if (TempBlock.isTempBlock(block) && temps.containsKey(block)) {
				TempBlock tb = TempBlock.get(block);
				tb.revertBlock();
				temps.remove(block);
			}
			if (states.containsKey(block)) {
				states.get(block).update(true);
				states.remove(block);
			}
			blocks.remove(block);
		}
	}

	/**
	 * Reverts all blocks.
	 */
	public static void revertAll() {
		for (Block b : blocks.keySet()) {
			if (temps.containsKey(b)) {
				TempBlock tb = temps.get(b);
				tb.revertBlock();
			}
			if (states.containsKey(b)) {
				states.get(b).update(true);
			}
		}
		temps.clear();
		states.clear();
		blocks.clear();
	}
	
	/**
	 * Returns true if the block is a RegenTempBlock.
	 * @param block
	 * @return
	 */
	public static boolean hasBlock(Block block) {
		if (blocks.containsKey(block)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Returns true if the block is stored as a temp block.
	 * @param block
	 * @return
	 */
	public static boolean isTempBlock(Block block) {
		if (temps.containsKey(block)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Returns true if the block is stored as a block state.
	 * @param block
	 * @return
	 */
	public static boolean isBlockState(Block block) {
		if (states.containsKey(block)) {
			return true;
		}
		return false;
	}
	
	/**
	 * If a block is a double plant, make sure the top of the plant is replaced.
	 * @param block
	 */
	/* Work in progress.
	@SuppressWarnings("deprecation")
	private static void handlePlant(Block block) {
		if (block.getType().equals(Material.DOUBLE_PLANT)) {
			block = block.getRelative(BlockFace.UP);
			if (block.getType().equals(Material.AIR)) {
				block.setType(Material.DOUBLE_PLANT);
				block.setData((byte) 8);
			}
		}
	}
	*/
}
