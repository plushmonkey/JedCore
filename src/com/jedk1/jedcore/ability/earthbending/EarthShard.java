package com.jedk1.jedcore.ability.earthbending;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.projectkorra.projectkorra.earthbending.passive.EarthPassive;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
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
import com.projectkorra.projectkorra.util.ParticleEffect.BlockData;
import com.projectkorra.projectkorra.util.TempBlock;

public class EarthShard extends EarthAbility implements AddonAbility {

	public static int range;
	public static int abilityRange;

	public static double normalDmg;
	public static double metalDmg;

	public static int maxShards;
	public static long cooldown;

	private boolean isPreparing = true;
	private boolean isThrown = false;
	private Location origin;

	private List<TempBlock> tblockTracker = new ArrayList<TempBlock>();
	private List<TempBlock> readyBlocksTracker = new ArrayList<TempBlock>();

	public EarthShard(Player player) {
		super(player);

		if (!bPlayer.canBend(this)) {
			return;
		}

		if (hasAbility(player, EarthShard.class)) {
			for (EarthShard es : EarthShard.getAbilities(player, EarthShard.class)) {
				if (!es.isThrown) {
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
		range = JedCore.plugin.getConfig().getInt("Abilities.Earth.EarthShard.PrepareRange");
		abilityRange = JedCore.plugin.getConfig().getInt("Abilities.Earth.EarthShard.AbilityRange");
		normalDmg = JedCore.plugin.getConfig().getInt("Abilities.Earth.EarthShard.Damage.Normal");
		metalDmg = JedCore.plugin.getConfig().getInt("Abilities.Earth.EarthShard.Damage.Metal");
		maxShards = JedCore.plugin.getConfig().getInt("Abilities.Earth.EarthShard.MaxShards");
		cooldown = JedCore.plugin.getConfig().getLong("Abilities.Earth.EarthShard.Cooldown");
	}

	public void select() {
		raiseEarthBlock(getEarthSourceBlock(range));
	}

	@SuppressWarnings("deprecation")
	public void raiseEarthBlock(Block block) {
		if (block == null) {
			revertBlocks();
			return;
		}

		if (tblockTracker.size() >= maxShards) {
			return;
		}
		
		for (int i = 1; i < 4; i++) {
			if (!isTransparent(block.getRelative(BlockFace.UP, i))) {
				return;
			}
		}

		if (isEarthbendable(block)) {
			if (isMetal(block))
				playMetalbendingSound(block.getLocation());
			else {
				ParticleEffect.BLOCK_CRACK.display(new BlockData(block.getType(), block.getData()), 0, 0, 0, 0, 20, block.getLocation().add(0, 1, 0), 20);
				playEarthbendingSound(block.getLocation());
			}

			Material material = getCorrectType(block);
			byte data = block.getData();

			if (EarthPassive.isPassiveSand(block)) {
				EarthPassive.revertSand(block);
			}

			Location loc = block.getLocation().add(0.5, 0, 0.5);
			new TempFallingBlock(loc, material, data, new Vector(0, 0.8, 0), this);
			TempBlock tb = new TempBlock(block, Material.AIR, (byte) 0);
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
		if (!bPlayer.canBendIgnoreCooldowns(this)) {
			remove();
			return;
		}
		if (isPreparing) {
			if (tblockTracker.isEmpty()) {
				remove();
				return;
			}
			for (TempFallingBlock tfb : TempFallingBlock.getFromAbility(this)) {
				FallingBlock fb = tfb.getFallingBlock();
				if (fb.isDead()) {
					TempBlock tb = new TempBlock(fb.getLocation().getBlock(), fb.getMaterial(), fb.getBlockData());
					readyBlocksTracker.add(tb);
					tfb.remove();
				}
				if (fb.getLocation().getBlockY() == origin.getBlockY() + 2) {
					TempBlock tb = new TempBlock(fb.getLocation().getBlock(), fb.getMaterial(), fb.getBlockData());
					readyBlocksTracker.add(tb);
					tfb.remove();
				}
			}
		}

		if (isThrown) {

			for (TempFallingBlock tfb : TempFallingBlock.getFromAbility(this)) {
				FallingBlock fb = tfb.getFallingBlock();
				for (Entity e : GeneralMethods.getEntitiesAroundPoint(fb.getLocation(), 2)) {
					if (e instanceof LivingEntity && e.getEntityId() != player.getEntityId()) {
						DamageHandler.damageEntity(e, isMetal(fb.getMaterial()) ? metalDmg : normalDmg, this);
						((LivingEntity) e).setNoDamageTicks(0);
						ParticleEffect.BLOCK_CRACK.display(new BlockData(fb.getMaterial(), fb.getBlockData()), 0, 0, 0, 0, 20, fb.getLocation(), 20);
						tfb.remove();
					}
				}
			}
			if (TempFallingBlock.getFromAbility(this).isEmpty()) {
				remove();
				return;
			}
		}
		return;
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
		if (!isPreparing || isThrown || tblockTracker.size() > readyBlocksTracker.size())
			return;

		Location tloc = GeneralMethods.getTargetedLocation(player, abilityRange);

		if (GeneralMethods.getTargetedEntity(player, abilityRange, new ArrayList<Entity>()) != null)
			tloc = GeneralMethods.getTargetedEntity(player, abilityRange, new ArrayList<Entity>()).getLocation();

		isPreparing = false;

		Vector vel = null;
		for (TempBlock tb : readyBlocksTracker) {
			Location target = player.getTargetBlock((HashSet<Material>) null, (int) 30).getLocation();
			if (target.getBlockX() == tb.getBlock().getX() && target.getBlockY() == tb.getBlock().getY() && target.getBlockZ() == tb.getBlock().getZ()) {
				vel = player.getEyeLocation().getDirection().multiply(2).add(new Vector(0, 0.2, 0));
				break;
			}
			vel = GeneralMethods.getDirection(tb.getLocation(), tloc).normalize().multiply(2).add(new Vector(0, 0.2, 0));
		}
		for (TempBlock tb : readyBlocksTracker) {
			new TempFallingBlock(tb.getLocation(), tb.getBlock().getType(), tb.getBlock().getData(), vel, this);
			tb.revertBlock();
		}

		revertBlocks();

		isThrown = true;

		if (player.isOnline())
			bPlayer.addCooldown(this);
	}

	public void removeDeadBlocks() {
		for (TempFallingBlock tfb : TempFallingBlock.getFromAbility(this)) {
			tfb.remove();
		}
	}

	public void revertBlocks() {
		for (TempBlock tb : tblockTracker) {
			tb.revertBlock();
		}

		//for (FallingBlock fb : fblockTracker) {
		//	fb.remove();
		//}

		for (TempBlock tb : readyBlocksTracker) {
			tb.revertBlock();
		}

		tblockTracker.clear();
		//fblockTracker.clear();
		readyBlocksTracker.clear();
	}

	@Override
	public void remove() {
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
		return "* JedCore Addon *\n" + JedCore.plugin.getConfig().getString("Abilities.Earth.EarthShard.Description");
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
		return JedCore.plugin.getConfig().getBoolean("Abilities.Earth.EarthShard.Enabled");
	}
}