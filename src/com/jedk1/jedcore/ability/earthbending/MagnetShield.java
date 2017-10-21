package com.jedk1.jedcore.ability.earthbending;

import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.configuration.JedCoreConfig;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.MetalAbility;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Arrays;

public class MagnetShield extends MetalAbility implements AddonAbility {

	private final static Material[] METAL = { Material.IRON_INGOT, Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS, Material.IRON_BLOCK, Material.IRON_AXE, Material.IRON_PICKAXE, Material.IRON_SWORD, Material.IRON_HOE, Material.IRON_SPADE, Material.IRON_DOOR };

	public MagnetShield(Player player) {
		super(player);
		
		if (!bPlayer.canBendIgnoreCooldowns(this) || !bPlayer.canMetalbend()) {
			return;
		}
		
		if (hasAbility(player, MagnetShield.class)) {
			((MagnetShield) getAbility(player, MagnetShield.class)).remove();
			return;
		}

		start();
	}

	@Override
	public void progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		}
		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			remove();
			return;
		}
		if (!player.isSneaking()) {
			remove();
			return;
		}

		for (Entity e : GeneralMethods.getEntitiesAroundPoint(player.getLocation(), 4)) {
			if (e instanceof Item) {
				Item i = (Item) e;

				if (Arrays.asList(METAL).contains(i.getItemStack().getType())) {
					Vector direction = GeneralMethods.getDirection(player.getLocation(), i.getLocation()).multiply(0.1);
					i.setVelocity(direction);
				}
			}

			else if (e instanceof FallingBlock) {
				FallingBlock fb = (FallingBlock) e;

				if (Arrays.asList(METAL).contains(fb.getMaterial())) {
					Vector direction = GeneralMethods.getDirection(player.getLocation(), fb.getLocation()).multiply(0.1);
					fb.setVelocity(direction);
					fb.setDropItem(false);
				}
			}
		}
		return;
	}
	
	@Override
	public long getCooldown() {
		return 0;
	}

	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public String getName() {
		return "MagnetShield";
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
		return "* JedCore Addon *\n" + config.getString("Abilities.Earth.MagnetShield.Description");
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
		return config.getBoolean("Abilities.Earth.MagnetShield.Enabled");
	}
}
