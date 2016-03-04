package com.jedk1.jedcore.ability.firebending;

import com.jedk1.jedcore.JedCore;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.firebending.BlazeArc;
import com.projectkorra.projectkorra.firebending.FireDamageTimer;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.concurrent.ConcurrentHashMap;

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
			
			if (controllable) {
				direction = player.getLocation().getDirection();
			}
			
			location = location.add(direction.clone().multiply(1));
			if(GeneralMethods.isSolid(location.getBlock()) || isWater(location.getBlock())){
				distanceTravelled = range;
				return;
			}
			
			new BlazeArc(player, location, direction, 2);
			ParticleEffect.LARGE_SMOKE.display(new Vector(0, 0, 0), 0f, location, 257D);
			ParticleEffect.LARGE_SMOKE.display(new Vector(0, 0, 0), 0f, location, 257D);
			for (int j = 0; j < 5; j++) {
				ParticleEffect.FLAME.display(new Vector(0, 0, 0), 0f, location, 257D);
			}
			
			for(Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 2)){
				if(entity instanceof LivingEntity && entity.getEntityId() != player.getEntityId() && !(entity instanceof ArmorStand)){
					doDamage((LivingEntity) entity);
				}
			}
		}
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