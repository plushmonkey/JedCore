package com.jedk1.jedcore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.jedk1.jedcore.util.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.jedk1.jedcore.configuration.JedCoreConfig;
import com.jedk1.jedcore.scoreboard.BendingBoard;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.util.TempBlock;

public class JCMethods {

	private static List<String> worlds = new ArrayList<String>();
	private static List<String> combos = new ArrayList<String>();

	public static List<String> getDisabledWorlds() {
		return JCMethods.worlds;
	}

	public static void registerDisabledWorlds() {
		worlds.clear();
		List<String> registeredworlds = ProjectKorra.plugin.getConfig().getStringList("Properties.DisabledWorlds");
		if (registeredworlds != null && !registeredworlds.isEmpty()) {
			for (String s : registeredworlds) {
				worlds.add(s);
			}
		}
	}
	
	public static boolean isDisabledWorld(World world) {
		return getDisabledWorlds().contains(world.getName());
	}

	public static List<String> getCombos() {
		return JCMethods.combos;
	}

	public static void registerCombos() {
		combos.clear();
		for (String s : ComboManager.getComboAbilities().keySet()) {
			combos.add(s);
		}
	}

	/**
	 * Gets the points of a line between two points.
	 * @param start
	 * @param end
	 * @param points
	 * @return locations
	 */
	public static List<Location> getLinePoints(Location startLoc, Location endLoc, int points){
		List<Location> locations = new ArrayList<Location>();
		Location diff = endLoc.subtract(startLoc);
		double diffX = diff.getX() / points;
		double diffY = diff.getY() / points;
		double diffZ = diff.getZ() / points;
		Location loc = startLoc;
		for(int i = 0; i < points; i++){
			loc.add(new Location(startLoc.getWorld(), diffX, diffY, diffZ));
			locations.add(loc.clone());
		}
		return locations;
	}

	public static List<Location> getCirclePoints(Location location, int points, double size) {
		return getCirclePoints(location, points, size, 0);
	}

	/**
	 * Gets points in a circle.
	 * @param location
	 * @param points
	 * @param size
	 * @return
	 */
	public static List<Location> getCirclePoints(Location location, int points, double size, double startangle){
		List<Location> locations = new ArrayList<Location>();
		for(int i = 0; i < 360; i += 360/points){
			double angle = (i * Math.PI / 180);
			double x = size * Math.cos(angle + startangle);
			double z = size * Math.sin(angle + startangle);
			Location loc = location.clone();
			loc.add(x, 0, z);
			locations.add(loc);
		}
		return locations;
	}

	/**
	 * Gets points in a vertical circle.
	 * @param location
	 * @param points
	 * @param size
	 * @param yawOffset
	 * @return
	 */
	public static List<Location> getVerticalCirclePoints(Location location, int points, double size, float yawOffset) {
		List<Location> locations = new ArrayList<Location>();
		Location fakeLoc = location.clone();
		fakeLoc.setPitch(0);
		fakeLoc.setYaw(yawOffset);
		Vector direction = fakeLoc.getDirection();

		for(double j = -180; j <= 180; j += points){
			Location tempLoc = fakeLoc.clone();
			Vector newDir = direction.clone().multiply(size * Math.cos(Math.toRadians(j)));
			tempLoc.add(newDir);
			tempLoc.setY(tempLoc.getY() + size + (size * Math.sin(Math.toRadians(j))));
			locations.add(tempLoc.clone());
		}
		return locations;
	}

	/**
	 * Remove an item from a players inventory.
	 * @param player
	 * @param material
	 * @param amount
	 * @return
	 */
	public static boolean removeItemFromInventory(Player player, Material material, int amount){
		for(ItemStack i : player.getInventory().getContents()){
			if(i != null && i.getType() == material){
				if (i.getAmount() == amount) {
					player.getInventory().removeItem(i);
				} else if (i.getAmount() > amount) {
					i.setAmount(i.getAmount() - amount);
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets points in a spiral shape.
	 * @param location
	 * @param points
	 * @param spiralCount
	 * @param startAngle
	 * @param startSize
	 * @param finalSize
	 * @param noClip
	 * @return
	 */
	public static List<Location> getSpiralPoints(Location location, int points, int spiralCount, int startAngle, double startSize, double finalSize, boolean noClip){
		return getSpiralPoints(location, points, spiralCount, 0.0D, startAngle, startSize, finalSize, noClip);
	}

	/**
	 * Gets points in a vertical spiral shape, could be used for a tornado.
	 * @param location
	 * @param points
	 * @param spiralCount
	 * @param height
	 * @param startAngle
	 * @param startSize
	 * @param finalSize
	 * @param noClip
	 * @return
	 */
	public static List<Location> getSpiralPoints(Location location, int points, int spiralCount, double height, int startAngle, double startSize, double finalSize, boolean noClip){
		List<Location> locations = new ArrayList<Location>();

		points = points/spiralCount;
		double sizeIncr = ((finalSize - startSize) / points)/spiralCount;
		double hightIncr = (height/points)/spiralCount;
		double size = startSize;
		for(int i = 0; i < spiralCount; i++){
			for(int j = 0; j < 360; j += 360/points){
				hightIncr = hightIncr + ((height/points)/spiralCount);
				size = size + sizeIncr;
				double angle = (j * Math.PI / 180);
				double x = size * Math.cos(angle + startAngle);
				double z = size * Math.sin(angle + startAngle);
				Location loc = location.clone();
				loc.add(x, hightIncr, z);
				if(!noClip && loc.getBlock().getType().equals(Material.AIR))
					locations.add(loc);
				else if(noClip)
					locations.add(loc);
			}
		}

		return locations;
	}

	private static byte full = 0x0;

	@SuppressWarnings("deprecation")
	public static void extinguishBlocks(Player player, String ability, int range, int radius, boolean fire, boolean lava){
		for (Block block : GeneralMethods.getBlocksAroundPoint(player.getTargetBlock((HashSet<Material>) null, (int) range).getLocation(), 2)) {

			Material mat = block.getType();
			if(mat != Material.FIRE && mat != Material.STATIONARY_LAVA && mat != Material.LAVA)
				continue;
			if (GeneralMethods.isRegionProtectedFromBuild(player, ability, block.getLocation()))
				continue;
			if (block.getType() == Material.FIRE && fire) {
				block.setType(Material.AIR);
				block.getWorld().playEffect(block.getLocation(), Effect.EXTINGUISH, 0);
			} else if (block.getType() == Material.STATIONARY_LAVA && lava) {
				block.setType(Material.OBSIDIAN);
				block.getWorld().playEffect(block.getLocation(), Effect.EXTINGUISH, 0);
			} else if (block.getType() == Material.LAVA && lava) {
				if (block.getData() == full) {
					block.setType(Material.OBSIDIAN);
				} else {
					block.setType(Material.COBBLESTONE);
				}
				block.getWorld().playEffect(block.getLocation(), Effect.EXTINGUISH, 0);
			}
		}
	}

	/**
	 * Checks if 3 blocks around the block are of the required type.
	 * @param block
	 * @param type
	 * @return
	 */
	public static boolean isAdjacentToThreeOrMoreSources(Block block, Material type) {
		if (TempBlock.isTempBlock(block)) {
			return false;
		}
		int sources = 0;
		BlockFace[] faces = { BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.UP, BlockFace.DOWN };
		for (BlockFace face : faces) {
			Block blocki = block.getRelative(face);
			if ((blocki.getType() == type)) {
				sources++;
			}
		}
		if (sources >= 2)
			return true;
		return false;
	}

	static Material[] unbreakables = { Material.BEDROCK, Material.BARRIER,
		Material.PORTAL, Material.ENDER_PORTAL,
		Material.ENDER_PORTAL_FRAME, Material.OBSIDIAN};

	public static boolean isUnbreakable(Block block) {
		if (block.getState() instanceof InventoryHolder) {
			return true;
		}
		if (Arrays.asList(unbreakables).contains(block.getType()))
			return true;
		return false;
	}

	public static void reload() {
		JedCore.log.info("JedCore Reloaded.");
		JedCore.plugin.reloadConfig();
		JedCoreConfig.board.reloadConfig();
		CoreAbility.registerPluginAbilities(JedCore.plugin, "com.jedk1.jedcore.ability");
		registerDisabledWorlds();
		registerCombos();
		UpdateChecker.fetch();
		RegenTempBlock.revertAll();
		TempFallingBlock.removeAllFallingBlocks();
		BendingBoard.setFields();
		BendingBoard.updateOnline();
		JedCore.plugin.initializeCollisions();
		FireTick.loadMethod();
		CooldownEnforcer.onConfigReload();

		if (UpdateChecker.hasUpdate()) {
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (player.hasPermission("jedcore.admin.notify") && JedCore.plugin.getConfig().getBoolean("Settings.Updater.Notify")) {
					player.sendMessage(ChatColor.DARK_RED + "JedCore: " + ChatColor.RED + "There is an update available for JedCore!");
				}
			}
		}

		BendingBoard.loadOtherCooldowns();
	}
}
