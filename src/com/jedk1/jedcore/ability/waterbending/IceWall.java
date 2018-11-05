package com.jedk1.jedcore.ability.waterbending;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import com.jedk1.jedcore.configuration.JedCoreConfig;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.jedk1.jedcore.JedCore;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.IceAbility;
import com.projectkorra.projectkorra.airbending.AirBlast;
import com.projectkorra.projectkorra.earthbending.EarthSmash;
import com.projectkorra.projectkorra.firebending.FireBlast;
import com.projectkorra.projectkorra.firebending.FireBlastCharged;
import com.projectkorra.projectkorra.firebending.lightning.Lightning;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.Torrent;
import com.projectkorra.projectkorra.waterbending.ice.IceBlast;

public class IceWall extends IceAbility implements AddonAbility {

	public static List<IceWall> instances = new ArrayList<IceWall>();
	private int maxHeight;
	private int minHeight;
	private int width;
	private int range;
	private int maxHealth;
	private int minHealth;
	private double damage;
	private long cooldown;

	public static boolean stackable;

	public static boolean lifetimeEnabled;
	public static long lifetimeTime;

	public int torrentDamage;
	public int torrentFreezeDamage;
	public int iceblastDamage;
	public int fireblastDamage;
	public int fireblastChargedDamage;
	public int lightningDamage;
	public int combustionDamage;
	public int earthSmashDamage;
	public int airBlastDamage;

	public boolean isWallDoneFor = false;
	public List<Block> affectedBlocks = new ArrayList<Block>();

	private boolean rising = false;
	private long lastDamageTime = 0;
	private long lifetime = 0;
	private int wallHealth;
	private int tankedDamage;
	private List<Block> lastBlocks = new ArrayList<Block>();
	private List<TempBlock> tempBlocks = new ArrayList<TempBlock>();

	Random rand = new Random();

	public IceWall(Player player) {
		super(player);
		if (!bPlayer.canBendIgnoreCooldowns(this) || !bPlayer.canIcebend()) {
			return;
		}

		setFields();
		Block b = getSourceBlock(player, (int) (range * getNightFactor(player.getWorld())));

		if (b == null)
			return;

		else {
			for (IceWall iw : instances) {
				if (iw.affectedBlocks.contains(b)) {
					iw.collapse(player, false);
					return;
				}
			}

			if (isWaterbendable(b)) {
				if (!bPlayer.canBend(this)) {
					return;
				}

				wallHealth = (int) (((rand.nextInt((maxHealth - minHealth) + 1)) + minHealth) * getNightFactor(player.getWorld()));
				loadAffectedBlocks(player, b);
				lifetime = System.currentTimeMillis() + lifetimeTime;
			}
		}
		start();
	}

	public void setFields() {
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);

		setMaxHeight(config.getInt("Abilities.Water.IceWall.MaxHeight"));
		setMinHeight(config.getInt("Abilities.Water.IceWall.MinHeight"));
		setWidth(config.getInt("Abilities.Water.IceWall.Width"));
		range = config.getInt("Abilities.Water.IceWall.Range");
		maxHealth = config.getInt("Abilities.Water.IceWall.MaxWallHealth");
		minHealth = config.getInt("Abilities.Water.IceWall.MinWallHealth");
		damage = config.getDouble("Abilities.Water.IceWall.Damage");
		cooldown = config.getLong("Abilities.Water.IceWall.Cooldown");
		stackable = config.getBoolean("Abilities.Water.IceWall.Stackable");
		lifetimeEnabled = config.getBoolean("Abilities.Water.IceWall.LifeTime.Enabled");
		lifetimeTime = config.getLong("Abilities.Water.IceWall.LifeTime.Duration");
		torrentDamage = config.getInt("Abilities.Water.IceWall.WallDamage.Torrent");
		torrentFreezeDamage = config.getInt("Abilities.Water.IceWall.WallDamage.TorrentFreeze");
		iceblastDamage = config.getInt("Abilities.Water.IceWall.WallDamage.IceBlast");
		fireblastDamage = config.getInt("Abilities.Water.IceWall.WallDamage.Fireblast");
		fireblastChargedDamage = config.getInt("Abilities.Water.IceWall.WallDamage.FireblastCharged");
		lightningDamage = config.getInt("Abilities.Water.IceWall.WallDamage.Lightning");
		combustionDamage = config.getInt("Abilities.Water.IceWall.WallDamage.Combustion");
		earthSmashDamage = config.getInt("Abilities.Water.IceWall.WallDamage.EarthSmash");
		airBlastDamage = config.getInt("Abilities.Water.IceWall.WallDamage.AirBlast");
	}

	public Block getSourceBlock(Player player, int range) {
		Vector direction = player.getEyeLocation().getDirection().normalize();

		for (int i = 0; i <= range; i++) {
			Block b = player.getEyeLocation().add(direction.clone().multiply((double) i)).getBlock();

			if (b.getType() == Material.WATER || b.getType() == Material.ICE || b.getType() == Material.PACKED_ICE
					//|| b.getType() == Material.SNOW
					|| b.getType() == Material.SNOW_BLOCK)
				return b;
		}

		return null;
	}

	public boolean isBendable(Block b) {
		if (b.getType() == Material.WATER || b.getType() == Material.ICE || b.getType() == Material.PACKED_ICE
				//|| b.getType() == Material.SNOW
				|| b.getType() == Material.SNOW_BLOCK)
			return true;

		return false;
	}

	public void loadAffectedBlocks(Player player, Block block) {
		Vector direction = player.getEyeLocation().getDirection().normalize();

		double ox, oy, oz;
		ox = -direction.getZ();
		oy = 0;
		oz = direction.getX();

		Vector orth = new Vector(ox, oy, oz);
		orth = orth.normalize();

		Location origin = block.getLocation();

		World world = origin.getWorld();

		int width = (int) (getWidth() * getNightFactor(world));
		int minHeight = (int) (getMinHeight() * getNightFactor(world));
		int maxHeight = (int) (getMaxHeight() * getNightFactor(world));

		int height = minHeight;
		boolean increasingHeight = true;
		for (int i = -(width / 2); i < width / 2; i++) {
			Block b = world.getBlockAt(origin.clone().add(orth.clone().multiply((double) i)));

			if (b.getType() == Material.AIR) {
				while (b.getType() == Material.AIR) {
					if (b.getY() < 0)
						return;

					b = b.getRelative(BlockFace.DOWN);
				}
			}

			if (b.getRelative(BlockFace.UP).getType() != Material.AIR) {
				while (b.getRelative(BlockFace.UP).getType() != Material.AIR) {
					if (b.getY() > b.getWorld().getMaxHeight())
						return;

					b = b.getRelative(BlockFace.UP);
				}
			}

			if (!stackable && isIceWallBlock(b)) {
				continue;
			}

			if (isBendable(b)) {
				affectedBlocks.add(b);
				for (int h = 1; h <= height; h++) {
					Block up = b.getRelative(BlockFace.UP, h);
					if (up.getType() == Material.AIR) {
						affectedBlocks.add(up);
					}
				}

				if (height < maxHeight && increasingHeight)
					height++;

				if (i == 0)
					increasingHeight = false;

				if (!increasingHeight && height > minHeight)
					height--;

				lastBlocks.add(b);
			}

		}

		bPlayer.addCooldown(this);
		rising = true;
		instances.add(this);
	}

	@Override
	public void progress() {
		if (rising) {
			if (lastBlocks.isEmpty()) {
				rising = false;
				return;
			}

			List<Block> theseBlocks = new ArrayList<Block>(lastBlocks);

			lastBlocks.clear();

			for (Block b : theseBlocks) {
				TempBlock tb = new TempBlock(b, Material.ICE, Material.ICE.createBlockData());
				tempBlocks.add(tb);

				playIcebendingSound(b.getLocation());

				Block up = b.getRelative(BlockFace.UP);

				if (affectedBlocks.contains(up))
					lastBlocks.add(up);
			}
		}

		if (System.currentTimeMillis() > lifetime && lifetimeEnabled) {
			collapse(player, false);
		}
	}

	public void damageWall(Player player, int damage) {
		long noDamageTicks = 1000;
		if (System.currentTimeMillis() < lastDamageTime + noDamageTicks)
			return;

		lastDamageTime = System.currentTimeMillis();
		tankedDamage += damage;
		if (tankedDamage >= wallHealth) {
			collapse(player, true);
		}
	}

	public void collapse(Player player, boolean forceful) {
		if (rising)
			return;

		remove(player, forceful);
	}

	public void remove(Player player, boolean forceful) {
		for (TempBlock tb : tempBlocks) {
			if (tb != null) {
				tb.revertBlock();

				ParticleEffect.BLOCK_CRACK.display(tb.getLocation(), 5, 0, 0, 0, 0, Material.PACKED_ICE.createBlockData());
				tb.getLocation().getWorld().playSound(tb.getLocation(), Sound.BLOCK_GLASS_BREAK, 5f, 5f);

				for (Entity e : GeneralMethods.getEntitiesAroundPoint(tb.getLocation(), 2.5)) {
					if (e.getEntityId() != player.getEntityId()) {
						if (e instanceof LivingEntity) {
							DamageHandler.damageEntity(e, damage * getNightFactor(player.getWorld()), this);
							if (forceful) {
								((LivingEntity) e).setNoDamageTicks(0);
							}
						}
					}
				}
			}
		}

		tempBlocks.clear();

		isWallDoneFor = true;
		remove();
	}

	public void remove() {
		for (TempBlock tb : tempBlocks) {
			if (tb != null) {
				tb.revertBlock();
			}
		}

		tempBlocks.clear();

		instances.remove(this);
		super.remove();
	}

	public static void removeDeadInstances() {
		for (int i = 0; i < instances.size(); i++) {
			IceWall iw = instances.get(i);
			if (iw.isWallDoneFor) {
				instances.remove(i);
			}
		}
	}

	public static void collisionDamage(Entity entity, double travelledDistance, Vector difference, Player instigator) {
		for (IceWall iw : IceWall.instances) {
			for (Block b : iw.affectedBlocks) {
				if (entity.getLocation().getWorld() == b.getLocation().getWorld() && entity.getLocation().distance(b.getLocation()) < 2) {
					double damage = ((travelledDistance - 5.0) < 0 ? 0 : travelledDistance - 5.0) / (difference.length());
					iw.damageWall(instigator, (int) damage);
				}
			}
		}
	}

	public static boolean checkExplosions(Location location, Entity entity) {
		for (IceWall iw : IceWall.instances) {
			for (Block b : iw.affectedBlocks) {
				if (location.getWorld() == b.getLocation().getWorld() && location.distance(b.getLocation()) < 2) {

					for (Entity e : GeneralMethods.getEntitiesAroundPoint(location, 3)) {
						if (e instanceof LivingEntity) {
							((LivingEntity) e).damage(7, entity);
						}
					}
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isIceWallBlock(Block block) {
		for (IceWall iw : IceWall.instances) {
			if (iw.affectedBlocks.contains(block)) {
				return true;
			}
		}
		return false;
	}

	public static void removeAll() {
		for (int i = 0; i < instances.size(); i++) {
			IceWall iw = instances.get(i);
			iw.remove();
		}
	}

	@SuppressWarnings("deprecation")
	public static void progressAll() {
		/*
		for (IceWall iw : IceWall.instances) {
			iw.progress();
		}
		*/

		ListIterator<IceWall> iwli = IceWall.instances.listIterator();

		while (iwli.hasNext()) {
			IceWall iw = iwli.next();
			for (Torrent t : getAbilities(Torrent.class)) {
				if (t.getLocation() == null) continue;
				for (int i = 0; i < t.getLaunchedBlocks().size(); i++) {
					TempBlock tb = t.getLaunchedBlocks().get(i);

					for (Block ice : iw.affectedBlocks) {
						if (ice.getLocation().getWorld() == tb.getLocation().getWorld() && ice.getLocation().distance(tb.getLocation()) <= 2) {
							if (t.isFreeze())
								iw.damageWall(t.getPlayer(), (int) (iw.torrentFreezeDamage * getNightFactor(ice.getWorld())));
							else
								iw.damageWall(t.getPlayer(), (int) (iw.torrentDamage * getNightFactor(ice.getWorld())));

							if (!iw.isWallDoneFor)
								t.setFreeze(false);
						}
					}
				}
			}

			for (IceBlast ib : getAbilities(IceBlast.class)) {
				if (ib.getLocation() == null) continue;
				for (Block ice : iw.affectedBlocks) {
					if (ib.source == null)
						break;

					if (ice.getLocation().getWorld() == ib.source.getLocation().getWorld() && ice.getLocation().distance(ib.source.getLocation()) <= 2) {
						iw.damageWall(ib.getPlayer(), (int) (iw.iceblastDamage * getNightFactor(ice.getWorld())));

						if (!iw.isWallDoneFor)
							ib.remove();
					}
				}
			}

			for (FireBlastCharged fb : getAbilities(FireBlastCharged.class)) {
				if (fb.getLocation() == null) continue;
				for (Block ice : iw.affectedBlocks) { 
					if (ice.getLocation().getWorld() == fb.getLocation().getWorld() && fb.getLocation().distance(ice.getLocation()) <= 1.5) {
						iw.damageWall(fb.getPlayer(), iw.fireblastChargedDamage);

						if (!iw.isWallDoneFor) fb.remove();; 
					} 
				}
			}
			
			for (FireBlast fb : getAbilities(FireBlast.class)) {
				if (fb.getLocation() == null) continue;
				for (Block ice : iw.affectedBlocks) { 
					if (ice.getLocation().getWorld() == fb.getLocation().getWorld() && fb.getLocation().distance(ice.getLocation()) <= 1.5) {
						iw.damageWall(fb.getPlayer(), iw.fireblastDamage);

						if (!iw.isWallDoneFor) fb.remove();; 
					} 
				}
			}

			for (EarthSmash es : getAbilities(EarthSmash.class)) {
				if (es.getLocation() == null) continue;
				for (Block ice : iw.affectedBlocks) {
					if (es.getState() == EarthSmash.State.SHOT) {
						for (int j = 0; j < es.getBlocks().size(); j++) {
							Block b = es.getBlocks().get(j);
							if (ice.getLocation().getWorld() == b.getLocation().getWorld() && b.getLocation().distance(ice.getLocation()) <= 2) {
								iw.damageWall(es.getPlayer(), iw.earthSmashDamage);

								if (!iw.isWallDoneFor) {
									for (Block block : es.getBlocksIncludingInner()) {
										if (block != null && block.getType() != Material.AIR) {
											ParticleEffect.BLOCK_CRACK.display(block.getLocation(), 5, 0, 0, 0, 0, block.getBlockData().clone());
										}
									}
									es.remove();
								}
							}
						}
					}
				}
			}

			for (Lightning l : getAbilities(Lightning.class)) {
				for (Lightning.Arc arc : l.getArcs()) {
					for (Block ice : iw.affectedBlocks) {
						for (Location loc : arc.getPoints()) {
							if (ice.getLocation().getWorld() == loc.getWorld() && loc.distance(ice.getLocation()) <= 1.5) {
								iw.damageWall(l.getPlayer(), (int) (FireAbility.getDayFactor(iw.lightningDamage, ice.getWorld())));

								if (!iw.isWallDoneFor)
									l.remove();
							}
						}
					}
				}
			}

			for (AirBlast ab : getAbilities(AirBlast.class)) {
				if (ab.getLocation() == null) continue;
				for (Block ice : iw.affectedBlocks) { 
					if (ice.getLocation().getWorld() == ab.getLocation().getWorld() && ab.getLocation().distance(ice.getLocation()) <= 1.5) {
						iw.damageWall(ab.getPlayer(), iw.airBlastDamage);

						if (!iw.isWallDoneFor) ab.remove();
					} 
				}
			}

			for (CoreAbility ca : getAbilities(getAbility("Combustion").getClass())) {
				if (ca.getLocation() == null) continue;
				for (Block ice : iw.affectedBlocks) { 
					if (ice.getLocation().getWorld() == ca.getLocation().getWorld() && ca.getLocation().distance(ice.getLocation()) <= 1.5) {
						iw.damageWall(ca.getPlayer(), iw.combustionDamage);
						if (!iw.isWallDoneFor) ca.remove();
					} 
				}
			}
		}

		IceWall.removeDeadInstances();
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
		return "IceWall";
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
		return "* JedCore Addon *\n" + config.getString("Abilities.Water.IceWall.Description");
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
		return config.getBoolean("Abilities.Water.IceWall.Enabled");
	}

	public int getMaxHeight() {
		return maxHeight;
	}

	public void setMaxHeight(int maxHeight) {
		this.maxHeight = maxHeight;
	}

	public int getMinHeight() {
		return minHeight;
	}

	public void setMinHeight(int minHeight) {
		this.minHeight = minHeight;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}
}
