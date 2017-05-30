package com.jedk1.jedcore.ability.firebending;

import java.util.concurrent.ConcurrentHashMap;

import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.airbending.AirShield;
import com.projectkorra.projectkorra.firebending.FireShield;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.jedk1.jedcore.JedCore;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.firebending.BlazeArc;
import com.projectkorra.projectkorra.firebending.util.FireDamageTimer;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;

public class FireBall extends FireAbility implements AddonAbility {

	public static ConcurrentHashMap<Block, Long> ignitedBlocks = new ConcurrentHashMap<Block, Long>();
	private Location location;
	private Vector direction;
	private double distanceTravelled;
	
	private long range;
	private long fireticks;
	private long cooldown;
	private double damage;
	private boolean controllable;
	private boolean shieldCollisions;
	private boolean fireTrail;

	public FireBall(Player player){
		super(player);
		if (!bPlayer.canBend(this)) {
			return;
		}
		
		setFields();
		
		location = player.getEyeLocation();
		direction = player.getEyeLocation().getDirection().normalize();
		bPlayer.addCooldown(this);
		start();
	}

	public void setFields() {
		range = JedCore.plugin.getConfig().getLong("Abilities.Fire.FireBall.Range");
		fireticks = JedCore.plugin.getConfig().getLong("Abilities.Fire.FireBall.FireDuration");
		cooldown = JedCore.plugin.getConfig().getLong("Abilities.Fire.FireBall.Cooldown");
		damage = JedCore.plugin.getConfig().getDouble("Abilities.Fire.FireBall.Damage");
		controllable = JedCore.plugin.getConfig().getBoolean("Abilities.Fire.FireBall.Controllable");
		shieldCollisions = JedCore.plugin.getConfig().getBoolean("Abilities.Fire.FireBall.ShieldCollisions");
		fireTrail = JedCore.plugin.getConfig().getBoolean("Abilities.Fire.FireBall.FireTrail");
	}
	
	@Override
	public void progress(){
		if(player.isDead() || !player.isOnline()){
			remove();
			return;
		}
		if(distanceTravelled >= range){
			remove();
			return;
		}
		if (GeneralMethods.isRegionProtectedFromBuild(player, "FireBall", location)) {
			remove();
			return;
		}
		progressFireball();
		return;
	}
	
	private void progressFireball(){
		for(int i = 0; i < 2; i++){
			distanceTravelled ++;
			if (distanceTravelled >= range) {
				return;
			}

			if (handleCollisions()) {
				remove();
				return;
			}

			if (controllable) {
				direction = player.getLocation().getDirection();
			}
			
			location = location.add(direction.clone().multiply(1));
			if(GeneralMethods.isSolid(location.getBlock()) || isWater(location.getBlock())){
				distanceTravelled = range;
				return;
			}

			ParticleEffect.LARGE_SMOKE.display(new Vector(0, 0, 0), 0f, location, 257D);
			ParticleEffect.LARGE_SMOKE.display(new Vector(0, 0, 0), 0f, location, 257D);
			for (int j = 0; j < 5; j++) {
				ParticleEffect.FLAME.display(new Vector(0, 0, 0), 0f, location, 257D);
			}

			boolean hitTarget = false;
			
			for(Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 2)){
				if(entity instanceof LivingEntity && entity.getEntityId() != player.getEntityId() && !(entity instanceof ArmorStand)){
					doDamage((LivingEntity) entity);
					hitTarget = true;
				}
			}

			if (!hitTarget) {
				if (this.distanceTravelled > 2 && this.fireTrail) {
					new BlazeArc(player, location.clone().subtract(direction).subtract(direction), direction, 2);
				}
			} else {
				remove();
				return;
			}
		}
	}

	private boolean handleCollisions() {
		if (!shieldCollisions) return false;

		for (AirShield airShield : CoreAbility.getAbilities(AirShield.class)) {
			if (!airShield.getPlayer().getWorld().equals(this.player.getWorld()))
				continue;

			double radius = airShield.getRadius();

			Location shieldLocation = airShield.getPlayer().getEyeLocation().clone();

			if (shieldLocation.distanceSquared(this.location) <= radius * radius) {
				Vector normal = this.location.toVector().subtract(shieldLocation.toVector()).normalize();
				// Move this instance so it's at the edge of the shield.
				this.location = shieldLocation.clone().add(normal.clone().multiply(radius));
				// Reflect the direction about the normal.
				this.direction.subtract(normal.clone().multiply(2 * this.direction.dot(normal))).normalize();
				break;
			}
		}

		for (FireShield fireShield : CoreAbility.getAbilities(FireShield.class)) {
			if (fireShield.getPlayer() == this.player) continue;
			Location playerLoc = fireShield.getPlayer().getLocation().clone();

			if (!playerLoc.getWorld().equals(this.player.getWorld()))
				continue;

			if (fireShield.isShield()) {
				if (playerLoc.distanceSquared(this.location) <= fireShield.getRadius() * fireShield.getRadius())
					return true;
			} else {
				double discRadius = fireShield.getDiscRadius();
				Location tempLoc = playerLoc.clone().add(playerLoc.clone().multiply(discRadius));
				if (tempLoc.distanceSquared(this.location) <= discRadius * discRadius)
					return true;
			}
		}

		return false;
	}
	
	private void doDamage(LivingEntity entity){
		distanceTravelled = range;
		DamageHandler.damageEntity(entity, damage, this);
		entity.setFireTicks(Math.round(fireticks/50));
		new FireDamageTimer(entity, player);
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
		return "FireBall";
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
	public String getAuthor() {
		return JedCore.dev;
	}

	@Override
	public String getVersion() {
		return JedCore.version;
	}

	@Override
	public String getDescription() {
		return "* JedCore Addon *\n" + JedCore.plugin.getConfig().getString("Abilities.Fire.FireBall.Description");
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
		return JedCore.plugin.getConfig().getBoolean("Abilities.Fire.FireBall.Enabled");
	}
}