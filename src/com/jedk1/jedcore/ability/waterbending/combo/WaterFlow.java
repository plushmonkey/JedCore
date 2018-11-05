package com.jedk1.jedcore.ability.waterbending.combo;

import com.jedk1.jedcore.JCMethods;
import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.configuration.JedCoreConfig;
import com.jedk1.jedcore.util.MaterialUtil;
import com.jedk1.jedcore.util.RegenTempBlock;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.airbending.AirSpout;
import com.projectkorra.projectkorra.earthbending.Catapult;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.WaterManipulation;
import com.projectkorra.projectkorra.waterbending.WaterSpout;

import com.projectkorra.projectkorra.waterbending.plant.PlantRegrowth;
import com.projectkorra.projectkorra.waterbending.util.WaterReturn;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class WaterFlow extends WaterAbility implements AddonAbility, ComboAbility {

	private int sourcerange; //10
	private int maxrange; //40
	private int minrange; //8
	private long duration; //10000
	private long cooldown; //15000
	private long meltdelay; //5000
	private long trail; //80
	private boolean avatar; //true
	private boolean stayatsource; //true
	private int stayrange; //100
	private boolean fullmoonEnabled;
	private int fullmoonCooldown;
	private int fullmoonDuration;
	private boolean playerRideOwnFlow;
	private int size; //1;
	private int avatarSize; //3;
	private int fullmoonSizeSmall; //2;
	private int fullmoonSizeLarge; //3;
	private long avatarDuration; //60000;
	private boolean canUseBottle;
	private boolean canUsePlants;
	private boolean removeOnAnyDamage;
	
	private long time;
	private Location origin;
	private Location head;
	private int range;
	private Vector direction;
	private Block sourceblock;
	private boolean frozen;
	private double prevHealth;
	private int headsize;
	private boolean usingBottle;
	private ConcurrentHashMap<Block, Location> directions = new ConcurrentHashMap<Block, Location>();
	private List<Block> blocks = new ArrayList<Block>();
	private List<Block> sources = new ArrayList<Block>();
	
	Random rand = new Random();

	public WaterFlow(Player player) {
		super(player);
		if (!bPlayer.canBendIgnoreBinds(this)) {
			return;
		}
		if (JCMethods.isLunarEclipse(player.getWorld())) {
			return;
		}
		if (hasAbility(player, WaterFlow.class)) {
			((WaterFlow) getAbility(player, WaterFlow.class)).remove();
			return;
		}
		setFields();

		usingBottle = false;

		if (prepare()) {
			headsize = size;
			trail = trail * size;
			range = maxrange;
			prevHealth = player.getHealth();
			time = System.currentTimeMillis();

			int augment = (int) Math.round(getNightFactor(player.getWorld()));
			if (isFullMoon(player.getWorld()) && fullmoonEnabled) {
				sources = getNearbySources(sourceblock, 3);
				if (sources != null) {
					if (sources.size() > 9) {
						headsize = fullmoonSizeSmall;
					}
					if (sources.size() > 36) {
						headsize = fullmoonSizeLarge;
					}
					trail = trail * augment;
					range = range - (range / 3);
					maxrange = range;
					duration = duration * fullmoonDuration;
					cooldown = cooldown * fullmoonCooldown;
				}
			}
			if (bPlayer.isAvatarState()) {
				headsize = avatarSize;
				if (avatar) {
					duration = 0;
				} else {
					duration = avatarDuration;
				}
			}
			start();
			if (hasAbility(player, WaterManipulation.class)) {
				WaterManipulation manip = (WaterManipulation) getAbility(player, WaterManipulation.class);
				manip.remove();
			}
		}
	}
	
	public void setFields() {
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);

		sourcerange = config.getInt("Abilities.Water.WaterCombo.WaterFlow.SourceRange");
		maxrange = config.getInt("Abilities.Water.WaterCombo.WaterFlow.MaxRange");
		minrange = config.getInt("Abilities.Water.WaterCombo.WaterFlow.MinRange");
		duration = config.getLong("Abilities.Water.WaterCombo.WaterFlow.Duration");
		cooldown = config.getInt("Abilities.Water.WaterCombo.WaterFlow.Cooldown");
		meltdelay = config.getInt("Abilities.Water.WaterCombo.WaterFlow.MeltDelay");
		trail = config.getInt("Abilities.Water.WaterCombo.WaterFlow.Trail");
		avatar = config.getBoolean("Abilities.Water.WaterCombo.WaterFlow.IsAvatarStateToggle");
		avatarDuration = config.getLong("Abilities.Water.WaterCombo.WaterFlow.AvatarStateDuration");
		stayatsource = config.getBoolean("Abilities.Water.WaterCombo.WaterFlow.PlayerStayNearSource");
		stayrange = config.getInt("Abilities.Water.WaterCombo.WaterFlow.MaxDistanceFromSource");
		canUseBottle = config.getBoolean("Abilities.Water.WaterCombo.WaterFlow.BottleSource");
		canUsePlants = config.getBoolean("Abilities.Water.WaterCombo.WaterFlow.PlantSource");
		removeOnAnyDamage = config.getBoolean("Abilities.Water.WaterCombo.WaterFlow.RemoveOnAnyDamage");
		fullmoonCooldown = config.getInt("Abilities.Water.WaterCombo.WaterFlow.FullMoon.Modifier.Cooldown");
		fullmoonDuration = config.getInt("Abilities.Water.WaterCombo.WaterFlow.FullMoon.Modifier.Duration");
		fullmoonEnabled = config.getBoolean("Abilities.Water.WaterCombo.WaterFlow.FullMoon.Enabled");
		playerRideOwnFlow = config.getBoolean("Abilities.Water.WaterCombo.WaterFlow.PlayerRideOwnFlow");
		size = config.getInt("Abilities.Water.WaterCombo.WaterFlow.Size.Normal");
		avatarSize = config.getInt("Abilities.Water.WaterCombo.WaterFlow.Size.AvatarState");
		fullmoonSizeSmall = config.getInt("Abilities.Water.WaterCombo.WaterFlow.Size.FullmoonSmall");
		fullmoonSizeLarge = config.getInt("Abilities.Water.WaterCombo.WaterFlow.Size.FullmoonLarge");
	}

	@SuppressWarnings("deprecation")
	public static List<Block> getNearbySources(Block block, int searchrange) {
		List<Block> sources = new ArrayList<Block>();
		for (Location l : GeneralMethods.getCircle(block.getLocation(), searchrange, 2, false, false, -1)) {
			Block blocki = l.getBlock();
			if (isWater(block)) {
				if (blocki.getType() == Material.WATER && JCMethods.isLiquidSource(blocki) && WaterManipulation.canPhysicsChange(blocki)) {
					sources.add(blocki);
				}
			}
			if (isLava(block)) {
				if (blocki.getType() == Material.LAVA && JCMethods.isLiquidSource(blocki) && WaterManipulation.canPhysicsChange(blocki)) {
					sources.add(blocki);
				}
			}
		}
		return sources;
	}

	private boolean prepare() {
		sourceblock = BlockSource.getWaterSourceBlock(player, sourcerange, ClickType.SHIFT_DOWN, true, bPlayer.canIcebend(), canUsePlants);
		if (sourceblock != null) {
			boolean isGoodSource = GeneralMethods.isAdjacentToThreeOrMoreSources(sourceblock) || (TempBlock.isTempBlock(sourceblock) && WaterAbility.isBendableWaterTempBlock(sourceblock));

			// canUsePlants needs to be checked here due to a bug with PK dynamic source caching.
			// getWaterSourceBlock can return a plant even if canUsePlants is passed as false.
			if (isGoodSource || (canUsePlants && isPlant(sourceblock))) {
				head = sourceblock.getLocation().clone();
				origin = sourceblock.getLocation().clone();
				if (isPlant(sourceblock)) {
					new PlantRegrowth(player, sourceblock);
				}
				return true;
			}
		}

		if (canUseBottle && WaterReturn.hasWaterBottle(player)){
			Location eye = player.getEyeLocation();
			Location forward = eye.clone().add(eye.getDirection());

			if (isTransparent(eye.getBlock()) && isTransparent(forward.getBlock())) {
				head = forward.clone();
				origin = forward.clone();
				usingBottle = true;
				WaterReturn.emptyWaterBottle(player);
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		}
		if (!bPlayer.canBendIgnoreBinds(this) || !bPlayer.canBendIgnoreCooldowns(getAbility("WaterManipulation"))) {
			remove();
			return;
		}
		if (duration > 0 && System.currentTimeMillis() > time + duration) {
			remove();
			return;
		}
		if ((stayatsource && player.getLocation().distance(origin) >= stayrange) || head.getY() > 255 || head.getY() < 1) {
			remove();
			return;
		}
		if (GeneralMethods.isRegionProtectedFromBuild(player, "Torrent", head)) {
			remove();
			return;
		}
		if (AirAbility.isWithinAirShield(head)) {
			remove();
			return;
		}
		if (prevHealth > player.getHealth()) {
			remove();
			return;
		}

		if (removeOnAnyDamage) {
			// Only update the previous health if any damage should remove it.
			prevHealth = player.getHealth();
		}

		if (!frozen) {
			if (player.isSneaking()) {
				if (range >= minrange) {
					range -= 2;
				}
				//BlockSource.update(player, sourcerange, ClickType.RIGHT_CLICK);
			} else {
				if (range < maxrange) {
					range += 2;
				}
			}
			moveWater();
			//updateBlocks();
			manageLength();
		}
	}

	private void manageLength() {
		int pos = 0;
		int ids = 0;
		List<Block> templist = new ArrayList<Block>(blocks);
		for (Block block : templist) {

			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(block.getLocation(), 2.8)) {
				if (entity.getEntityId() == player.getEntityId() && !playerRideOwnFlow) {
					continue;
				}
				if (getPlayers(AirSpout.class).contains(entity)) {
					continue;
				} else if (getPlayers(WaterSpout.class).contains(entity)) {
					continue;
				} else if (getPlayers(Catapult.class).contains(entity)) {
					continue;
				}
				Location temp = directions.get(block);
				Vector dir = GeneralMethods.getDirection(entity.getLocation(), directions.get(block).add(temp.getDirection().multiply(1.5)));
				entity.setVelocity(dir.clone().normalize().multiply(1));
				entity.setFallDistance(0f);
			}

			if (!MaterialUtil.isTransparent(block) || GeneralMethods.isRegionProtectedFromBuild(player, "Torrent", block.getLocation())) {
				blocks.remove(block);
				directions.remove(block);
				if (TempBlock.isTempBlock(block)) {
					TempBlock.revertBlock(block, Material.AIR);
				}
			} else {
				if (!isWater(block)) {
					new TempBlock(block, Material.WATER, Material.WATER.createBlockData(bd -> ((Levelled)bd).setLevel(0)));
				}
			}
			pos++;
			if (pos > trail) {
				ids++;
			}
		}
		for (int i = 0; i < ids; i++) {
			if (i >= blocks.size()) {
				break;
			}
			Block block = blocks.get(i);
			blocks.remove(block);
			directions.remove(block);
			if (TempBlock.isTempBlock(block)) {
				TempBlock.revertBlock(block, Material.AIR);
			}
		}
		templist.clear();
	}

	private void moveWater() {
		if (!MaterialUtil.isTransparent(head.getBlock()) || GeneralMethods.isRegionProtectedFromBuild(player, "Torrent", head)) {
			range -= 2;
		}
		direction = GeneralMethods.getDirection(head, GeneralMethods.getTargetedLocation(player, range, Material.WATER)).normalize();
		head = head.add(direction.clone().multiply(1));
		head.setDirection(direction);
		playWaterbendingSound(head);
		for (Block block : GeneralMethods.getBlocksAroundPoint(head, headsize)) {
			if (directions.containsKey(block)) {
				directions.replace(block, head.clone());
			} else {
				directions.put(block, head.clone());
				blocks.add(block);
			}
		}
	}

	private void removeBlocks() {
		for (Block block : directions.keySet()) {
			if (TempBlock.isTempBlock(block)) {
				TempBlock.revertBlock(block, Material.AIR);
			}
		}
	}

	public static void freeze(Player player) {
		if (hasAbility(player, WaterFlow.class)) {
			WaterFlow wf = (WaterFlow) getAbility(player, WaterFlow.class);
			if (!wf.bPlayer.canIcebend()) return;
			if (!wf.frozen) {
				wf.bPlayer.addCooldown(wf);
				wf.freeze();
			}
		}
	}

	private void freeze() {
		frozen = true;
		for (Block block : directions.keySet()) {
			if (TempBlock.isTempBlock(block)) {
				if (rand.nextInt(5) == 0) {
					playIcebendingSound(block.getLocation());
				}
				new RegenTempBlock(block, Material.ICE, Material.ICE.createBlockData(), randInt((int) meltdelay - 250, (int) meltdelay + 250));
			}
		}
	}
	
	public int randInt(int min, int max) {
		return rand.nextInt(max - min) + min;
	}

	@Override
	public void remove() {
		if (player.isOnline() && cooldown > 0) {
			bPlayer.addCooldown(this);
		}
		if (!frozen) {
			removeBlocks();
		}

		if (usingBottle) {
			new WaterReturn(player, head.getBlock());
		}
		super.remove();
	}
	
	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return head;
	}

	@Override
	public String getName() {
		return "WaterFlow";
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
		return new WaterFlow(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combination = new ArrayList<>();
		combination.add(new AbilityInformation("WaterManipulation", ClickType.SHIFT_DOWN));
		combination.add(new AbilityInformation("WaterManipulation", ClickType.SHIFT_UP));
		combination.add(new AbilityInformation("Torrent", ClickType.SHIFT_DOWN));
		combination.add(new AbilityInformation("Torrent", ClickType.SHIFT_UP));
		combination.add(new AbilityInformation("Torrent", ClickType.SHIFT_DOWN));
		combination.add(new AbilityInformation("WaterManipulation", ClickType.SHIFT_UP));
		return combination;
	}

	@Override
	public String getInstructions() {
		return "WaterManipulation (Tap Shift) > Torrent (Tap Shift) > Torrent (Hold Shift) > WaterManipulation (Release Shift)";
	}

	@Override
	public String getDescription() {
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);
	   return "* JedCore Addon *\n" + config.getString("Abilities.Water.WaterCombo.WaterFlow.Description");
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
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);
		return config.getBoolean("Abilities.Water.WaterCombo.WaterFlow.Enabled");
	}
}
