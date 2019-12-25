package com.jedk1.jedcore.ability.earthbending;

import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.configuration.JedCoreConfig;
import com.jedk1.jedcore.util.RegenTempBlock;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.LavaAbility;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Levelled;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class LavaThrow extends LavaAbility implements AddonAbility {

	private long cooldown;
	private int range;
	private double damage;
	private int sourceRange;
	private long sourceRegen;
	private int shotMax;
	private int fireTicks;

	private Location location;
	private int shots;

	private ConcurrentHashMap<Location, Location> blasts = new ConcurrentHashMap<Location, Location>();

	public LavaThrow(Player player) {
		super(player);

		if (hasAbility(player, LavaThrow.class)) {
			LavaThrow.createBlast(player);
			return;
		}
		
		if (!bPlayer.canBend(this) || !bPlayer.canLavabend()) {
			return;
		}

		setFields();
		location = player.getLocation();
		location.setPitch(0);
		location = location.toVector().add(location.getDirection().multiply(sourceRange)).toLocation(location.getWorld());
		sourceRange = Math.round(sourceRange / 2);
		if (prepare()) {
			createBlast();
			start();
		}
	}

	public void setFields() {
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);
		
		cooldown = config.getLong("Abilities.Earth.LavaThrow.Cooldown");
		range = config.getInt("Abilities.Earth.LavaThrow.Range");
		damage = config.getDouble("Abilities.Earth.LavaThrow.Damage");
		sourceRange = config.getInt("Abilities.Earth.LavaThrow.SourceGrabRange");
		sourceRegen = config.getLong("Abilities.Earth.LavaThrow.SourceRegenDelay");
		shotMax = config.getInt("Abilities.Earth.LavaThrow.MaxShots");
		fireTicks = config.getInt("Abilities.Earth.LavaThrow.FireTicks");
	}

	@Override
	public void progress() {
		if (player == null || player.isDead() || !player.isOnline()) {
			remove();
			return;
		}
		if (player.getWorld() != location.getWorld()) {
			bPlayer.addCooldown(this);
			remove();
			return;
		}
		if (!bPlayer.canBendIgnoreCooldowns(this)) {
			bPlayer.addCooldown(this);
			remove();
			return;
		}
		if (shots >= shotMax) {
			bPlayer.addCooldown(this);
		}
		handleBlasts();
		if (blasts.isEmpty()) {
			bPlayer.addCooldown(this);
			remove();
			return;
		}
		return;
	}

	private boolean prepare() {
		Block block = getRandomSourceBlock(location, 3);
		if (block != null) {
			return true;
		}
		return false;
	}

	public void createBlast() {
		Block source = getRandomSourceBlock(location, 3);
		if (source != null) {
			shots++;
			Vector direction = player.getEyeLocation().getDirection().clone().normalize();
			Location origin = source.getLocation().clone().add(0, 2, 0);
			Location head = origin.clone();
			head.setDirection(direction);
			blasts.put(head, origin);
			new RegenTempBlock(source.getRelative(BlockFace.UP), Material.LAVA, Material.LAVA.createBlockData(bd -> ((Levelled)bd).setLevel(0)), 200);
			new RegenTempBlock(source, Material.AIR, Material.AIR.createBlockData(), sourceRegen, false);
		}
	}

	public void handleBlasts() {
		for (Location l : blasts.keySet()) {
			Location head = l.clone();
			Location origin = blasts.get(l);
			if (l.distance(origin) > range) {
				blasts.remove(l);
				continue;
			}
			if(GeneralMethods.isRegionProtectedFromBuild(this, l)){
				blasts.remove(l);
				continue;
			}
			if(GeneralMethods.isSolid(l.getBlock())){
				blasts.remove(l);
				continue;
			}
			head = head.add(head.getDirection().multiply(1));
			new RegenTempBlock(l.getBlock(), Material.LAVA, Material.LAVA.createBlockData(bd -> ((Levelled)bd).setLevel(0)), 200);
			ParticleEffect.LAVA.display(head, 1, Math.random(), Math.random(), Math.random(), 0);

			boolean hit = false;

			for(Entity entity : GeneralMethods.getEntitiesAroundPoint(l, 2.0D)){
				if(entity instanceof LivingEntity && entity.getEntityId() != player.getEntityId() && !GeneralMethods.isRegionProtectedFromBuild(this, entity.getLocation()) && !((entity instanceof Player) && Commands.invincible.contains(((Player) entity).getName()))){
					DamageHandler.damageEntity(entity, damage, this);
					blasts.remove(l);

					hit = true;
					entity.setFireTicks(this.fireTicks);
				}
			}

			if (!hit) {
				blasts.remove(l);
				blasts.put(head, origin);
			}
		}
	}

	public static Block getRandomSourceBlock(Location location, int radius) {
		Random rand = new Random();
		List<Integer> checked = new ArrayList<Integer>();
		List<Block> blocks = GeneralMethods.getBlocksAroundPoint(location, radius);
		for (int i = 0; i < blocks.size(); i++) {
			int index = rand.nextInt(blocks.size());
			while (checked.contains(index)) {
				index = rand.nextInt(blocks.size());
			}
			checked.add(index);
			Block block = blocks.get(index);
			if (block == null || !LavaAbility.isLava(block)) {
				continue;
			}
			return block;
		}
		return null;
	}
	
	public static void createBlast(Player player) {
		if (hasAbility(player, LavaThrow.class)) {
			LavaThrow lt = (LavaThrow) getAbility(player, LavaThrow.class);
			if (lt.shots < lt.shotMax) {
				lt.createBlast();
			}
		}
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
		return "LavaThrow";
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
		return "* JedCore Addon *\n" + config.getString("Abilities.Earth.LavaThrow.Description");
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
		return config.getBoolean("Abilities.Earth.LavaThrow.Enabled");
	}
}
