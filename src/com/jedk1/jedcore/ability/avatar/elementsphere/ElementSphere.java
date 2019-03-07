package com.jedk1.jedcore.ability.avatar.elementsphere;

import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.configuration.JedCoreConfig;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AvatarAbility;
import com.projectkorra.projectkorra.ability.MultiAbility;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager.MultiAbilityInfoSub;
import com.projectkorra.projectkorra.util.ParticleEffect;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class ElementSphere extends AvatarAbility implements AddonAbility, MultiAbility {

	public static ConcurrentHashMap<Player, HashMap<Integer, String>> abilities = new ConcurrentHashMap<Player, HashMap<Integer, String>>();

	private World world;

	public int airUses;
	public int fireUses;
	public int waterUses;
	public int earthUses;

	public long cooldown;
	public long duration;
	private double height;
	private double speed;

	private boolean setup;
	private Location location;
	private double yaw;
	private int point;
	private long time;
	
	public long cooldownAir;
	public long cooldownEarth;
	public long cooldownFire;
	public long cooldownWater;
	public long cooldownStream;

	private long lastClickTime;

	Random rand = new Random();

	public ElementSphere(Player player) {
		super(player);
		ElementSphere oldES = getAbility(player, ElementSphere.class);
		if (oldES != null) {
			if (player.isSneaking()) {
				oldES.prepareCancel();
			} else {
				if (oldES.setup) {
					if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
						return;
					}

					switch (player.getInventory().getHeldItemSlot()) {
						case 0:
							if (player.hasPermission("bending.ability.ElementSphere.Air")) {
								new ESAir(player);
							}
							break;
						case 1:
							if (player.hasPermission("bending.ability.ElementSphere.Earth")) {
								new ESEarth(player);
							}
							break;
						case 2:
							if (player.hasPermission("bending.ability.ElementSphere.Fire")) {
								new ESFire(player);
							}
							break;
						case 3:
							if (player.hasPermission("bending.ability.ElementSphere.Water")) {
								new ESWater(player);
							}
							break;
						case 4:
							if (player.hasPermission("bending.ability.ElementSphere.Stream")) {
								new ESStream(player);
							}
							break;
						default:
							break;
					}
				}
			}
			return;
		}

		setFields();
		location = player.getLocation().clone().subtract(0, 1, 0);

		if (bPlayer.canBend(this)) {
			world = player.getWorld();
			time = System.currentTimeMillis() + duration;
			MultiAbilityManager.bindMultiAbility(player, "ElementSphere");
			bPlayer.addCooldown(this);
			flightHandler.createInstance(player, this.getName());
			start();
			if (ChatColor.stripColor(bPlayer.getBoundAbilityName()) == null) {
				remove();
				return;
			}
		}
	}

	public void setFields() {
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);
		
		airUses = config.getInt("Abilities.Avatar.ElementSphere.Air.Uses");
		fireUses = config.getInt("Abilities.Avatar.ElementSphere.Fire.Uses");
		waterUses = config.getInt("Abilities.Avatar.ElementSphere.Water.Uses");
		earthUses = config.getInt("Abilities.Avatar.ElementSphere.Earth.Uses");
		cooldown = config.getLong("Abilities.Avatar.ElementSphere.Cooldown");
		duration = config.getLong("Abilities.Avatar.ElementSphere.Duration");
		height = config.getDouble("Abilities.Avatar.ElementSphere.MaxControlledHeight");
		speed = config.getDouble("Abilities.Avatar.ElementSphere.FlySpeed");
	}

	@Override
	public void progress() {
		if (player.isDead() || !player.isOnline() || world != player.getWorld()) {
			remove();
			return;
		}
		if (player.getGameMode().equals(GameMode.SPECTATOR)) {
			remove();
			return;
		}
		if (!bPlayer.isToggled()) {
			remove();
			return;
		}
		if (!MultiAbilityManager.hasMultiAbilityBound(player, "ElementSphere")) {
			remove();
			return;
		}
		if (System.currentTimeMillis() > time && duration > 0) {
			remove();
			return;
		}
		if (airUses == 0 && fireUses == 0 && waterUses == 0 && earthUses == 0) {
			remove();
			return;
		}
		player.setFallDistance(0);
		if (player.isSneaking())
			player.setVelocity(player.getLocation().getDirection().multiply(speed));

		Block block = getGround();
		if (block != null) {
			double dy = player.getLocation().getY() - block.getY();
			if (dy > height) {
				removeFlight();
			} else {
				allowFlight();
			}
		}

		for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 2.5)) {
			if (!GeneralMethods.isRegionProtectedFromBuild(player, "ElementSphere", entity.getLocation())) {
				if (entity instanceof LivingEntity && entity.getEntityId() != player.getEntityId() && !(entity instanceof ArmorStand)) {
					entity.setVelocity(entity.getLocation().toVector().subtract(player.getLocation().toVector()).multiply(1));
				}
			}
		}

		location = player.getLocation().clone().subtract(0, 1, 0);
		playParticles();
		setup = true;
	}

	private void allowFlight() {
		if (!player.getAllowFlight()) {
			player.setAllowFlight(true);
		}
		if (!player.isFlying()) {
			player.setFlying(true);
		}
	}

	private void removeFlight() {
		if (player.getAllowFlight()) {
			player.setAllowFlight(false);
		}
		if (player.isFlying()) {
			player.setFlying(false);
		}
	}

	private Block getGround() {
		Block standingblock = player.getLocation().getBlock();
		for (int i = 0; i <= height + 5; i++) {
			Block block = standingblock.getRelative(BlockFace.DOWN, i);
			if (GeneralMethods.isSolid(block) || block.isLiquid()) {
				return block;
			}
		}
		return null;
	}

	private void playParticles() {
		Location fakeLoc = location.clone();
		fakeLoc.setPitch(0);
		fakeLoc.setYaw((float) (yaw += 40));
		Vector direction = fakeLoc.getDirection();
		if (airUses != 0)
			for (double j = -180; j <= 180; j += 45) {
				Location tempLoc = fakeLoc.clone();
				Vector newDir = direction.clone().multiply(2 * Math.cos(Math.toRadians(j)));
				tempLoc.add(newDir);
				tempLoc.setY(tempLoc.getY() + 2 + (2 * Math.sin(Math.toRadians(j))));
				if (rand.nextInt(30) == 0) {
					ParticleEffect.SPELL_MOB.display(tempLoc, 0, 255, 255, 255, 0.003);
				} else {
					ParticleEffect.SPELL_MOB_AMBIENT.display(tempLoc, 1, 0, 0, 0, 0);
				}
			}

		point++;
		if (fireUses != 0)
			for (int i = -180; i < 180; i += 40) {
				double angle = (i * Math.PI / 180);
				double x = 2 * Math.cos(angle + point);
				double z = 2 * Math.sin(angle + point);
				Location loc = location.clone();
				loc.add(x, 2, z);
				ParticleEffect.FLAME.display(loc, 0, 0, 0, 0, 1);
			}

		point++;
		Location location = this.location.clone().add(0, 2, 0);
		for (int i = -180; i < 180; i += 30) {
			double angle = (i * Math.PI / 180);
			double xRotation = 3.141592653589793D / 3 * 2.1;
			Vector v = new Vector(Math.cos(angle + point), Math.sin(angle + point), 0.0D).multiply(2);
			Vector v1 = v.clone();
			rotateAroundAxisX(v, xRotation);
			rotateAroundAxisY(v, -((location.getYaw() * Math.PI / 180) - 1.575));
			rotateAroundAxisX(v1, -xRotation);
			rotateAroundAxisY(v1, -((location.getYaw() * Math.PI / 180) - 1.575));

			if (waterUses != 0)
				GeneralMethods.displayColoredParticle("06C1FF", location.clone().add(v));

			if (earthUses != 0)
				GeneralMethods.displayColoredParticle("754719", location.clone().add(v1));
		}

		if (point == 360)
			point = 0;
	}

	private Vector rotateAroundAxisX(Vector v, double angle) {
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);
		double y = v.getY() * cos - v.getZ() * sin;
		double z = v.getY() * sin + v.getZ() * cos;
		return v.setY(y).setZ(z);
	}

	private Vector rotateAroundAxisY(Vector v, double angle) {
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);
		double x = v.getX() * cos + v.getZ() * sin;
		double z = v.getX() * -sin + v.getZ() * cos;
		return v.setX(x).setZ(z);
	}

	@Override
	public void remove() {
		super.remove();
		MultiAbilityManager.unbindMultiAbility(player);
		flightHandler.removeInstance(player, this.getName());
	}

	public void prepareCancel() {
		if (System.currentTimeMillis() < lastClickTime + 500L) {
			remove();
		} else {
			lastClickTime = System.currentTimeMillis();
		}
	}

	public int getAirUses() {
		return airUses;
	}

	public void setAirUses(int airuses) {
		this.airUses = airuses;
	}

	public int getEarthUses() {
		return earthUses;
	}

	public void setEarthUses(int earthuses) {
		this.earthUses = earthuses;
	}

	public int getFireUses() {
		return fireUses;
	}

	public void setFireUses(int fireuses) {
		this.fireUses = fireuses;
	}

	public int getWaterUses() {
		return waterUses;
	}

	public void setWaterUses(int wateruses) {
		this.waterUses = wateruses;
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
		return "ElementSphere";
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
		return "* JedCore Addon *\n" + config.getString("Abilities.Avatar.ElementSphere.Description");
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
		return config.getBoolean("Abilities.Avatar.ElementSphere.Enabled");
	}

	@Override
	public ArrayList<MultiAbilityInfoSub> getMultiAbilities() {
		ArrayList<MultiAbilityInfoSub> elementsphere = new ArrayList<MultiAbilityInfoSub>();
		elementsphere.add(new MultiAbilityInfoSub("Air", Element.AIR));
		elementsphere.add(new MultiAbilityInfoSub("Earth", Element.EARTH));
		elementsphere.add(new MultiAbilityInfoSub("Fire", Element.FIRE));
		elementsphere.add(new MultiAbilityInfoSub("Water", Element.WATER));
		elementsphere.add(new MultiAbilityInfoSub("Stream", Element.AVATAR));
		return elementsphere;
	}
}
