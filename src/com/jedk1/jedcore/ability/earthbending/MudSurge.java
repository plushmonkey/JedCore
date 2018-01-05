package com.jedk1.jedcore.ability.earthbending;

import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.configuration.JedCoreConfig;
import com.jedk1.jedcore.policies.removal.*;
import com.jedk1.jedcore.util.TempFallingBlock;
import com.jedk1.jedcore.util.VersionUtil;
import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TempBlock;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

public class MudSurge extends EarthAbility implements AddonAbility {
	private int prepareRange;
	private int blindChance;
	private int blindTicks;
	private boolean multipleHits;
	private double damage;
	private int waves;
	private int waterSearchRadius;
	private boolean wetSource;
	private long cooldown;
	private double collisionRadius;

	public static int surgeInterval = 300;
	public static int mudPoolRadius = 2;
	public static long mudCreationInterval = 100;
	public static Material[] mudTypes = new Material[] { Material.SAND, Material.CLAY, Material.STAINED_CLAY, Material.GRASS, Material.DIRT, Material.MYCEL, Material.SOUL_SAND, Material.RED_SANDSTONE, Material.SANDSTONE };

	private CompositeRemovalPolicy removalPolicy;

	private Block source;
	private TempBlock sourceTB;

	private int wavesOnTheRun = 0;
	private long lastSurgeTime = 0;
	private boolean mudFormed = false;
	private boolean doNotSurge = false;
	public boolean started = false;

	private List<Block> mudArea = new ArrayList<>();
	private ListIterator<Block> mudAreaItr;
	private List<TempBlock> mudBlocks = new ArrayList<>();
	private List<Player> blind = new ArrayList<>();
	private List<Entity> affectedEntities = new ArrayList<>();

	private List<TempFallingBlock> fallingBlocks = new ArrayList<>();
	
	private Random rand = new Random();

	public MudSurge(Player player) {
		super(player);

		if (!bPlayer.canBend(this)) {
			return;
		}

		if (hasAbility(player, MudSurge.class)) {
			MudSurge ms = getAbility(player, MudSurge.class);
			if (!ms.hasStarted()) {
				ms.remove();
			} else {
				return;
			}
		}

		this.removalPolicy = new CompositeRemovalPolicy(this,
				new CannotBendRemovalPolicy(this.bPlayer, this, true, true),
				new IsOfflineRemovalPolicy(this.player),
				new IsDeadRemovalPolicy(this.player),
				new OutOfRangeRemovalPolicy(this.player, 25.0, () -> {
					return this.source.getLocation();
				}),
				new SwappedSlotsRemovalPolicy<>(bPlayer, MudSurge.class)
		);
		
		setFields();

		if (getSource()) {
			loadMudPool();
			start();
		}
	}
	
	public void setFields() {
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);

		this.removalPolicy.load(config);
		
		prepareRange = config.getInt("Abilities.Earth.MudSurge.SourceRange");
		blindChance = config.getInt("Abilities.Earth.MudSurge.BlindChance");
		damage = config.getDouble("Abilities.Earth.MudSurge.Damage");
		waves = config.getInt("Abilities.Earth.MudSurge.Waves");
		waterSearchRadius = config.getInt("Abilities.Earth.MudSurge.WaterSearchRadius");
		wetSource = config.getBoolean("Abilities.Earth.MudSurge.WetSourceOnly");
		cooldown = config.getLong("Abilities.Earth.MudSurge.Cooldown");
		blindTicks = config.getInt("Abilities.Earth.MudSurge.BlindTicks");
		multipleHits = config.getBoolean("Abilities.Earth.MudSurge.MultipleHits");
		collisionRadius = config.getDouble("Abilities.Earth.MudSurge.CollisionRadius");
	}

	@Override
	public void progress() {
		if (removalPolicy.shouldRemove()) {
			remove();
			return;
		}

		if (mudFormed && started && System.currentTimeMillis() > lastSurgeTime + surgeInterval) {
			surge();
			affect();
			if (TempFallingBlock.getFromAbility(this).isEmpty()) {
				remove();
				return;
			}
			return;
		}

		if (!mudFormed) {
			createMudPool();
		}
	}

	private boolean getSource() {
		Block block = getMudSourceBlock(prepareRange);

		if (block != null) {
			if (isMud(block)) {
				boolean water = true;

				if (wetSource) {
					water = false;
					List<Block> nearby = GeneralMethods.getBlocksAroundPoint(block.getLocation(), waterSearchRadius);

					for (Block b : nearby) {
						if (b.getType() == Material.STATIONARY_WATER || b.getType() == Material.WATER) {
							water = true;
							break;
						}
					}
				}

				if (water) {
					this.source = block;
					this.sourceTB = new TempBlock(this.source, Material.STAINED_CLAY, (byte) 12);
					return true;
				}
			}
		}

		return false;
	}

	private void startSurge() {
		started = true;
		this.bPlayer.addCooldown(this);

		// Clear out the policies that only apply while sourcing.
		this.removalPolicy.removePolicyType(IsDeadRemovalPolicy.class);
		this.removalPolicy.removePolicyType(OutOfRangeRemovalPolicy.class);
		this.removalPolicy.removePolicyType(SwappedSlotsRemovalPolicy.class);
	}

	private boolean hasStarted() {
		return this.started;
	}

	public static boolean isSurgeBlock(Block block) {
		if (block.getType() != Material.STAINED_CLAY || block.getData() != 12) {
			return false;
		}

		for (MudSurge surge : CoreAbility.getAbilities(MudSurge.class)) {
			if (surge.mudArea.contains(block)) {
				return true;
			}
		}

		return false;
	}

	// Returns true if the event should be cancelled.
	public static boolean onFallDamage(Player player) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null || !bPlayer.hasElement(Element.EARTH)) {
			return false;
		}

		ConfigurationSection config = JedCoreConfig.getConfig(player);

		boolean fallDamage = config.getBoolean("Abilities.Earth.MudSurge.AllowFallDamage");
		if (fallDamage) {
			return false;
		}

		Block block = player.getLocation().clone().subtract(0, 0.1, 0).getBlock();
		return isSurgeBlock(block);
	}

	public static void mudSurge(Player player) {
		if (!hasAbility(player, MudSurge.class))
			return;

		getAbility(player, MudSurge.class).startSurge();
	}

	private Block getMudSourceBlock(int range) {
		Block testBlock = VersionUtil.getTargetedLocationTransparent(player, range).getBlock();
		if (isMud(testBlock))
			return testBlock;

		Location loc = player.getEyeLocation();
		Vector dir = player.getEyeLocation().getDirection().clone().normalize();

		for (int i = 0; i <= range; i++) {
			Block block = loc.clone().add(dir.clone().multiply(i == 0 ? 1 : i)).getBlock();
			if (GeneralMethods.isRegionProtectedFromBuild(player, "MudSurge", block.getLocation()))
				continue;

			if (isMud(block))
				return block;
		}

		return null;
	}

	private boolean isMud(Block block) {
		for (Material mat : mudTypes) {
			if (mat.name().equalsIgnoreCase(block.getType().name()))
				return true;
		}

		return false;
	}

	private void createMud(Block block) {
		mudBlocks.add(new TempBlock(block, Material.STAINED_CLAY, (byte) 12));
	}

	private void loadMudPool() {
		List<Location> area = GeneralMethods.getCircle(source.getLocation(), mudPoolRadius, 3, false, true, 0);

		for (Location l : area) {
			Block b = l.getBlock();

			if (isMud(b)) {
				if (isTransparent(b.getRelative(BlockFace.UP))) {
					boolean water = true;

					if (wetSource) {
						water = false;
						List<Block> nearby = GeneralMethods.getBlocksAroundPoint(l, waterSearchRadius);

						for (Block block : nearby) {
							if (block.getType() == Material.STATIONARY_WATER || block.getType() == Material.WATER) {
								water = true;
								break;
							}
						}
					}

					if (water) {
						mudArea.add(b);
						playEarthbendingSound(b.getLocation());
					}
				}
			}
		}

		mudAreaItr = mudArea.listIterator();
	}

	private void createMudPool() {
		if (!mudAreaItr.hasNext()) {
			mudFormed = true;
			return;
		}

		Block b = mudAreaItr.next();

		if (b != null)
			createMud(b);
	}

	private void revertMudPool() {
		for (TempBlock tb : mudBlocks)
			tb.revertBlock();

		mudBlocks.clear();
	}

	private void surge() {
		if (wavesOnTheRun >= waves) {
			doNotSurge = true;
			return;
		}

		if (doNotSurge)
			return;

		for (TempBlock tb : mudBlocks) {
			Vector direction = GeneralMethods.getDirection(tb.getLocation().add(0, 1, 0), VersionUtil.getTargetedLocation(player, 30)).multiply(0.07);

			double x = rand.nextDouble() / 5;
			double z = rand.nextDouble() / 5;

			x = (rand.nextBoolean()) ? -x : x;
			z = (rand.nextBoolean()) ? -z : z;

			fallingBlocks.add(new TempFallingBlock(tb.getLocation().add(0, 1, 0), Material.STAINED_CLAY, (byte) 12, direction.clone().add(new Vector(x, 0.2, z)), this));
			
			playEarthbendingSound(tb.getLocation());
		}

		wavesOnTheRun++;
	}

	private void affect() {
		for (TempFallingBlock tfb : TempFallingBlock.getFromAbility(this)) {
			FallingBlock fb = tfb.getFallingBlock();
			if (fb.isDead()) {
				tfb.remove();
				continue;
			}

			for (Entity e : GeneralMethods.getEntitiesAroundPoint(fb.getLocation(), 1.5)) {
				if (fb.isDead()) {
					tfb.remove();
					continue;
				}

				if (e instanceof LivingEntity) {
					if (this.multipleHits || !this.affectedEntities.contains(e)) {
						DamageHandler.damageEntity(e, damage, this);
						if (!this.multipleHits) {
							this.affectedEntities.add(e);
						}
					}

					if (e instanceof Player) {
						if (e.getEntityId() == player.getEntityId())
							continue;

						if (rand.nextInt(100) < blindChance && !blind.contains(e)) {
							((Player) e).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, this.blindTicks, 2));
						}

						blind.add((Player) e);
					}

					e.setVelocity(fb.getVelocity().multiply(0.8));
					tfb.remove();
				}
			}
		}
	}

	@Override
	public void remove() {
		sourceTB.revertBlock();
		revertMudPool();
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
		if (collision.isRemovingFirst()) {
			Location location = collision.getLocationSecond();
			double radius = collision.getAbilitySecond().getCollisionRadius();

			// Loop through all falling blocks because the collision system stops on the first collision.
			for (Iterator<TempFallingBlock> iterator = fallingBlocks.iterator(); iterator.hasNext();) {
				TempFallingBlock tfb = iterator.next();

				// Check if this falling block is within collision radius
				if (tfb.getLocation().distanceSquared(location) <= radius * radius) {
					tfb.remove();
					iterator.remove();
				}
			}
		}
	}

	@Override
	public double getCollisionRadius() {
		return collisionRadius;
	}

	@Override
	public String getName() {
		return "MudSurge";
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
		return "* JedCore Addon *\n" + config.getString("Abilities.Earth.MudSurge.Description");
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
		return config.getBoolean("Abilities.Earth.MudSurge.Enabled");
	}
}
