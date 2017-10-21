package com.jedk1.jedcore.ability.waterbending.combo;

import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.configuration.JedCoreConfig;
import com.jedk1.jedcore.util.RegenTempBlock;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.waterbending.Torrent;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Maelstrom extends WaterAbility implements AddonAbility, ComboAbility {

	private int depth;
	private int range;
	private long cooldown;
	private long duration;

	private List<Block> pool = new ArrayList<Block>();
	private List<Block> wave = new ArrayList<Block>();
	private Location origin;
	private int step;
	private int levelStep;
	private int angle;
	private boolean canRemove;
	private long start;

	public Maelstrom(Player player) {
		super(player);
		if (!bPlayer.canBendIgnoreBinds(this) || hasAbility(player, Maelstrom.class)) {
			return;
		}
		setFields();
		if (setOrigin()) {
			start();
			bPlayer.addCooldown(this);
			Torrent t = getAbility(player, Torrent.class);
			if (t != null) {
				t.remove();
			}
		}
	}

	public void setFields() {
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);

		cooldown = config.getLong("Abilities.Water.WaterCombo.Maelstrom.Cooldown");
		duration = config.getLong("Abilities.Water.WaterCombo.Maelstrom.Duration");
		depth = config.getInt("Abilities.Water.WaterCombo.Maelstrom.MaxDepth");
		range = config.getInt("Abilities.Water.WaterCombo.Maelstrom.Range");
		canRemove = true;
		start = System.currentTimeMillis();
	}

	public boolean setOrigin() {
		Block block = BlockSource.getWaterSourceBlock(player, range, ClickType.LEFT_CLICK, true, false, false);
		if (block != null) {
			if (!isTransparent(block.getRelative(BlockFace.UP))) {
				return false;
			}
			for (int i = 0; i < depth; i++) {
				if (!isWater(block.getRelative(BlockFace.DOWN, i))) {
					setDepth(i - 1);
					break;
				}
			}
			if (getDepth() < 3) {
				return false;
			}
			origin = block.getLocation().clone();
			for (Location l : GeneralMethods.getCircle(origin, getDepth(), 1, false, false, 0)) {
				if (!isWater(l.getBlock())) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public void progress() {
		if (player == null || !player.isOnline() || player.getWorld() != origin.getWorld()) {
			remove();
			return;
		}
		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			remove();
			return;
		}
		if (System.currentTimeMillis() > start + duration) {
			remove();
			return;
		}
		removeWater(false);
		playAnimation();
		dragEntities();
		if (canRemove && (step == 0 || step % 20 == 0)) {
			if (levelStep < getDepth()) {
				levelStep++;
				removeWater(true);
			}
			if (step == 20) {
				step = 0;
			}
		}
		step++;
	}

	public void removeWater(boolean increase) {
		if (increase) {
			pool.clear();
			for (int i = 0; i < levelStep; i++) {
				for (Location l : GeneralMethods.getCircle(origin.clone().subtract(0, i, 0), levelStep - i, 1, false, false, 0)) {
					if (!isWater(l.getBlock()) && !isTransparent(l.getBlock())) {
						canRemove = false;
						break;
					}
					if (!pool.contains(l.getBlock())) {
						pool.add(l.getBlock());
					}
				}
			}
		}
		for (Block b : pool) {
			if (wave.contains(b)) continue;
			new RegenTempBlock(b, Material.AIR, (byte) 0, 100);
		}
	}

	public void dragEntities(){
		for(Block b : pool){
			if (pool.indexOf(b) % 3 == 0) {
				Location l = b.getLocation();
				for(Entity entity : GeneralMethods.getEntitiesAroundPoint(l, 1.5D)){
					Vector direction = GeneralMethods.getDirection(entity.getLocation(), origin.clone().subtract(-0.5, (levelStep - 1), -0.5));
					entity.setVelocity(direction.multiply(0.2));
				}
			}
		}
	}

	public void playAnimation() {
		wave.clear();
		int waves = 5;
		int newAngle = this.angle;
		for (int i = 0; i < levelStep; i++) {
			for (int degree = 0; degree < waves; degree++) {
				double size = (levelStep - i) - 1;
				double angle = ((newAngle + (degree * (360/waves))) * Math.PI / 180);
				double x = size * Math.cos(angle);
				double z = size * Math.sin(angle);
				Location loc = origin.clone();
				loc.add(x + 0.5, -(i - 0.5), z + 0.5);
				Block b = loc.getBlock();
				for (int j = 0; j < 2; j++) {
					wave.add(b.getRelative(BlockFace.DOWN, j));
					new RegenTempBlock(b.getRelative(BlockFace.DOWN, j), Material.WATER, (byte) 1, 0);
					ParticleEffect.WATER_SPLASH.display((float) Math.random(), (float) Math.random(), (float) Math.random(), 0, 3, loc, 257D);
				}
			}
			newAngle+=15;
		}
		this.angle+=(levelStep*2);

	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public int getDepth() {
		return depth;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return origin;
	}

	@Override
	public String getName() {
		return "Maelstrom";
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
	public Object createNewComboInstance(Player player) {
		return new Maelstrom(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combination = new ArrayList<>();
		combination.add(new AbilityInformation("WaterBubble", ClickType.SHIFT_DOWN));
		combination.add(new AbilityInformation("Torrent", ClickType.LEFT_CLICK));
		combination.add(new AbilityInformation("Torrent", ClickType.LEFT_CLICK));
		return combination;
	}

	@Override
	public String getInstructions() {
		return "WaterBubble (Hold Shift) > Torrent (Left Click) > Torrent (Left Click)";
	}

	@Override
	public String getDescription() {
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);
		return "* JedCore Addon *\n" + config.getString("Abilities.Water.WaterCombo.Maelstrom.Description");
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
		return config.getBoolean("Abilities.Water.WaterCombo.Maelstrom.Enabled");
	}
}
