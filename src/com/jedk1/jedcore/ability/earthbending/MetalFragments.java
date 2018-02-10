package com.jedk1.jedcore.ability.earthbending;

import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.configuration.JedCoreConfig;
import com.jedk1.jedcore.util.TempFallingBlock;
import com.jedk1.jedcore.util.VersionUtil;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.MetalAbility;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TempBlock;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class MetalFragments extends MetalAbility implements AddonAbility {

	private int maxSources;
	private int selectRange;
	private int maxFragments;
	private double damage;
	private long cooldown;

	public List<Block> sources = new ArrayList<Block>();
	private List<Item> thrownFragments = new ArrayList<Item>();
	private List<TempBlock> tblockTracker = new ArrayList<TempBlock>();
	//private List<FallingBlock> fblockTracker = new ArrayList<FallingBlock>();
	private HashMap<Block, Integer> counters = new HashMap<Block, Integer>();

	public MetalFragments(Player player) {
		super(player);
		
		if (hasAbility(player, MetalFragments.class)) {
			MetalFragments.selectAnotherSource(player);
			return;
		}

		if (!bPlayer.canBend(this) || !bPlayer.canMetalbend()) {
			return;
		}
		
		setFields();

		if (tblockTracker.size() >= maxSources) {
			return;
		}

		if (prepare()) {
			Block b = selectSource();
                        if (GeneralMethods.isRegionProtectedFromBuild(player, "MetalFragments", b.getLocation())) {
                            return;
			}
			translateUpward(b);

			start();
		}
	}
	
	public void setFields() {
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);
		
		maxSources = config.getInt("Abilities.Earth.MetalFragments.MaxSources");
		selectRange = config.getInt("Abilities.Earth.MetalFragments.SourceRange");
		maxFragments = config.getInt("Abilities.Earth.MetalFragments.MaxFragments");
		damage = config.getDouble("Abilities.Earth.MetalFragments.Damage");
		cooldown = config.getInt("Abilities.Earth.MetalFragments.Cooldown");
	}

	public static void shootFragment(Player player, boolean left) {
		if (hasAbility(player, MetalFragments.class)) {
			((MetalFragments) getAbility(player, MetalFragments.class)).shootFragment(left);
		}
	}

	@SuppressWarnings("deprecation")
	private void shootFragment(boolean left) {
		if (sources.size() <= 0)
			return;

		Random randy = new Random();
		int i = randy.nextInt(sources.size());
		Block source = sources.get(i);
		ItemStack is = null;

		switch (source.getType()) {
			case IRON_BLOCK:
				is = new ItemStack(Material.IRON_INGOT, 1);
				break;
			case GOLD_BLOCK:
				is = new ItemStack(Material.GOLD_INGOT, 1);
				break;
			case IRON_ORE:
				is = new ItemStack(Material.IRON_INGOT, 1);
				break;
			case GOLD_ORE:
				is = new ItemStack(Material.GOLD_INGOT, 1);
				break;
			case COAL_BLOCK:
				is = new ItemStack(Material.COAL, 1);
				break;
			case COAL_ORE:
				is = new ItemStack(Material.COAL_ORE, 1);
				break;
			default:
				is = new ItemStack(Material.IRON_INGOT, 1);
				break;
		}

		Vector direction;
		if (GeneralMethods.getTargetedEntity(player, 30, new ArrayList<Entity>()) != null) {
			direction = GeneralMethods.getDirection(source.getLocation(), GeneralMethods.getTargetedEntity(player, 30, new ArrayList<Entity>()).getLocation());
		} else {
			direction = GeneralMethods.getDirection(source.getLocation(), VersionUtil.getTargetedLocation(player, 30));
		}

		Item ii = player.getWorld().dropItemNaturally(source.getLocation().getBlock().getRelative(GeneralMethods.getCardinalDirection(direction)).getLocation(), is);
		ii.setPickupDelay(Integer.MAX_VALUE);
		ii.setVelocity(direction.multiply(2).normalize());
		playMetalbendingSound(ii.getLocation());
		thrownFragments.add(ii);

		if (counters.containsKey(source)) {
			int count = counters.get(source);
			count++;
			if (count >= maxFragments) {
				counters.remove(source);
				source.getWorld().spawnFallingBlock(source.getLocation().add(0.5, 0, 0.5), source.getType(), source.getData());
				TempBlock tempBlock = TempBlock.get(source);
				if (tempBlock != null) {
					tempBlock.revertBlock();
				}
				sources.remove(source);
				source.getWorld().playSound(source.getLocation(), Sound.ENTITY_ITEM_BREAK, 10, 5);
			} else {
				counters.put(source, count);
			}

			if (sources.size() == 0) {
				remove();
				return;
			}
		}
	}

	public static void selectAnotherSource(Player player) {
		if (hasAbility(player, MetalFragments.class)) {
			((MetalFragments) getAbility(player, MetalFragments.class)).selectAnotherSource();
		}
	}

	private void selectAnotherSource() {
		if (tblockTracker.size() >= maxSources)
			return;

		if (prepare()) {
			Block b = selectSource();
			translateUpward(b);
		}
	}

	public boolean prepare() {
		Block block = BlockSource.getEarthSourceBlock(player, selectRange, ClickType.SHIFT_DOWN);

		if (block == null)
			return false;

		if (isMetal(block))
			return true;

		return false;
	}

	public Block selectSource() {
		Block block = BlockSource.getEarthSourceBlock(player, selectRange, ClickType.SHIFT_DOWN);
		if (isMetal(block))
			return block;
		return null;
	}

	@SuppressWarnings("deprecation")
	public void translateUpward(Block block) {
		if (block == null)
			return;

		if (sources.contains(block))
			return;

		if (block.getRelative(BlockFace.UP).getType().isSolid())
			return;

		if (isEarthbendable(player, block)) {
			new TempFallingBlock(block.getLocation().add(0.5, 0, 0.5), block.getType(), block.getData(), new Vector(0, 0.8, 0), this);
			block.setType(Material.AIR);

			playMetalbendingSound(block.getLocation());
		}
	}

	@SuppressWarnings("deprecation")
	public void progress() {
		if (player == null || player.isDead() || !player.isOnline()) {
			remove();
			return;
		}
		if (!hasAbility(player, MetalFragments.class)) {
			return;
		}
		if (!bPlayer.canBendIgnoreCooldowns(this)) {
			remove();
			return;
		}

		for (int i = 0; i < tblockTracker.size(); i++) {
			TempBlock tb = tblockTracker.get(i);
			if (player.getLocation().distance(tb.getLocation()) >= 10) {
				player.getWorld().spawnFallingBlock(tb.getLocation(), tb.getBlock().getType(), tb.getBlock().getData());
				tb.revertBlock();
				tblockTracker.remove(i);
				sources.clear();

				for (TempBlock b : tblockTracker) {
					sources.add(b.getBlock());
				}
			}
		}

		for (TempFallingBlock tfb : TempFallingBlock.getFromAbility(this)) {
			FallingBlock fb = tfb.getFallingBlock();
			if (fb.getLocation().getY() >= player.getEyeLocation().getY() + 1) {
				TempBlock tb = new TempBlock(fb.getLocation().getBlock(), fb.getMaterial(), fb.getBlockData());
				tblockTracker.add(tb);
				sources.add(tb.getBlock());
				counters.put(tb.getBlock(), 0);
				tfb.remove();
			}

			if (fb.isOnGround())
				fb.getLocation().getBlock().setType(fb.getMaterial());
		}

		for (Item f : thrownFragments) {
			if (f.isOnGround())
				f.remove();

			if (f.isDead())
				continue;

			for (Entity e : GeneralMethods.getEntitiesAroundPoint(f.getLocation(), 3)) {
				if (e instanceof LivingEntity && e.getEntityId() != player.getEntityId()) {
					DamageHandler.damageEntity(e, damage, this);
					f.remove();
				}
			}
		}

		//removeDeadFBlocks();
		return;
	}

	/*
	public void removeDeadFBlocks() {
		for (int i = 0; i < fblockTracker.size(); i++)
			if (fblockTracker.get(i).isDead())
				fblockTracker.remove(i);
	}
	*/

	public void removeDeadItems() {
		for (int i = 0; i < thrownFragments.size(); i++)
			if (thrownFragments.get(i).isDead())
				thrownFragments.remove(i);
	}

	@SuppressWarnings("deprecation")
	public void dropSources() {
		for (TempBlock tb : tblockTracker) {
			tb.getBlock().getWorld().spawnFallingBlock(tb.getLocation(), tb.getBlock().getType(), tb.getBlock().getData());
			tb.revertBlock();
		}

		tblockTracker.clear();
	}

	public void removeFragments() {
		for (Item i : thrownFragments) {
			i.remove();
		}
		thrownFragments.clear();
	}

	public static void remove(Player player, Block block) {
		if (hasAbility(player, MetalFragments.class)) {
			MetalFragments mf = (MetalFragments) getAbility(player, MetalFragments.class);
			if (mf.sources.contains(block)) {
				mf.remove();
			}
		}
	}

	@Override
	public void remove() {
		dropSources();
		removeFragments();
		removeDeadItems();
		if (player.isOnline()) {
			bPlayer.addCooldown(this);
		}
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
		return "MetalFragments";
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
		return "* JedCore Addon *\n" + config.getString("Abilities.Earth.MetalFragments.Description");
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
		return config.getBoolean("Abilities.Earth.MetalFragments.Enabled");
	}
}
