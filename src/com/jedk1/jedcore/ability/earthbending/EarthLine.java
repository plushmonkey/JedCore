package com.jedk1.jedcore.ability.earthbending;

import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.configuration.JedCoreConfig;
import com.jedk1.jedcore.util.RegenTempBlock;
import com.jedk1.jedcore.util.TempFallingBlock;
import com.jedk1.jedcore.util.VersionUtil;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class EarthLine extends EarthAbility implements AddonAbility {

	private Location location;
	private Location endLocation;
	private Block sourceblock;
	private Material sourcetype;
	private boolean progressing;
	private boolean hitted;
	private int goOnAfterHit;

	private long cooldown;
	private double range;
	private double preparerange;
	private int affectingradius;
	private double damage;

	public EarthLine(Player player) {
		super(player);

		if (!bPlayer.canBend(this)) {
			return;
		}
		location = null;
		endLocation = null;
		sourceblock = null;
		sourcetype = null;
		progressing = false;
		goOnAfterHit = 1;

		setFields();
		if (prepare()) {
			bPlayer.addCooldown(this);
			start();
		}
	}
	
	public void setFields() {
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);
		
		cooldown = config.getLong("Abilities.Earth.EarthLine.Cooldown");
		range = config.getInt("Abilities.Earth.EarthLine.Range");
		preparerange = config.getInt("Abilities.Earth.EarthLine.PrepareRange");
		affectingradius = config.getInt("Abilities.Earth.EarthLine.AffectingRadius");
		damage = config.getDouble("Abilities.Earth.EarthLine.Damage");
	}

	public boolean prepare() {
		if (hasAbility(player, EarthLine.class)) {
			EarthLine el = (EarthLine) getAbility(player, EarthLine.class);
			if (!el.progressing) {
				el.remove();
			}
		}
		Block block = BlockSource.getEarthSourceBlock(player, 3, ClickType.SHIFT_DOWN);
		if (block != null) {
			sourceblock = block;
			focusBlock();
			return true;
		} else {
			return false;
		}
	}

	private void focusBlock() {
		if (sourceblock.getType() == Material.SAND) {
			if (VersionUtil.isPassiveSand(this.sourceblock)) {
				VersionUtil.revertSand(this.sourceblock);
				this.sourcetype = this.sourceblock.getType();
			} else {
				sourcetype = Material.SAND;
			}
			sourceblock.setType(Material.SANDSTONE);
		} else if (sourceblock.getType() == Material.STONE) {
			sourcetype = sourceblock.getType();
			sourceblock.setType(Material.COBBLESTONE);
		} else {
			sourcetype = sourceblock.getType();
			sourceblock.setType(Material.STONE);
		}
		location = sourceblock.getLocation();
	}

	private void breakSourceBlock() {
		sourceblock.setType(sourcetype);
		new RegenTempBlock(sourceblock, Material.AIR, (byte) 0, 5000L);
	}

	@Override
	public void remove() {
		sourceblock.setType(sourcetype);
		super.remove();
	}

	private static Location getTargetLocation(Player player) {
		ConfigurationSection config = JedCoreConfig.getConfig(player);
		double range = config.getInt("Abilities.Earth.EarthLine.Range");
		Entity target = GeneralMethods.getTargetedEntity(player, range, player.getNearbyEntities(range, range, range));
		Location location;
		if (target == null) {
			location = VersionUtil.getTargetedLocation(player, range);
		} else {
			location = ((LivingEntity) target).getEyeLocation();
		}
		return location;
	}

	public void shootline(Location endLocation) {
		this.endLocation = endLocation;
		progressing = true;
		breakSourceBlock();
		sourceblock.getWorld().playEffect(sourceblock.getLocation(), Effect.GHAST_SHOOT, 0, 10);
	}

	public static boolean shootLine(Player player) {
		if (hasAbility(player, EarthLine.class)) {
			EarthLine el = (EarthLine) getAbility(player, EarthLine.class);
			if (!el.progressing) {
				el.shootline(getTargetLocation(player));
				return true;
			}
		}
		return false;
	}

	public void progress() {
		if (!progressing)
			return;
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		}
		if (!bPlayer.canBendIgnoreCooldowns(this)) {
			remove();
			return;
		}
		if (sourceblock == null || GeneralMethods.isRegionProtectedFromBuild(player, "EarthBlast", location)) {
			remove();
			return;
		}
		if (player.getWorld() != sourceblock.getWorld()) {
			remove();
			return;
		}
		if (sourceblock.getLocation().distance(player.getLocation()) > preparerange) {
			remove();
			return;
		}
		if (player.isSneaking())
			endLocation = getTargetLocation(player);

		double x1 = endLocation.getX();
		double z1 = endLocation.getZ();
		double x0 = sourceblock.getX();
		double z0 = sourceblock.getZ();
		Vector looking = new Vector(x1 - x0, 0.0D, z1 - z0);
		Vector push = new Vector(x1 - x0, 0.34999999999999998D, z1 - z0);
		if (location.distance(sourceblock.getLocation()) < range) {
			if (VersionUtil.isPassiveSand(location.getBlock())) {
				VersionUtil.revertSand(location.getBlock());
			}

			Material cloneType = location.getBlock().getType();
			Location locationYUP = location.clone().add(0.0D, 0.1D, 0.0D);

			playEarthbendingSound(location);

			new RegenTempBlock(location.getBlock(), Material.AIR, (byte) 0, 700L);

			new TempFallingBlock(locationYUP, cloneType, (byte) 0, push, this);

			location.add(looking.normalize());
			if (location.clone().add(0.0D, 1.0D, 0.0D).getBlock().getType() != Material.AIR && !isTransparent(location.clone().add(0.0D, 1.0D, 0.0D).getBlock())) {
				location.add(0.0D, 1.0D, 0.0D);
				if (!isEarthbendable(player, location.getBlock()) || location.clone().add(0.0D, 1.0D, 0.0D).getBlock().getType() != Material.AIR && !isTransparent(location.clone().add(0.0D, 1.0D, 0.0D).getBlock())) {
					remove();
					return;
				}
			} else if ((location.clone().getBlock().getType() == Material.AIR || isTransparent(location.clone().add(0.0D, 1.0D, 0.0D).getBlock())) && location.clone().add(0.0D, -1D, 0.0D).getBlock().getType() != Material.AIR) {
				location.add(0.0D, -1D, 0.0D);
				if (!isEarthbendable(player, location.clone().getBlock()) || location.clone().add(0.0D, -1D, 0.0D).getBlock().getType() == Material.AIR) {
					remove();
					return;
				}
			}
			if (hitted) {
				if (goOnAfterHit != 0) {
					goOnAfterHit--;
				} else {
					remove();
					return;
				}
			} else {
				for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, affectingradius)) {
					if (GeneralMethods.isRegionProtectedFromBuild(player, "EarthLine", location))
						return;
					if ((entity instanceof LivingEntity) && entity.getEntityId() != player.getEntityId()) {
						entity.setVelocity(push.normalize().multiply(2));
						DamageHandler.damageEntity(entity, damage, this);
						hitted = true;
					}
				}
			}
		} else {
			remove();
			return;
		}
		if (!isEarthbendable(player, location.getBlock()) && !isTransparent(location.getBlock())) {
			remove();
			return;
		}
		return;
	}
	
	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public String getName() {
		return "EarthLine";
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
		return "* JedCore Addon *\n" + config.getString("Abilities.Earth.EarthLine.Description");
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
		return config.getBoolean("Abilities.Earth.EarthLine.Enabled");
	}
}
