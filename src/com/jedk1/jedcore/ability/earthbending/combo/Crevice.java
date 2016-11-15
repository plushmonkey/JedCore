package com.jedk1.jedcore.ability.earthbending.combo;

import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.util.RegenTempBlock;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.util.ClickType;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class Crevice extends EarthAbility implements AddonAbility, ComboAbility {
	
	private double range;
	private long regenDelay;
	private int randomDepth;
	private int avatarDepth;
	private long cooldown;

	private Location origin;
	private Location location;
	private Vector direction;
	private double travelled;
	private boolean skip;
	private long time;

	private HashMap<Integer, List<BlockState>> collumn = new HashMap<Integer, List<BlockState>>();

	Random rand = new Random();

	public Crevice(Player player) {
		super(player);
		if (!bPlayer.canBendIgnoreBinds(this)) {
			return;
		}
		
		setFields();
		time = System.currentTimeMillis();
		createInstance();
	}
	
	public void setFields() {
		range = JedCore.plugin.getConfig().getDouble("Abilities.Earth.EarthCombo.Crevice.Range");
		regenDelay = JedCore.plugin.getConfig().getLong("Abilities.Earth.EarthCombo.Crevice.RevertDelay");
		randomDepth = JedCore.plugin.getConfig().getInt("Abilities.Earth.EarthCombo.Crevice.Depth");
		avatarDepth = JedCore.plugin.getConfig().getInt("Abilities.Earth.EarthCombo.Crevice.AvatarStateDepth");
		cooldown = JedCore.plugin.getConfig().getLong("Abilities.Earth.EarthCombo.Crevice.Cooldown");
	}

	private void createInstance() {
		origin = player.getTargetBlock((HashSet<Material>) null, (int) 6).getLocation();
		if (isEarthbendable(origin.getBlock())) {
			Location tempLoc = player.getLocation().clone();
			tempLoc.setPitch(0);
			direction = tempLoc.getDirection().clone();
			origin.setDirection(tempLoc.getDirection());
			location = origin.clone();
			bPlayer.addCooldown(this);
			if (bPlayer.isAvatarState()) {
				randomDepth = avatarDepth;
			}

			start();
		}
	}

	@Override
	public void progress() {
		if (player.isDead() || !player.isOnline()) {
			prepareRevert();
			remove();
			return;
		}
		if (travelled >= range || skip) {
			if (System.currentTimeMillis() > time + regenDelay) {
				prepareRevert();
				remove();
				return;
			}
			return;
		}
		advanceCrevice();
	}

	public static void closeCrevice(Player player) {
		Block target = player.getTargetBlock((HashSet<Material>) null, (int) 10);
		for (Block near : GeneralMethods.getBlocksAroundPoint(target.getLocation(), 2)) {
			for (Crevice c : getAbilities(Crevice.class)) {
				for (int id2 : c.collumn.keySet()) {
					for (BlockState bs : c.collumn.get(id2)) {
						if (near.getLocation().equals(bs.getLocation())) {
							c.prepareRevert();
							c.remove();
							return;
						}
					}
				}
			}
		}
	}

	private void advanceCrevice() {
		switch (rand.nextInt(2)) {
			case 0:
				if (location.getYaw() <= origin.getYaw()) {
					location.setYaw(location.getYaw() + 40);
					direction = location.getDirection().clone();
				}
				break;
			case 1:
				if (location.getYaw() >= origin.getYaw()) {
					location.setYaw(location.getYaw() - 40);
					direction = location.getDirection().clone();
				}
				break;
			default:
				direction = location.getDirection().clone();
				break;
		}

		Location tempLoc = location.clone();
		location = location.add(direction.multiply(1));
		playEarthbendingSound(tempLoc);
		location.getWorld().playSound(location, Sound.ENTITY_ZOMBIE_BREAK_DOOR_WOOD, (float) 0.5, (float) 0.5);
		if (skip) {
			return;
		}

		travelled++;
		if (travelled >= range)
			return;

		if (GeneralMethods.isRegionProtectedFromBuild(player, "RaiseEarth", location)) {
			return;
		}

		if (!isTransparent(location.getBlock().getRelative(BlockFace.UP))) {
			location.add(0, 1, 0);
			if (!isTransparent(location.getBlock().getRelative(BlockFace.UP)) || !isEarthbendable(location.getBlock())) {
				skip = true;
				return;
			}
		} else if (isTransparent(location.getBlock()) || !isEarthbendable(location.getBlock())) {
			location.subtract(0, 1, 0);
			if (isTransparent(location.getBlock()) || !isEarthbendable(location.getBlock())) {
				skip = true;
				return;
			}
		}

		removePilar(tempLoc, randInt(randomDepth + 1 - 2, randomDepth + 1 + 2));
		removePilar(GeneralMethods.getRightSide(tempLoc, 1), randInt(randomDepth - 1, randomDepth + 1));
		removePilar(GeneralMethods.getLeftSide(tempLoc, 1), randInt(randomDepth - 1, randomDepth + 1));
	}
	
	public int randInt(int min, int max) {
		return rand.nextInt(max - min) + min;
	}

	private void removePilar(Location location, int depth) {
		List<BlockState> blocks = new ArrayList<BlockState>();
		Location tempLoc = location.clone().getBlock().getLocation();
		tempLoc.add(0, 1, 0);
		for (int i = 0; i < depth + 1; i++) {
			if (tempLoc.getY() < 2 || tempLoc.getY() > 255) {
				return;
			}
			if (GeneralMethods.isRegionProtectedFromBuild(player, "Crevice", tempLoc)) {
				continue;
			}
			if (i == 0 && !isTransparent(tempLoc.getBlock())) {
				continue;
			}
			if (i > 0 && !isEarthbendable(tempLoc.getBlock())) {
				continue;
			}

			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(tempLoc, 1)) {
				entity.setVelocity(new Vector(0, -0.75, 0));
			}

			blocks.add(tempLoc.getBlock().getState());
			tempLoc.getBlock().setType(Material.AIR);
			tempLoc.subtract(0, 1, 0);
		}
		Collections.reverse(blocks);
		collumn.put(collumn.size() + 1, blocks);
	}

	private void prepareRevert() {
		for (int id : collumn.keySet()) {
			for (BlockState bs : collumn.get(id)) {
				bs.update(true);
				for (Entity entity : GeneralMethods.getEntitiesAroundPoint(bs.getLocation(), 1)) {
					entity.setVelocity(new Vector(0, 0.7, 0));
				}
				new RegenTempBlock(bs.getBlock(), Material.AIR, (byte) 0, collumn.get(id).indexOf(bs) * 50);
			}
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
		return "Crevice";
	}
	
	@Override
	public boolean isHiddenAbility() {
		return true;
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
	public Object createNewComboInstance(Player player) {
		return new Crevice(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combination = new ArrayList<>();
		combination.add(new AbilityInformation("Collapse", ClickType.RIGHT_CLICK_BLOCK));
		combination.add(new AbilityInformation("Shockwave", ClickType.SHIFT_DOWN));
		combination.add(new AbilityInformation("Shockwave", ClickType.SHIFT_UP));
		combination.add(new AbilityInformation("Shockwave", ClickType.SHIFT_DOWN));
		return combination;
	}

	@Override
	public String getInstructions() {
		return "Collapse (Right Click a block) > Shockwave (Tap Shift) > Shockwave (Tap Shift)";
	}

	@Override
	public String getDescription() {
	   return "* JedCore Addon *\n" + JedCore.plugin.getConfig().getString("Abilities.Earth.EarthCombo.Crevice.Description");
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
	public void load() {
	}

	@Override
	public void stop() {
	}
	
	@Override
	public boolean isEnabled() {
		return JedCore.plugin.getConfig().getBoolean("Abilities.Earth.EarthCombo.Crevice.Enabled");
	}
}
