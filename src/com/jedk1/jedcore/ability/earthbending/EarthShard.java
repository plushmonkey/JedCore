package com.jedk1.jedcore.ability.earthbending;

import java.util.*;
import java.util.stream.Collectors;

import com.jedk1.jedcore.collision.AABB;
import com.jedk1.jedcore.collision.CollisionDetector;
import com.jedk1.jedcore.collision.CollisionUtil;
import com.jedk1.jedcore.configuration.JedCoreConfig;
import com.jedk1.jedcore.util.BlockUtil;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.earthbending.passive.DensityShift;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.util.TempFallingBlock;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;

public class EarthShard extends EarthAbility implements AddonAbility {
	public static int range;
	public static int abilityRange;

	public static double normalDmg;
	public static double metalDmg;

	public static int maxShards;
	public static long cooldown;

	private boolean isThrown = false;
	private Location origin;
	private double abilityCollisionRadius;
	private double entityCollisionRadius;

	private List<TempBlock> tblockTracker = new ArrayList<>();
	private List<TempBlock> readyBlocksTracker = new ArrayList<>();
	private List<TempFallingBlock> fallingBlocks = new ArrayList<>();

	public EarthShard(Player player) {
		super(player);

		if (!bPlayer.canBend(this)) {
			return;
		}

		if (hasAbility(player, EarthShard.class)) {
			for (EarthShard es : EarthShard.getAbilities(player, EarthShard.class)) {
				if (es.isThrown && System.currentTimeMillis() - es.getStartTime() >= 20000) {
					// Remove the old instance because it got into a broken state.
					// This shouldn't affect normal gameplay because the cooldown is long enough that the
					// shards should have already hit their target.
					es.remove();
				} else {
					es.select();
					return;
				}
			}
		}

		setFields();
		origin = player.getLocation().clone();
		raiseEarthBlock(getEarthSourceBlock(range));
		start();
	}

	public void setFields() {
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);
		
		range = config.getInt("Abilities.Earth.EarthShard.PrepareRange");
		abilityRange = config.getInt("Abilities.Earth.EarthShard.AbilityRange");
		normalDmg = config.getDouble("Abilities.Earth.EarthShard.Damage.Normal");
		metalDmg = config.getDouble("Abilities.Earth.EarthShard.Damage.Metal");
		maxShards = config.getInt("Abilities.Earth.EarthShard.MaxShards");
		cooldown = config.getLong("Abilities.Earth.EarthShard.Cooldown");
		abilityCollisionRadius = config.getDouble("Abilities.Earth.EarthShard.AbilityCollisionRadius");
		entityCollisionRadius = config.getDouble("Abilities.Earth.EarthShard.EntityCollisionRadius");
	}

	public void select() {
		raiseEarthBlock(getEarthSourceBlock(range));
	}

	@SuppressWarnings("deprecation")
	public void raiseEarthBlock(Block block) {
		if (block == null) {
			return;
		}

		if (tblockTracker.size() >= maxShards) {
			return;
		}

		Vector blockVector = block.getLocation().toVector().toBlockVector().setY(0);

		// Don't select from locations that already have an EarthShard block.
		for (TempBlock tempBlock : tblockTracker) {
			if (tempBlock.getLocation().getWorld() != block.getWorld()) {
				continue;
			}

			Vector tempBlockVector = tempBlock.getLocation().toVector().toBlockVector().setY(0);

			if (tempBlockVector.equals(blockVector)) {
				return;
			}
		}
		
		for (int i = 1; i < 4; i++) {
			if (!isTransparent(block.getRelative(BlockFace.UP, i))) {
				return;
			}
		}

		if (isEarthbendable(block)) {
			if (isMetal(block)) {
				playMetalbendingSound(block.getLocation());
			} else {
				ParticleEffect.BLOCK_CRACK.display(block.getLocation().add(0, 1, 0), 20, 0.0, 0.0, 0.0, 0.0, block.getBlockData());
				playEarthbendingSound(block.getLocation());
			}

			Material material = getCorrectType(block);

			if (DensityShift.isPassiveSand(block)) {
				DensityShift.revertSand(block);
			}

			Location loc = block.getLocation().add(0.5, 0, 0.5);
			new TempFallingBlock(loc, material.createBlockData(), new Vector(0, 0.8, 0), this);
			TempBlock tb = new TempBlock(block, Material.AIR, Material.AIR.createBlockData());
			tblockTracker.add(tb);
		}
	}
	
	@SuppressWarnings("deprecation")
	public Material getCorrectType(Block block) {
		if (block.getType().equals(Material.SAND)) {
			if (block.getData() == (byte) 0x1) {
				return Material.RED_SANDSTONE;
			}

			return Material.SANDSTONE;
		}

		if (block.getType().equals(Material.GRAVEL)) {
			return Material.COBBLESTONE;
		}

		return block.getType();
	}

	@SuppressWarnings("deprecation")
	public void progress() {
		if (player == null || !player.isOnline() || player.isDead()) {
			remove();
			return;
		}

		if (!isThrown) {
			if (!bPlayer.canBendIgnoreCooldowns(this)) {
				remove();
				return;
			}

			if (tblockTracker.isEmpty()) {
				remove();
				return;
			}

			for (TempFallingBlock tfb : TempFallingBlock.getFromAbility(this)) {
				FallingBlock fb = tfb.getFallingBlock();

				if (fb.isDead() || fb.getLocation().getBlockY() == origin.getBlockY() + 2) {
					TempBlock tb = new TempBlock(fb.getLocation().getBlock(), fb.getMaterial(), fb.getBlockData());
					readyBlocksTracker.add(tb);
					tfb.remove();
				}
			}
		} else {
			for (TempFallingBlock tfb : TempFallingBlock.getFromAbility(this)) {
				FallingBlock fb = tfb.getFallingBlock();

				AABB collider = BlockUtil.getFallingBlockBoundsFull(fb).scale(entityCollisionRadius * 2.0);

				CollisionDetector.checkEntityCollisions(player, collider, (e) -> {
					DamageHandler.damageEntity(e, isMetal(fb.getMaterial()) ? metalDmg : normalDmg, this);
					((LivingEntity) e).setNoDamageTicks(0);
					ParticleEffect.BLOCK_CRACK.display(fb.getLocation(), 20, 0, 0, 0, 0, fb.getBlockData());
					tfb.remove();
					return false;
				});

				if (fb.isDead()) {
					tfb.remove();
				}
			}

			if (TempFallingBlock.getFromAbility(this).isEmpty()) {
				remove();
			}
		}
	}

	public static void throwShard(Player player) {
		if (hasAbility(player, EarthShard.class)) {
			for (EarthShard es : EarthShard.getAbilities(player, EarthShard.class)) {
				if (!es.isThrown) {
					es.throwShard();
					break;
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void throwShard() {
		if (isThrown || tblockTracker.size() > readyBlocksTracker.size()) {
			return;
		}

		Location targetLocation = GeneralMethods.getTargetedLocation(player, abilityRange);

		if (GeneralMethods.getTargetedEntity(player, abilityRange, new ArrayList<>()) != null) {
			targetLocation = GeneralMethods.getTargetedEntity(player, abilityRange, new ArrayList<>()).getLocation();
		}

		Vector vel = null;

		for (TempBlock tb : readyBlocksTracker) {
			Location target = player.getTargetBlock(null, 30).getLocation();

			if (target.getBlockX() == tb.getBlock().getX() && target.getBlockY() == tb.getBlock().getY() && target.getBlockZ() == tb.getBlock().getZ()) {
				vel = player.getEyeLocation().getDirection().multiply(2).add(new Vector(0, 0.2, 0));
				break;
			}

			vel = GeneralMethods.getDirection(tb.getLocation(), targetLocation).normalize().multiply(2).add(new Vector(0, 0.2, 0));
		}

		for (TempBlock tb : readyBlocksTracker) {
			fallingBlocks.add(new TempFallingBlock(tb.getLocation(), tb.getBlock().getBlockData(), vel, this));
			tb.revertBlock();
		}

		revertBlocks();

		isThrown = true;

		if (player.isOnline()) {
			bPlayer.addCooldown(this);
		}
	}

	public void revertBlocks() {
		for (TempBlock tb : tblockTracker) {
			tb.revertBlock();
		}

		for (TempBlock tb : readyBlocksTracker) {
			tb.revertBlock();
		}

		tblockTracker.clear();
		readyBlocksTracker.clear();
	}

	@Override
	public void remove() {
		// Destroy any remaining falling blocks.
		for (TempFallingBlock tfb : TempFallingBlock.getFromAbility(this)) {
			tfb.remove();
		}

		revertBlocks();

		super.remove();
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
	public List<Location> getLocations() {
		return fallingBlocks.stream().map(TempFallingBlock::getLocation).collect(Collectors.toList());
	}

	@Override
	public void handleCollision(Collision collision) {
		CollisionUtil.handleFallingBlockCollisions(collision, fallingBlocks);
	}

	@Override
	public double getCollisionRadius() {
		return abilityCollisionRadius;
	}

	@Override
	public String getName() {
		return "EarthShard";
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
		return "* JedCore Addon *\n" + config.getString("Abilities.Earth.EarthShard.Description");
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
		return config.getBoolean("Abilities.Earth.EarthShard.Enabled");
	}
}
