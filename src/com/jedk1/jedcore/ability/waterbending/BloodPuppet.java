package com.jedk1.jedcore.ability.waterbending;

import java.util.*;

import com.jedk1.jedcore.util.VersionUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Witch;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;

import com.jedk1.jedcore.JedCore;
import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.BloodAbility;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.util.DamageHandler;

@SuppressWarnings("deprecation")
public class BloodPuppet extends BloodAbility implements AddonAbility {

	private boolean nightOnly;
	private boolean fullMoonOnly;
	private boolean undeadMobs;
	private boolean bloodpuppetThroughBlocks;
	private boolean requireBound;
	private int distance;
	private long holdtime;
	private long cooldown;

	private long time;
	private long damagecd = 0;

	public LivingEntity puppet;
	private long lastDamageTime = 0;

	Random rand = new Random();
	
	private Integer[] transparent = {0, 6, 8, 9, 10, 11, 27, 28, 30, 31, 32, 
			37, 38, 39, 40, 50, 51, 55, 59, 63, 64, 
			65, 66, 68, 69, 70, 71, 72, 75, 76, 77, 
			78, 83, 93, 94, 104, 105, 111, 115, 117, 
			132, 141, 142, 143, 147, 148, 149, 150, 
			157, 175, 176, 177, 183, 184, 185, 187, 
			193, 194, 195, 196, 197};

	public BloodPuppet(Player player) {
		super(player);
		if (!isEligible(player, true)) {
			return;
		}

		setFields();
		time = System.currentTimeMillis() + holdtime;

		if (grab()) {
			start();
		}
	}
	
	public void setFields() {
		nightOnly = JedCore.plugin.getConfig().getBoolean("Abilities.Water.BloodPuppet.NightOnly");
		fullMoonOnly = JedCore.plugin.getConfig().getBoolean("Abilities.Water.BloodPuppet.FullMoonOnly");
		undeadMobs = JedCore.plugin.getConfig().getBoolean("Abilities.Water.BloodPuppet.UndeadMobs");
		bloodpuppetThroughBlocks = JedCore.plugin.getConfig().getBoolean("Abilities.Water.BloodPuppet.IgnoreWalls");
		requireBound = JedCore.plugin.getConfig().getBoolean("Abilities.Water.BloodPuppet.RequireBound");
		distance = JedCore.plugin.getConfig().getInt("Abilities.Water.BloodPuppet.Distance");
		holdtime = JedCore.plugin.getConfig().getLong("Abilities.Water.BloodPuppet.HoldTime");
		cooldown = JedCore.plugin.getConfig().getLong("Abilities.Water.BloodPuppet.Cooldown");
	}

	public boolean isEligible(Player player, boolean hasAbility) {
		if (!bPlayer.canBend(this) || !bPlayer.canBloodbend() || (hasAbility && hasAbility(player, BloodPuppet.class))) {
			return false;
		}
		if (nightOnly && !isNight(player.getWorld()) && !bPlayer.canBloodbendAtAnytime()) {
			return false;
		}
		return !fullMoonOnly || isFullMoon(player.getWorld()) || bPlayer.canBloodbendAtAnytime();
	}

	private boolean canAttack() {
		switch (puppet.getType()) {
			case CREEPER:
				break;
			case SKELETON:
				return true;
			case SPIDER:
				return true;
			case GIANT:
				return true;
			case ZOMBIE:
				return true;
			case SLIME:
				return true;
			case GHAST:
				return true;
			case PIG_ZOMBIE:
				return true;
			case ENDERMAN:
				return true;
			case CAVE_SPIDER:
				return true;
			case SILVERFISH:
				return true;
			case BLAZE:
				return true;
			case MAGMA_CUBE:
				return true;
			case WITCH:
				return true;
			case ENDERMITE:
				return true;
			case PLAYER:
				return true;
			default:
				break;
		}

		return false;
	}

	private boolean grab() {
		List<Entity> entities = new ArrayList<Entity>();
		for (int i = 0; i < distance; i++) {
			Location location = null;
			if (bloodpuppetThroughBlocks) {
				location = player.getTargetBlock((HashSet<Material>) null, i).getLocation();
			} else {
				Material[] materials = (Material[])Arrays.stream(transparent).map(Material::getMaterial).toArray();
				location = VersionUtil.getTargetedLocation(player, i, materials);
			}
			entities = GeneralMethods.getEntitiesAroundPoint(location, 1.7);
			if (entities.contains(player)) {
				entities.remove(player);
			}
			if (entities != null && !entities.isEmpty() && !entities.contains(player)) {
				break;
			}
		}
		if (entities == null || entities.isEmpty()) {
			return false;
		}
		Entity e = entities.get(0);

		if (e == null)
			return false;

		if (!(e instanceof LivingEntity))
			return false;

		if (!undeadMobs && com.projectkorra.projectkorra.waterbending.blood.Bloodbending.isUndead(e))
			return false;

		if ((e instanceof Player) && !canBeBloodbent((Player) e)) {
			return false;
		}
		if (GeneralMethods.isRegionProtectedFromBuild(player, "BloodPuppet", e.getLocation())) {
			return false;
		}

		for (BloodPuppet bb : getAbilities(BloodPuppet.class)) {
			if (bb.puppet.getEntityId() == e.getEntityId()) {
				return false;
			}
		}

		puppet = (LivingEntity) e;
		DamageHandler.damageEntity(puppet, 0, this);
		if (puppet instanceof Creature)
			((Creature) puppet).setTarget(null);

		if (e instanceof Player && BendingPlayer.getBendingPlayer((Player) e) != null) {
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer((Player) e);
			bPlayer.blockChi();
		}

		return true;
	}

	private boolean canBeBloodbent(Player player) {
		if (Commands.invincible.contains(player.getName())) {
			return false;
		}
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (requireBound) {
			if (bPlayer.getAbilities().containsValue("Bloodbending")) {
				return false;
			}
			if (bPlayer.getAbilities().containsValue("BloodPuppet")) {
				return false;
			}
		} else {
			if (bPlayer.canBind(getAbility("Bloodbending")) && bPlayer.canBloodbend()) {
				if ((!isDay(player.getWorld()) || bPlayer.canBloodbendAtAnytime())) {
					return false;
				}
			}
		}
		return true;
	}

	public static void attack(Player player) {
		if (hasAbility(player, BloodPuppet.class)) {
			((BloodPuppet) getAbility(player, BloodPuppet.class)).attack();
		}
	}

	private void attack() {
		if (!canAttack())
			return;

		if (System.currentTimeMillis() > lastDamageTime + damagecd) {
			lastDamageTime = System.currentTimeMillis();

			if (puppet instanceof Skeleton) {
				Skeleton skelly = (Skeleton) puppet;
				List<Entity> nearby = GeneralMethods.getEntitiesAroundPoint(skelly.getLocation(), 5);
				if (nearby.contains(puppet))
					nearby.remove(puppet);
				if (nearby.size() < 1)
					return;
				int randy = rand.nextInt(nearby.size());
				Entity target = nearby.get(randy);
				if (target instanceof LivingEntity) {
					LivingEntity e = (LivingEntity) target;
					Location loc = puppet.getLocation().getBlock().getRelative(GeneralMethods.getCardinalDirection(GeneralMethods.getDirection(puppet.getEyeLocation(), e.getEyeLocation()))).getLocation();
					Arrow a = puppet.getWorld().spawnArrow(loc, GeneralMethods.getDirection(puppet.getEyeLocation(), e.getEyeLocation()), 0.6f, 12);
					a.setShooter(puppet);
					if (e instanceof Creature)
						((Creature) e).setTarget(puppet);
				}
				return;
			}

			else if (puppet instanceof Creeper) {
				Creeper creep = (Creeper) puppet;
				creep.setPowered(true);
				return;
			}

			else if (puppet instanceof Ghast) {
				Ghast gaga = (Ghast) puppet;
				List<Entity> nearby = GeneralMethods.getEntitiesAroundPoint(gaga.getLocation(), 5);
				if (nearby.contains(puppet))
					nearby.remove(puppet);
				if (nearby.size() < 1)
					return;
				int randy = rand.nextInt(nearby.size());
				Entity target = nearby.get(randy);
				if (target instanceof LivingEntity) {
					LivingEntity e = (LivingEntity) target;
					Location loc = puppet.getLocation().getBlock().getRelative(GeneralMethods.getCardinalDirection(GeneralMethods.getDirection(puppet.getEyeLocation(), e.getEyeLocation()))).getLocation();
					Fireball fb = puppet.getWorld().spawn(loc, Fireball.class);
					fb.setVelocity(GeneralMethods.getDirection(puppet.getEyeLocation(), e.getEyeLocation()).multiply(0.25));
					fb.setIsIncendiary(true);
					fb.setShooter(puppet);
					if (e instanceof Creature)
						((Creature) e).setTarget(puppet);
				}

				return;
			}

			else if (puppet instanceof Blaze) {
				Blaze balawalaze = (Blaze) puppet;
				List<Entity> nearby = GeneralMethods.getEntitiesAroundPoint(balawalaze.getLocation(), 5);
				if (nearby.contains(puppet))
					nearby.remove(puppet);
				if (nearby.size() < 1)
					return;
				int randy = rand.nextInt(nearby.size());
				Entity target = nearby.get(randy);
				if (target instanceof LivingEntity) {
					LivingEntity e = (LivingEntity) target;
					Location loc = puppet.getLocation().getBlock().getRelative(GeneralMethods.getCardinalDirection(GeneralMethods.getDirection(puppet.getEyeLocation(), e.getEyeLocation()))).getLocation();
					Fireball fb = puppet.getWorld().spawn(loc, Fireball.class);
					fb.setVelocity(GeneralMethods.getDirection(puppet.getEyeLocation(), e.getEyeLocation()).multiply(0.5));
					fb.setShooter(puppet);
					if (e instanceof Creature)
						((Creature) e).setTarget(puppet);
				}

				return;
			}

			else if (puppet instanceof Witch) {
				Witch missmagus = (Witch) puppet;
				List<Entity> nearby = GeneralMethods.getEntitiesAroundPoint(missmagus.getLocation(), 5);
				if (nearby.contains(puppet))
					nearby.remove(puppet);
				if (nearby.size() < 1)
					return;
				int randy = rand.nextInt(nearby.size());
				Entity target = nearby.get(randy);
				if (target instanceof LivingEntity) {
					LivingEntity e = (LivingEntity) target;
					@SuppressWarnings("unused")
					Location loc = puppet.getLocation().getBlock().getRelative(GeneralMethods.getCardinalDirection(GeneralMethods.getDirection(puppet.getEyeLocation(), e.getEyeLocation()))).getLocation();
					ThrownPotion tp = missmagus.launchProjectile(ThrownPotion.class, GeneralMethods.getDirection(puppet.getEyeLocation(), e.getEyeLocation()));
					Potion potion = new Potion(PotionType.INSTANT_DAMAGE);
					potion.setSplash(true);
					tp.setItem(potion.toItemStack(1));
					tp.setVelocity(GeneralMethods.getDirection(puppet.getEyeLocation(), e.getEyeLocation()).multiply(0.125));
					tp.setShooter(puppet);
					if (e instanceof Creature)
						((Creature) e).setTarget(puppet);
				}

				return;
			}

			else {
				for (Entity e : GeneralMethods.getEntitiesAroundPoint(puppet.getLocation(), 2)) {
					if (e.getEntityId() == puppet.getEntityId())
						continue;

					if (e instanceof LivingEntity) {
						int damage = 2;
						if (puppet instanceof Player) {
							Player p = (Player) puppet;

							switch (p.getItemInHand().getType()) {
								case WOOD_SWORD:
								case GOLD_SWORD:
									damage = 5;
									break;
								case STONE_SWORD:
									damage = 6;
									break;
								case IRON_SWORD:
									damage = 7;
									break;
								case DIAMOND_SWORD:
									damage = 8;
									break;
								default:
									break;
							}
						}
						((LivingEntity) e).damage(damage, puppet);
						if (e instanceof Creature)
							((Creature) e).setTarget(puppet);
					}
				}
			}
		}
	}

	@Override
	public void progress() {
		if (player == null || !player.isOnline() || player.isDead()) {
			remove();
			return;
		}
		if (!isEligible(player, false)) {
			remove();
			return;
		}

		if (!player.isSneaking()) {
			remove();
			return;
		}

		if (System.currentTimeMillis() > time) {
			remove();
			return;
		}

		if ((puppet instanceof Player && !((Player) puppet).isOnline()) || puppet.isDead()) {
			remove();
			return;
		}

		Location newlocation = puppet.getLocation();

		Location location = VersionUtil.getTargetedLocation(player, distance + 1);
		double distance = location.distance(newlocation);
		double dx, dy, dz;
		dx = location.getX() - newlocation.getX();
		dy = location.getY() - newlocation.getY();
		dz = location.getZ() - newlocation.getZ();
		Vector vector = new Vector(dx, dy, dz);
		if (distance > .5) {
			puppet.setVelocity(vector.normalize().multiply(.5));
		} else {
			puppet.setVelocity(new Vector(0, 0, 0));
		}
		puppet.setFallDistance(0);
		if (puppet instanceof Creature) {
			((Creature) puppet).setTarget(null);
		}
		AirAbility.breakBreathbendingHold(puppet);
		return;
	}

	@Override
	public void remove() {
		if (player.isOnline()) {
			bPlayer.addCooldown(this);
		}
		if (puppet instanceof Player && puppet != null && ((Player) puppet).isOnline()) {
			BendingPlayer.getBendingPlayer((Player) puppet).unblockChi();
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
		return "BloodPuppet";
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
		return "* JedCore Addon *\n" + JedCore.plugin.getConfig().getString("Abilities.Water.BloodPuppet.Description");
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
		return JedCore.plugin.getConfig().getBoolean("Abilities.Water.BloodPuppet.Enabled");
	}
}
