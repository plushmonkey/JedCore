package com.jedk1.jedcore.util;

import com.projectkorra.projectkorra.ability.Ability;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class TempFallingBlock {

	public static ConcurrentHashMap<FallingBlock, TempFallingBlock> instances = new ConcurrentHashMap<FallingBlock, TempFallingBlock>();
	
	private FallingBlock fallingblock;
	private Ability ability;
	private long creation;
	private boolean expire;
	
	public TempFallingBlock(Location location, Material material, byte data, Vector veloctiy, Ability ability) {
		this(location, material, data, veloctiy, ability, false);
	}
	
	@SuppressWarnings("deprecation")
	public TempFallingBlock(Location location, Material material, byte data, Vector veloctiy, Ability ability, boolean expire) {
		this.fallingblock = location.getWorld().spawnFallingBlock(location, material, data);
		this.fallingblock.setVelocity(veloctiy);
		this.fallingblock.setDropItem(false);
		this.ability = ability;
		this.creation = System.currentTimeMillis();
		this.expire = expire;
		instances.put(fallingblock, this);
	}
	
	public static void manage() {
		for (TempFallingBlock tfb : instances.values()) {
			if (tfb.canExpire() && System.currentTimeMillis() > tfb.getCreationTime() + 5000) {
				tfb.remove();
			}
		}
	}
	
	public static TempFallingBlock get(FallingBlock fallingblock) {
		if (isTempFallingBlock(fallingblock)) {
			return instances.get(fallingblock);
		}
		return null;
	}
	
	public static boolean isTempFallingBlock(FallingBlock fallingblock) {
		return instances.containsKey(fallingblock);
	}
	
	public static void removeFallingBlock(FallingBlock fallingblock) {
		if (isTempFallingBlock(fallingblock)) {
			fallingblock.remove();
			instances.remove(fallingblock);
		}
	}
	
	public static void removeAllFallingBlocks() {
		for (FallingBlock fallingblock : instances.keySet()) {
			fallingblock.remove();
			instances.remove(fallingblock);
		}
	}
	
	public static List<TempFallingBlock> getFromAbility(Ability ability) {
		List<TempFallingBlock> tfbs = new ArrayList<TempFallingBlock>();
		for (TempFallingBlock tfb :  instances.values()) {
			if (tfb.getAbility().equals(ability)) {
				tfbs.add(tfb);
			}
		}
		return tfbs;
	}
	
	public void remove() {
		fallingblock.remove();
		instances.remove(fallingblock);
	}
	
	public FallingBlock getFallingBlock() {
		return fallingblock;
	}
	
	public Ability getAbility() {
		return ability;
	}
	
	public Material getMaterial() {
		return fallingblock.getMaterial();
	}

	@SuppressWarnings("deprecation")
	public byte getData() {
		return fallingblock.getBlockData();
	}
	
	public Location getLocation() {
		return fallingblock.getLocation();
	}
	
	public long getCreationTime() {
		return creation;
	}
	
	public boolean canExpire() {
		return expire;
	}
}
