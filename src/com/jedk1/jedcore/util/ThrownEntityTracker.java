package com.jedk1.jedcore.util;

import com.jedk1.jedcore.JedCore;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.util.DamageHandler;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ThrownEntityTracker {

	public static ConcurrentHashMap<Entity, ThrownEntityTracker> instances = new ConcurrentHashMap<Entity, ThrownEntityTracker>();
	public static boolean collisions = JedCore.plugin.getConfig().getBoolean("Properties.MobCollisions.Enabled");
	private long delay;
	private long fireTime;
	private Entity entity;
	private Player instigator;
	private Vector thisVelocity;
	private Ability ability;

	public ThrownEntityTracker(Ability ability, Entity e, Player instigator, long delay) {
		entity = e;
		this.instigator = instigator;
		fireTime = System.currentTimeMillis();
		this.delay = delay;
		thisVelocity = e.getVelocity();
		this.delay = delay;
		instances.put(entity, this);
	}

	public void update() {
		if (System.currentTimeMillis() < fireTime + delay) {
			return;
		}
		if (!collisions || entity.isOnGround()) {
			remove();
			return;
		}
		thisVelocity = entity.getVelocity().clone();
		List<Entity> nearby = GeneralMethods.getEntitiesAroundPoint(entity.getLocation(), 2D);
		if (nearby.contains(entity)) {
			nearby.remove(entity);
		}
		if (nearby.contains(instigator)) {
			nearby.remove(instigator);
		}
		if (nearby.size() != 0) {
			entity.setVelocity(thisVelocity.multiply(0.5D));
			for(Entity e : nearby){
				e.setVelocity(entity.getVelocity().multiply(0.25D).add(GeneralMethods.getDirection(entity.getLocation(), e.getLocation()).multiply(2)));
				if (e instanceof LivingEntity) {
					DamageHandler.damageEntity(e, 2D, ability);
				}
				if (entity instanceof LivingEntity) {
					DamageHandler.damageEntity(entity, 1D, ability);
					((LivingEntity) entity).setNoDamageTicks(0);
				}
			}
			remove();
		}
	}

	public static void updateAll() {
		for (Entity entity : instances.keySet()) {
			if (entity == null) {
				instances.remove(entity);
				continue;
			}
			instances.get(entity).update();
		}
	}

	public void remove() {
		remove(entity);
	}

	public static void remove(Entity entity) {
		if (instances.containsKey(entity)) {
			instances.remove(entity);
		}
	}
	
	public static void removeAll() {
		instances.clear();
	}

}
