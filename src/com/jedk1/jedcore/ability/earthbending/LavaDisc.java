package com.jedk1.jedcore.ability.earthbending;

import java.util.List;

import com.projectkorra.projectkorra.earthbending.passive.EarthPassive;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.jedk1.jedcore.JCMethods;
import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.util.RegenTempBlock;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.LavaAbility;
import com.projectkorra.projectkorra.firebending.util.FireDamageTimer;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;

public class LavaDisc extends LavaAbility implements AddonAbility {

	private Location location;
	private Vector direction;
	private long time;
	private boolean hasHit;
	private boolean isTraveling;
	private int recallCount;
	private int angle;

	private double damage;
	private long cooldown;
	private long duration;
	private long regen;
	private long sourceRegen;
	private int particles;
	private boolean lavaOnly;
	private boolean passHit;
	private boolean damageBlocks;
	private boolean lavaTrail;
	private int recall;
	private List<String> meltable;

	public LavaDisc(Player player) {
		super(player);

		if (!bPlayer.canBend(this) || hasAbility(player, LavaDisc.class) || !bPlayer.canLavabend()) {
			return;
		}

		setFields();
		time = System.currentTimeMillis();
		isTraveling = false;
		if (prepare()) {
			start();
		}
	}

	public void setFields() {
		damage = JedCore.plugin.getConfig().getDouble("Abilities.Earth.LavaDisc.Damage");
		cooldown = JedCore.plugin.getConfig().getLong("Abilities.Earth.LavaDisc.Cooldown");
		duration = JedCore.plugin.getConfig().getLong("Abilities.Earth.LavaDisc.Duration");
		regen = JedCore.plugin.getConfig().getLong("Abilities.Earth.LavaDisc.Regen");
		sourceRegen = JedCore.plugin.getConfig().getLong("Abilities.Earth.LavaDisc.SourceRegen");
		particles = JedCore.plugin.getConfig().getInt("Abilities.Earth.LavaDisc.Particles");
		lavaOnly = JedCore.plugin.getConfig().getBoolean("Abilities.Earth.LavaDisc.LavaSourceOnly");
		passHit = JedCore.plugin.getConfig().getBoolean("Abilities.Earth.LavaDisc.ContinueAfterEntityHit");
		damageBlocks = JedCore.plugin.getConfig().getBoolean("Abilities.Earth.LavaDisc.BlockDamage");
		lavaTrail = JedCore.plugin.getConfig().getBoolean("Abilities.Earth.LavaDisc.LavaTrail");
		recall = JedCore.plugin.getConfig().getInt("Abilities.Earth.LavaDisc.RecallLimit") - 1;
		meltable = JedCore.plugin.getConfig().getStringList("Abilities.Earth.LavaDisc.AdditionalMeltableBlocks");
	}

	private boolean prepare() {
		if (getLavaSourceBlock(player, 4) != null) {
			Block block = getLavaSourceBlock(player, 4);
			new RegenTempBlock(block, Material.STATIONARY_LAVA, (byte) 4, sourceRegen);
			return true;
		} else if (getEarthSourceBlock(4) != null) {
			if (lavaOnly)
				return false;
			Block block = getEarthSourceBlock(4);
			new RegenTempBlock(block, Material.STATIONARY_LAVA, (byte) 4, sourceRegen);
			return true;
		}
		return false;
	}

	@Override
	public void progress() {
		if (player == null || player.isDead() || !player.isOnline()) {
			remove();
			return;
		}
		if (!hasAbility(player, LavaDisc.class)) {
			return;
		}
		if (!bPlayer.canBendIgnoreCooldowns(this)) {
			bPlayer.addCooldown(this);
			remove();
			return;
		}
		if (player.isSneaking() && !isTraveling) {

			location = player.getEyeLocation();
			Vector dV = location.getDirection().normalize();
			location.add(new Vector(dV.getX() * 3, dV.getY() * 3, dV.getZ() * 3));

			//TODO - Fix this. It crashes servers.
			while (!isLocationSafe() && isLocationSafe(player.getLocation())) {
				location.subtract(new Vector(dV.getX() * 0.1, dV.getY() * 0.1, dV.getZ() * 0.1));
				if (location.distance(player.getEyeLocation()) > 3) {
					break;
				}
			}

			displayLavaDisc(false);

			time = System.currentTimeMillis();
			location.setPitch(0);
			direction = location.getDirection().normalize();

		} else if (player.isSneaking() && isTraveling && isLocationSafe()) {
			if (recallCount <= recall) {
				returnToSender();
				advanceLocation();
				displayLavaDisc(true);
				if (hasHit) {
					bPlayer.addCooldown(this);
					remove();
					return;
				}
			}
		} else {
			if (System.currentTimeMillis() < time + duration && isLocationSafe()) {
				isTraveling = true;
				alterPitch();
				advanceLocation();
				displayLavaDisc(true);
				if (hasHit) {
					bPlayer.addCooldown(this);
					remove();
					return;
				}
			} else {
				bPlayer.addCooldown(this);
				remove();
				return;
			}
		}
		return;
	}

	private boolean isLocationSafe() {
		if (location == null) {
			return false;
		}
		if (location.getY() < 2 || location.getY() > (location.getWorld().getMaxHeight() - 1)) {
			return false;
		}
		Block block = location.getBlock();

		if (block == null) {
			return false;
		}

		if (!isTransparent(block)) {
			return false;
		}
		return true;
	}

	private boolean isLocationSafe(Location location) {
		if (location == null) {
			return false;
		}
		if (location.getY() < 2 || location.getY() > (location.getWorld().getMaxHeight() - 1)) {
			return false;
		}
		return true;
	}

	private void alterPitch() {
		Location loc = player.getLocation().clone();

		if (loc.getPitch() < -20)
			loc.setPitch(-20);
		if (loc.getPitch() > 20)
			loc.setPitch(20);

		direction = loc.getDirection().normalize();
	}

	private void returnToSender() {
		Location loc = player.getEyeLocation();
		Vector dV = loc.getDirection().normalize();
		loc.add(new Vector(dV.getX() * 3, dV.getY() * 3, dV.getZ() * 3));

		Vector vector = loc.toVector().subtract(location.toVector());
		direction = loc.setDirection(vector).getDirection().normalize();

		double distanceAway = location.distance(loc);
		if (distanceAway < 0.5) {
			isTraveling = false;
			recallCount++;
		}
	}

	private void advanceLocation() {
		for (int i = 0; i < 5; i++) {
			location = location.add(direction.clone().multiply(0.15));

			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 2.0D)) {
				if (entity instanceof LivingEntity && entity.getEntityId() != player.getEntityId()) {
					doDamage(entity);
					if (!passHit)
						hasHit = true;
					return;
				}
			}
		}
	}

	private void displayLavaDisc(boolean largeLava) {
		if (largeLava)
			ParticleEffect.LAVA.display(location, (float) Math.random(), (float) Math.random(), (float) Math.random(), 0.1F, particles * 2);
		else
			ParticleEffect.LAVA.display(location, (float) Math.random(), (float) Math.random(), (float) Math.random(), 0.1F, 1);

		angle += 1;
		if (angle > 360)
			angle = 0;

		for (Location l : JCMethods.getCirclePoints(location, 20, 1, angle)) {
			//ParticleEffect.SMOKE.display(l, 0F, 0F, 0F, 0F, 1);
			ParticleEffect.RED_DUST.display((float) 196, (float) 93, (float) 0, 0.005F, 0, l, 257D);
			if (largeLava && damageBlocks)
				damageBlocks(l);
		}

		for (Location l : JCMethods.getCirclePoints(location, 10, 0.5, angle)) {
			ParticleEffect.FLAME.display(l, 0F, 0F, 0F, 0.01F, 1);
			ParticleEffect.SMOKE.display(l, 0F, 0F, 0F, 0.05F, 1);
			if (largeLava && damageBlocks)
				damageBlocks(l);
		}
	}

	private void damageBlocks(Location l) {
		if (!GeneralMethods.isRegionProtectedFromBuild(player, "LavaDisc", l)) {

			if (!TempBlock.isTempBlock(l.getBlock()) && (isEarthbendable(player, l.getBlock()) || isMetal(l.getBlock()) || meltable.contains(l.getBlock().getType().name()))) {
				if (EarthPassive.isPassiveSand(l.getBlock())) {
					EarthPassive.revertSand(l.getBlock());
				}

				if (lavaTrail)
					new RegenTempBlock(l.getBlock(), Material.LAVA, (byte) 4, regen);
				else
					new RegenTempBlock(l.getBlock(), Material.AIR, (byte) 0, regen);
				ParticleEffect.LAVA.display(location, (float) Math.random(), (float) Math.random(), (float) Math.random(), 0.2F, particles * 2);
			}
		}
	}

	private void doDamage(Entity entity) {
		DamageHandler.damageEntity(entity, damage, this);
		entity.setFireTicks(20);
		new FireDamageTimer(entity, player);
		ParticleEffect.LAVA.display(entity.getLocation(), (float) Math.random(), (float) Math.random(), (float) Math.random(), 0.1F, 15);
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
		return "LavaDisc";
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
		return "* JedCore Addon *\n" + JedCore.plugin.getConfig().getString("Abilities.Earth.LavaDisc.Description");
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
		return JedCore.plugin.getConfig().getBoolean("Abilities.Earth.LavaDisc.Enabled");
	}
}
